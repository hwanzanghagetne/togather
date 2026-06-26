package com.example.chat.domain.meetup.dto;

import com.example.chat.domain.meetup.domain.JoinStatus;
import com.example.chat.domain.meetup.domain.Meetup;
import com.example.chat.domain.meetup.domain.MeetupCategory;
import com.example.chat.domain.meetup.domain.MeetupStatus;
import com.example.chat.domain.meetup.domain.MeetupVisibility;
import com.example.chat.domain.meetup.domain.TimeMode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record MeetupResponse(
        Long id,
        Long hostId,
        String hostNickname,
        String title,
        String description,
        MeetupCategory category,
        MeetupVisibility visibility,
        JoinStatus joinStatus,
        boolean joinedByMe,

        // 지도용 좌표 (항상 공개)
        Double blurredLatitude,
        Double blurredLongitude,

        // 정확 좌표 (HOST/JOINED만 공개, 나머지 null)
        Double latitude,
        Double longitude,

        String address,
        String exactAddress,         // HOST/JOINED만 공개
        boolean exactAddressExposed,

        Integer minAge,
        Integer maxAge,
        Integer maxParticipants,
        int currentCount,

        LocalDate meetingDate,
        TimeMode timeMode,
        LocalTime meetingTime,

        MeetupStatus status,
        LocalDateTime visibleUntil,
        LocalDateTime createdAt,

        Integer distanceMeters       // /nearby 응답에서만 채워짐, 단건 조회는 null
) {
    public static MeetupResponse of(Meetup meetup, JoinStatus joinStatus, Double distanceMeters) {
        boolean exposed = joinStatus == JoinStatus.HOST || joinStatus == JoinStatus.JOINED;
        Long id = meetup.getId();

        // 결정론적 blur: meetupId 기반 ±~300m 오프셋
        double latOff = ((id % 7) - 3) * 0.001;
        double lngOff = ((id * 13L % 11) - 5) * 0.0012;

        Integer distance = distanceMeters != null ? (int) Math.round(distanceMeters) : null;

        return new MeetupResponse(
                id,
                meetup.getHost().getId(),
                meetup.getHost().getNickname(),
                meetup.getTitle(),
                meetup.getDescription(),
                meetup.getCategory(),
                meetup.getVisibility(),
                joinStatus,
                joinStatus != JoinStatus.NOT_JOINED,
                meetup.getLatitude() + latOff,
                meetup.getLongitude() + lngOff,
                exposed ? meetup.getLatitude() : null,
                exposed ? meetup.getLongitude() : null,
                meetup.getAddress(),
                exposed ? meetup.getAddress() : null,
                exposed,
                meetup.getMinAge(),
                meetup.getMaxAge(),
                meetup.getMaxParticipants(),
                meetup.getCurrentCount(),
                meetup.getMeetingDate(),
                meetup.getTimeMode(),
                meetup.getMeetingTime(),
                meetup.getStatus(),
                meetup.getVisibleUntil(),
                meetup.getCreatedAt(),
                distance
        );
    }
}
