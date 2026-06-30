package com.example.chat.domain.notification.dto;

import com.example.chat.domain.notification.domain.Notification;
import com.example.chat.domain.notification.domain.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        NotificationType type,
        Long meetupId,
        String actorNickname,
        boolean isRead,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getType(),
                n.getMeetupId(),
                n.getActorNickname(),
                n.isRead(),
                n.getCreatedAt()
        );
    }
}
