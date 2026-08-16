package com.spkmainan.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Payload for the current user updating their own profile. */
public record UpdateProfileRequest(
        @NotBlank(message = "Nama wajib diisi") String name,
        @NotBlank(message = "Email wajib diisi") @Email(message = "Format email tidak valid") String email,
        String avatarUrl) {}
