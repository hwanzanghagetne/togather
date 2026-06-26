package com.example.chat.domain.meetup.dto;

import com.example.chat.domain.meetup.domain.MeetupJoinRequest;

public record JoinRequestSummary(
        Long requestId,
        Long userId,
        String nickname
) {
    public static JoinRequestSummary from(MeetupJoinRequest req) {
        return new JoinRequestSummary(
                req.getId(),
                req.getUser().getId(),
                req.getUser().getNickname()
        );
    }
}
