package com.example.chat.domain.notification.service;

import com.example.chat.domain.notification.SseEmitterStore;
import com.example.chat.domain.notification.domain.Notification;
import com.example.chat.domain.notification.domain.NotificationType;
import com.example.chat.domain.notification.dto.NotificationResponse;
import com.example.chat.domain.notification.repository.NotificationRepository;
import com.example.chat.domain.user.domain.User;
import com.example.chat.domain.user.repository.UserRepository;
import com.example.chat.global.exception.BusinessException;
import com.example.chat.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SseEmitterStore sseEmitterStore;

    @Override
    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        sseEmitterStore.add(userId, emitter);

        // 연결 직후 더미 이벤트 — 브라우저가 연결을 확인하는 용도
        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (Exception e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

    @Override
    @Transactional
    public void send(Long toUserId, NotificationType type, Long meetupId, String actorNickname) {
        User user = userRepository.findById(toUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Notification notification = notificationRepository.save(
                Notification.create(user, type, meetupId, actorNickname)
        );

        sseEmitterStore.send(toUserId, NotificationResponse.from(notification));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public void markRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!notification.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        notification.markRead();
    }
}
