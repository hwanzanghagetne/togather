package com.example.chat.domain.notification.domain;

public enum NotificationType {
    JOIN_REQUESTED,  // 비공개 모임에 참가 요청이 왔을 때 (방장에게)
    APPROVED,        // 참가 요청이 승인됐을 때 (신청자에게)
    REJECTED         // 참가 요청이 거절됐을 때 (신청자에게)
}
