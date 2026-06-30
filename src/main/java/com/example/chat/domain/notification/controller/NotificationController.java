package com.example.chat.domain.notification.controller;

import com.example.chat.domain.notification.dto.NotificationResponse;
import com.example.chat.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/members/me/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal Long userId) {
        return notificationService.subscribe(userId);
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(notificationService.getNotifications(userId));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markRead(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long notificationId
    ) {
        notificationService.markRead(userId, notificationId);
        return ResponseEntity.ok().build();
    }
}
