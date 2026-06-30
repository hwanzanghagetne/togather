package com.example.chat.domain.user.dto;

public record UserUpdateRequest(
        String nickname,
        String bio
) {}
