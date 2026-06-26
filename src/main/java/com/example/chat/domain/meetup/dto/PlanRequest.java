package com.example.chat.domain.meetup.dto;

import java.time.LocalDateTime;

public record PlanRequest(
        String placeName,
        String address,
        LocalDateTime meetingAt
) {}
