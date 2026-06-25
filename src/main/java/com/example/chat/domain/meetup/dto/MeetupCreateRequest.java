package com.example.chat.domain.meetup.dto;

import com.example.chat.domain.meetup.domain.MeetupCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record MeetupCreateRequest(

        @NotBlank
        String title,

        String description,

        @NotNull
        Double latitude,

        @NotNull
        Double longitude,

        String address,      // nullable — 프론트에서 reverse geocoding 없이 위경도만 보낼 수 있음

        @NotNull
        MeetupCategory category,

        @Min(2)
        int maxParticipants,

        LocalDateTime expiresAt  // nullable — null이면 서비스에서 2시간 후로 기본값 설정
) {}
