package com.example.chat.domain.meetup.dto;

import com.example.chat.domain.meetup.domain.MeetupPlan;

import java.time.LocalDateTime;

public record PlanResponse(
        Long meetupId,
        String placeName,
        String address,
        LocalDateTime meetingAt
) {
    public static PlanResponse from(MeetupPlan plan) {
        return new PlanResponse(
                plan.getMeetup().getId(),
                plan.getPlaceName(),
                plan.getAddress(),
                plan.getMeetingAt()
        );
    }
}
