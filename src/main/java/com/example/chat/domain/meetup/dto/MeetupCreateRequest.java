package com.example.chat.domain.meetup.dto;

import com.example.chat.domain.meetup.domain.MeetupCategory;
import com.example.chat.domain.meetup.domain.MeetupVisibility;
import com.example.chat.domain.meetup.domain.TimeMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record MeetupCreateRequest(

        @NotBlank
        String title,

        String description,

        @NotNull
        Double latitude,

        @NotNull
        Double longitude,

        String address,         // nullable — reverse geocoding 없이 위경도만 보낼 수 있음

        @NotNull
        MeetupCategory category,

        MeetupVisibility visibility,    // nullable → 기본값 PUBLIC

        @Min(2)
        Integer maxParticipants,        // nullable → 정원 제한 없음

        Integer minAge,
        Integer maxAge,

        LocalDate meetingDate,

        TimeMode timeMode,              // nullable → 기본값 FLEXIBLE

        LocalTime meetingTime,          // EXACT 모드일 때만 의미 있음

        LocalDateTime visibleUntil      // nullable → 기본값 2시간 후
) {}
