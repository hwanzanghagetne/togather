package com.example.chat.domain.chat.dto;

public record ChatMessageRequest(
        String content   // 메시지 내용만 받음. 보낸 사람/시간은 서버에서 결정
) {}
