package com.spkmainan.auth;

import com.spkmainan.auth.dto.LoginRequest;
import com.spkmainan.auth.dto.LoginResponse;
import com.spkmainan.common.exception.ResourceNotFoundException;
import com.spkmainan.security.JwtService;
import com.spkmainan.user.User;
import com.spkmainan.user.UserRepository;
import com.spkmainan.user.dto.UserResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
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

    private User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}
