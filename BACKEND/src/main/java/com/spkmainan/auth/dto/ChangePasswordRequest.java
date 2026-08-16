package com.spkmainan.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Payload for the current user changing their own password. */
public record ChangePasswordRequest(
        @NotBlank(message = "Password lama wajib diisi") String currentPassword,
        @NotBlank(message = "Password baru wajib diisi")
        @Size(min = 8, message = "Password baru minimal 8 karakter") String newPassword) {}
