package com.example.chat.global.config;

import com.example.chat.global.jwt.JwtUtil;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return false;
        }

        Cookie[] cookies = servletRequest.getServletRequest().getCookies();
        if (cookies == null) return false;

        for (Cookie cookie : cookies) {
            if ("access_token".equals(cookie.getName())) {
                try {
                    jwtUtil.validate(cookie.getValue());
                    Long userId = jwtUtil.getUserId(cookie.getValue());
                    attributes.put("userId", userId);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        }

        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
