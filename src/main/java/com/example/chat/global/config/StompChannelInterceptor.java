package com.example.chat.global.config;

import com.example.chat.global.exception.BusinessException;
import com.example.chat.global.exception.ErrorCode;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class StompChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // JwtHandshakeInterceptor가 HTTP 핸드셰이크 시 userId를 세션에 저장해둠
        // CONNECT 시 그 값이 있는지만 확인 (방어적 검증)
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            if (sessionAttributes == null || sessionAttributes.get("userId") == null) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED);
            }
        }

        return message;
    }
}
