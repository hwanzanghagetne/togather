package com.example.chat.domain.chat.controller;

import com.example.chat.domain.chat.dto.ChatMessageRequest;
import com.example.chat.domain.chat.dto.ChatMessageResponse;
import com.example.chat.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    // WebSocket: /app/chat/{meetupId} 로 오는 메시지 처리
    @MessageMapping("/chat/{meetupId}")
    public void sendMessage(
            @DestinationVariable Long meetupId,  // URL 경로에서 meetupId 꺼냄
            @Payload ChatMessageRequest request, // 메시지 본문
            SimpMessageHeaderAccessor headerAccessor // WebSocket 세션 정보
    ) {
        // WebSocket 연결 시 저장해둔 userId 꺼냄 (5단계에서 설명)
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");

        ChatMessageResponse response = chatService.sendMessage(userId, meetupId, request);

        // /topic/chat/{meetupId} 구독자 전원에게 브로드캐스트
        messagingTemplate.convertAndSend("/topic/chat/" + meetupId, response);
    }

    // HTTP: 채팅방 입장 시 이전 메시지 이력 조회
    @GetMapping("/api/chat/{meetupId}")
    @org.springframework.web.bind.annotation.ResponseBody
    public ResponseEntity<List<ChatMessageResponse>> getMessages(
            @PathVariable Long meetupId
    ) {
        return ResponseEntity.ok(chatService.getMessages(meetupId));
    }


}
