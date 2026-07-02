package com.example.chat.domain.chat.dto;

import com.example.chat.domain.chat.domain.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatMessageRequest(
        @NotBlank @Size(max = 500)
        String content,

        MessageType type  // null이면 TEXT로 처리
) {}
