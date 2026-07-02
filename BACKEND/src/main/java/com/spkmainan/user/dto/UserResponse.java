package com.spkmainan.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.spkmainan.user.User;

/**
 * Public representation of a user. Field names match what the mobile client
 * expects (see {@code User.fromJson} in the Flutter app).
 */
public record UserResponse(
        String id,
        String name,
        String email,
        @JsonProperty("avatar_url") String avatarUrl,
        String role) {

    public static UserResponse from(User user) {
        return new UserResponse(
                String.valueOf(user.getId()),
                user.getName(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getRole().name().toLowerCase());
    }
}
