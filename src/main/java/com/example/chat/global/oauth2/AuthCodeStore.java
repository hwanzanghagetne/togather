package com.example.chat.global.oauth2;

import com.example.chat.global.exception.BusinessException;
import com.example.chat.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuthCodeStore {

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    public String generate(Long userId) {
        String code = UUID.randomUUID().toString();
        store.put(code, new Entry(userId, Instant.now().plusSeconds(30)));
        return code;
    }

    public Long consume(String code) {
        Entry entry = store.remove(code);
        if (entry == null || entry.expiresAt().isBefore(Instant.now())) {
            throw new BusinessException(ErrorCode.INVALID_AUTH_CODE);
        }
        return entry.userId();
    }

    private record Entry(Long userId, Instant expiresAt) {}
}
