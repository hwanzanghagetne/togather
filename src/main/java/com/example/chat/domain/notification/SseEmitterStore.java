package com.example.chat.domain.notification;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterStore {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter add(Long userId, SseEmitter emitter) {
        emitters.put(userId, emitter);
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError(e -> emitters.remove(userId));
        return emitter;
    }

    public void send(Long userId, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) return;

        try {
            emitter.send(SseEmitter.event().name("notification").data(data));
        } catch (Exception e) {
            emitters.remove(userId);
        }
    }
}
