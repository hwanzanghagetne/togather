package com.example.chat.domain.meetup.service;

import com.example.chat.domain.meetup.dto.MeetupCreateRequest;
import com.example.chat.domain.meetup.dto.MeetupResponse;
import com.example.chat.domain.meetup.dto.ReviewRequest;

import java.util.List;

public interface MeetupService {
    MeetupResponse create(Long userId, MeetupCreateRequest request);
    List<MeetupResponse> findNearby(double lat, double lng, double radius);
    MeetupResponse findById(Long meetupId);
    MeetupResponse join(Long userId, Long meetupId);
    MeetupResponse leave(Long userId, Long meetupId);
    void arrive(Long userId, Long meetupId);
    void review(Long userId, Long meetupId, ReviewRequest request);
}
