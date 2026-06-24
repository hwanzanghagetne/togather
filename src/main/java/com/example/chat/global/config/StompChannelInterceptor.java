package com.example.chat.global.config;

import com.example.chat.global.exception.BusinessException;
import com.example.chat.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StompChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // CONNECT 명령일 때만 JWT 검증 (최초 연결 시 한 번만)
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            // 프론트가 연결 시 헤더에 토큰을 담아서 보냄
            // Authorization: Bearer eyJhbGci...
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new BusinessException(com.example.chat.global.exception.ErrorCode.UNAUTHORIZED);
            }

            String token = authHeader.substring(7); // "Bearer " 제거

            try {
                jwtUtil.validate(token);
                Long userId = jwtUtil.getUserId(token);

                // 검증된 userId를 WebSocket 세션에 저장
                // 이후 메시지마다 headerAccessor.getSessionAttributes().get("userId") 로 꺼냄
                accessor.getSessionAttributes().put("userId", userId);

            } catch (BusinessException e) {
                throw new BusinessException(com.example.chat.global.exception.ErrorCode.UNAUTHORIZED);
            }
        }

        return message;
    }
}
