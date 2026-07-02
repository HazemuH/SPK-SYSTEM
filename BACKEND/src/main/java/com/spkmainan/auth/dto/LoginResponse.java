package com.spkmainan.auth.dto;

import com.spkmainan.user.dto.UserResponse;

/**
 * Login result. Shape matches the mobile client: {@code { "user": {...}, "token": "..." }}.
 */
public record LoginResponse(UserResponse user, String token) {
}
