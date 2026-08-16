package com.spkmainan.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Payload for creating a user (admin only). Role is validated in the service. */
public record CreateUserRequest(
        @NotBlank(message = "Username wajib diisi")
        @Size(max = 100, message = "Username maksimal 100 karakter")
        String username,

        @NotBlank(message = "Email wajib diisi")
        @Email(message = "Format email tidak valid")
        String email,

        @NotBlank(message = "Nama wajib diisi")
        String name,

        @NotBlank(message = "Password wajib diisi")
        @Size(min = 8, message = "Password minimal 8 karakter")
        String password,

        @NotBlank(message = "Role wajib diisi")
        String role) {}
