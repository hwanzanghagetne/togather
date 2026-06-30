package com.example.chat.domain.notification.service;

import com.example.chat.domain.notification.domain.NotificationType;
import com.example.chat.domain.notification.dto.NotificationResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface NotificationService {
    SseEmitter subscribe(Long userId);
    void send(Long toUserId, NotificationType type, Long meetupId, String actorNickname);
    List<NotificationResponse> getNotifications(Long userId);
    void markRead(Long userId, Long notificationId);
}
