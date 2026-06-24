package com.example.chat.domain.chat.service;

import com.example.chat.domain.chat.dto.ChatMessageRequest;
import com.example.chat.domain.chat.dto.ChatMessageResponse;

import java.util.List;

public interface ChatService {
    ChatMessageResponse sendMessage(Long userId, Long meetupId, ChatMessageRequest request);
    List<ChatMessageResponse> getMessages(Long meetupId);
}
