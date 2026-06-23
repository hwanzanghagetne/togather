package com.example.chat.domain.meetup.dto;

import com.example.chat.domain.meetup.domain.Meetup;
import com.example.chat.domain.meetup.domain.MeetupCategory;
import com.example.chat.domain.meetup.domain.MeetupStatus;

import java.time.LocalDateTime;

public record MeetupResponse(
        Long id,
        Long hostId,
        String hostNickname,
        String title,
        String description,
        Double latitude,
        Double longitude,
        String address,
        MeetupCategory category,
        int maxParticipants,
        int currentCount,
        MeetupStatus status,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {
    public static MeetupResponse from(Meetup meetup) {
        return new MeetupResponse(
                meetup.getId(),
                meetup.getHost().getId(),
                meetup.getHost().getNickname(),
                meetup.getTitle(),
                meetup.getDescription(),
                meetup.getLatitude(),
                meetup.getLongitude(),
                meetup.getAddress(),
                meetup.getCategory(),
                meetup.getMaxParticipants(),
                meetup.getCurrentCount(),
                meetup.getStatus(),
                meetup.getExpiresAt(),
                meetup.getCreatedAt()
        );
    }
}
