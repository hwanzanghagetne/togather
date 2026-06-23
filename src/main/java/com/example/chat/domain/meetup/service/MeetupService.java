package com.example.chat.domain.meetup.service;

import com.example.chat.domain.meetup.dto.MeetupCreateRequest;
import com.example.chat.domain.meetup.dto.MeetupResponse;

import java.util.List;

public interface MeetupService {
    MeetupResponse create(Long userId, MeetupCreateRequest request);
    List<MeetupResponse> findNearby(double lat, double lng, double radius);
    MeetupResponse findById(Long meetupId);
}
