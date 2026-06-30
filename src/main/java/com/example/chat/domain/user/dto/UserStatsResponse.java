package com.example.chat.domain.user.dto;

public record UserStatsResponse(
        long joinedCount,
        long hostedCount,
        long reviewCount,
        double mannerTemperature
) {}
