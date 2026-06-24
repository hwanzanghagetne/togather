package com.example.chat.domain.chat.dto;

import com.example.chat.domain.chat.domain.ChatMessage;
import com.example.chat.domain.chat.domain.MessageType;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long id,
        Long chatRoomId,
        Long senderId,
        String senderNickname,
        String content,
        MessageType type,       // TEXT(일반 메시지) or SYSTEM(입장/퇴장 알림)
        LocalDateTime createdAt
) {
    // DB에 저장된 ChatMessage 엔티티 → 응답 DTO 변환
    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getChatRoom().getId(),
                message.getSender() != null ? message.getSender().getId() : null,
                message.getSender() != null ? message.getSender().getNickname() : null,
                message.getContent(),
                message.getType(),
                message.getCreatedAt()
        );
    }

    // SYSTEM 메시지 (입장/퇴장) — DB 저장 없이 즉시 브로드캐스트할 때
    public static ChatMessageResponse ofSystem(Long chatRoomId, String content) {
        return new ChatMessageResponse(
                null, chatRoomId, null, null,
                content, MessageType.SYSTEM,
                LocalDateTime.now()
        );
    }
}
