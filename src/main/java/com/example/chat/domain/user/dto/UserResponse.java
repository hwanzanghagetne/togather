package com.example.chat.domain.user.dto;

import com.example.chat.domain.user.domain.User;

public record UserResponse(
        Long id,
        String email,
        String nickname,
        String profileImageUrl,
        String bio
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getBio()
        );
    }
}
