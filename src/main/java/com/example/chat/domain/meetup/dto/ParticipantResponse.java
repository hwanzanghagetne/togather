package com.example.chat.domain.meetup.dto;

public record ParticipantResponse(
        Long userId,
        String nickname,
        boolean arrived
) {}
