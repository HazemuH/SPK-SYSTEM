package com.spkmainan.auth;

import com.spkmainan.auth.dto.ChangePasswordRequest;
import com.spkmainan.auth.dto.LoginRequest;
import com.spkmainan.auth.dto.LoginResponse;
import com.spkmainan.auth.dto.UpdateProfileRequest;
import com.spkmainan.common.exception.BadRequestException;
import com.spkmainan.common.exception.ConflictException;
import com.spkmainan.common.exception.ResourceNotFoundException;
import com.spkmainan.security.JwtService;
import com.spkmainan.user.User;
import com.spkmainan.user.UserRepository;
import com.spkmainan.user.dto.UserResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Verifies credentials and issues a JWT. A failed authentication throws
     * {@code BadCredentialsException}, handled globally as a 401.
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        User user = getByUsername(request.username());
        String token = jwtService.generateToken(user.getUsername());
        return new LoginResponse(UserResponse.from(user), token);
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(String username) {
        return UserResponse.from(getByUsername(username));
    }

    /** Update the current user's own profile (name, email, avatar). */
    @Transactional
    public UserResponse updateProfile(String username, UpdateProfileRequest request) {
        User user = getByUsername(username);
        String email = request.email().trim();
        if (!email.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(email)) {
            throw new ConflictException("Email sudah dipakai: " + email);
        }
        user.setName(request.name().trim());
        user.setEmail(email);
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl().isBlank() ? null : request.avatarUrl().trim());
        }
        return UserResponse.from(userRepository.save(user));
    }

    /** Change the current user's own password after verifying the current one. */
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = getByUsername(username);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadRequestException("Password lama salah.");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    private User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}
