package com.spkmainan.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Payload for updating a user (admin only). Username is immutable (it is the login
 * identifier). {@code password} is optional — when blank/null the password is kept;
 * when present it is validated (min length) in the service.
 */
public record UpdateUserRequest(
        @NotBlank(message = "Email wajib diisi")
        @Email(message = "Format email tidak valid")
        String email,

        @NotBlank(message = "Nama wajib diisi")
        String name,

        @NotBlank(message = "Role wajib diisi")
        String role,

        String password) {}
