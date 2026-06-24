package com.example.chat.domain.chat.repository;

import com.example.chat.domain.chat.domain.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 채팅방 메시지 최신순 조회 (Pageable로 100개씩 끊어서 가져옴)
    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId, Pageable pageable);
}
