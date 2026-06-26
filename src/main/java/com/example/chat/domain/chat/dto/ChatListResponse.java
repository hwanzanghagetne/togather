package com.example.chat.domain.chat.dto;

import java.time.LocalDateTime;

public record ChatListResponse(
        Long meetupId,
        String title,
        String lastMessage,
        LocalDateTime lastMessageAt,
        int participantCount,
        int unreadCount
) {}
