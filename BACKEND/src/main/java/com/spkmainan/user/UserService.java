package com.spkmainan.user;

import com.spkmainan.common.exception.BadRequestException;
import com.spkmainan.common.exception.ConflictException;
import com.spkmainan.common.exception.ResourceNotFoundException;
import com.spkmainan.user.dto.CreateUserRequest;
import com.spkmainan.user.dto.UpdateUserRequest;
import com.spkmainan.user.dto.UserResponse;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin user management. Enforces uniqueness and two anti-lockout guardrails:
 * an admin cannot delete their own account, and the last remaining ADMIN cannot
 * be deleted or demoted.
 */
@Service
public class UserService {

    private static final int MIN_PASSWORD = 8;

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return repository.findAll().stream().map(UserResponse::from).toList();
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (repository.existsByUsername(request.username())) {
            throw new ConflictException("Username sudah dipakai: " + request.username());
        }
        if (repository.existsByEmail(request.email())) {
            throw new ConflictException("Email sudah dipakai: " + request.email());
        }
        User user = new User(
                request.username().trim(),
                request.email().trim(),
                request.name().trim(),
                passwordEncoder.encode(request.password()),
                parseRole(request.role()));
        return UserResponse.from(repository.save(user));
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {
        User user = getOrThrow(id);
        Role newRole = parseRole(request.role());

        // Guardrail: don't allow demoting the last remaining admin.
        if (user.getRole() == Role.ADMIN && newRole != Role.ADMIN) {
            ensureNotLastAdmin("menurunkan role admin terakhir");
        }

        String email = request.email().trim();
        if (!email.equalsIgnoreCase(user.getEmail()) && repository.existsByEmail(email)) {
            throw new ConflictException("Email sudah dipakai: " + email);
        }

        user.setEmail(email);
        user.setName(request.name().trim());
        user.setRole(newRole);

        // Optional password reset by an admin.
        if (request.password() != null && !request.password().isBlank()) {
            if (request.password().length() < MIN_PASSWORD) {
                throw new BadRequestException("Password minimal " + MIN_PASSWORD + " karakter");
            }
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        return UserResponse.from(repository.save(user));
    }

    @Transactional
    public void delete(Long id, Long currentUserId) {
        User user = getOrThrow(id);
        if (user.getId().equals(currentUserId)) {
            throw new ConflictException("Tidak dapat menghapus akun sendiri.");
        }
        if (user.getRole() == Role.ADMIN) {
            ensureNotLastAdmin("menghapus admin terakhir");
        }
        repository.delete(user);
    }

    private void ensureNotLastAdmin(String action) {
        if (repository.countByRole(Role.ADMIN) <= 1) {
            throw new ConflictException("Tidak dapat " + action + " — minimal harus ada satu admin.");
        }
    }

    private Role parseRole(String role) {
        try {
            return Role.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Role tidak valid: " + role);
        }
    }

    private User getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan: " + id));
    }
}
