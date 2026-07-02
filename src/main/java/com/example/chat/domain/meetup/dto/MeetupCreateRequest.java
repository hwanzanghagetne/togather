package com.example.chat.domain.meetup.dto;

import com.example.chat.domain.meetup.domain.MeetupCategory;
import com.example.chat.domain.meetup.domain.MeetupVisibility;
import com.example.chat.domain.meetup.domain.TimeMode;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record MeetupCreateRequest(

        @NotBlank @Size(max = 50)
        String title,

        @Size(max = 300)
        String description,

        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0")
        Double latitude,

        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0")
        Double longitude,

        @Size(max = 200)
        String address,

        @NotNull
        MeetupCategory category,

        MeetupVisibility visibility,

        @Min(2) @Max(100)
        Integer maxParticipants,

        @Min(20) @Max(100)
        Integer minAge,
        @Min(20) @Max(100)
        Integer maxAge,

        LocalDate meetingDate,

        TimeMode timeMode,              // nullable → 기본값 FLEXIBLE

        LocalTime meetingTime,          // EXACT 모드일 때만 의미 있음

        LocalDateTime visibleUntil      // nullable → 기본값 2시간 후
) {}
