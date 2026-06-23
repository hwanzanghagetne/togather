package com.example.chat.domain.meetup.dto;

import com.example.chat.domain.meetup.domain.MeetupCategory;
import jakarta.validation.constraints.Future;
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

        @NotBlank
        String address,

        @NotNull
        MeetupCategory category,

        @Min(2)
        int maxParticipants,

        @NotNull
        @Future
        LocalDateTime expiresAt
) {}
