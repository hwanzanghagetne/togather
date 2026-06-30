package com.example.chat.domain.meetup.service;

import com.example.chat.domain.meetup.dto.HostTransferRequest;
import com.example.chat.domain.meetup.dto.JoinRequestSummary;
import com.example.chat.domain.meetup.dto.JoinResponse;
import com.example.chat.domain.meetup.dto.MeetupCreateRequest;
import com.example.chat.domain.meetup.dto.MeetupResponse;
import com.example.chat.domain.meetup.dto.ParticipantResponse;
import com.example.chat.domain.meetup.dto.PlanRequest;
import com.example.chat.domain.meetup.dto.PlanResponse;
import com.example.chat.domain.meetup.dto.ReviewRequest;

import java.util.List;

public interface MeetupService {
    MeetupResponse create(Long userId, MeetupCreateRequest request);
    List<MeetupResponse> findNearby(Long userId, double lat, double lng, double radius);
    MeetupResponse findById(Long userId, Long meetupId);
    JoinResponse join(Long userId, Long meetupId);
    JoinResponse leave(Long userId, Long meetupId);
    JoinResponse transferHost(Long userId, Long meetupId, Long newHostId);
    List<MeetupResponse> getMyMeetups(Long userId);
    void deleteMeetup(Long userId, Long meetupId);
    void arrive(Long userId, Long meetupId);
    void review(Long userId, Long meetupId, ReviewRequest request);

    // 비공개 모임 승인 흐름
    List<JoinRequestSummary> getJoinRequests(Long hostId, Long meetupId);
    void approveJoinRequest(Long hostId, Long meetupId, Long requestId);
    void rejectJoinRequest(Long hostId, Long meetupId, Long requestId);

    // 참가자 목록
    List<ParticipantResponse> getParticipants(Long meetupId);

    // 장소·일정 확정 카드
    PlanResponse savePlan(Long userId, Long meetupId, PlanRequest request);
    PlanResponse getPlan(Long meetupId);
}
