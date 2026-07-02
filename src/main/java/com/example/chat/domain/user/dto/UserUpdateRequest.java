package com.example.chat.domain.user.dto;

import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @Size(min = 1, max = 20)
        String nickname,

        @Size(max = 200)
        String bio
) {}
