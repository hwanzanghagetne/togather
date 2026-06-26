package com.example.chat.domain.meetup.dto;

import com.example.chat.domain.meetup.domain.JoinStatus;

public record JoinResponse(
        Long meetupId,
        JoinStatus joinStatus
) {}
