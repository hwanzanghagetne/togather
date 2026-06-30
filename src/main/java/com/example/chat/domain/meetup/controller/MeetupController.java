package com.example.chat.domain.meetup.controller;

import com.example.chat.domain.meetup.dto.HostTransferRequest;
import com.example.chat.domain.meetup.dto.JoinRequestSummary;
import com.example.chat.domain.meetup.dto.JoinResponse;
import com.example.chat.domain.meetup.dto.MeetupCreateRequest;
import com.example.chat.domain.meetup.dto.MeetupResponse;
import com.example.chat.domain.meetup.dto.ParticipantResponse;
import com.example.chat.domain.meetup.dto.PlanRequest;
import com.example.chat.domain.meetup.dto.PlanResponse;
import com.example.chat.domain.meetup.dto.ReviewRequest;
import com.example.chat.domain.meetup.service.MeetupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/meetups")
@RequiredArgsConstructor
public class MeetupController {

    private final MeetupService meetupService;

    @PostMapping
    public ResponseEntity<MeetupResponse> create(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid MeetupCreateRequest request
    ) {
        MeetupResponse response = meetupService.create(userId, request);
        return ResponseEntity.created(URI.create("/api/meetups/" + response.id())).body(response);
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<MeetupResponse>> findNearby(
            @AuthenticationPrincipal Long userId,
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5.0") double radius
    ) {
        return ResponseEntity.ok(meetupService.findNearby(userId, lat, lng, radius));
    }

    @GetMapping("/{meetupId}")
    public ResponseEntity<MeetupResponse> findById(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long meetupId
    ) {
        return ResponseEntity.ok(meetupService.findById(userId, meetupId));
    }

    @PostMapping("/{meetupId}/join")
    public ResponseEntity<JoinResponse> join(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long meetupId
    ) {
        return ResponseEntity.ok(meetupService.join(userId, meetupId));
    }

    @DeleteMapping("/{meetupId}/join")
    public ResponseEntity<JoinResponse> leave(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long meetupId
    ) {
        return ResponseEntity.ok(meetupService.leave(userId, meetupId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<MeetupResponse>> getMyMeetups(
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(meetupService.getMyMeetups(userId));
    }

    @DeleteMapping("/{meetupId}")
    public ResponseEntity<Void> deleteMeetup(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long meetupId
    ) {
        meetupService.deleteMeetup(userId, meetupId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{meetupId}/host")
    public ResponseEntity<JoinResponse> transferHost(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long meetupId,
            @RequestBody @Valid HostTransferRequest request
    ) {
        return ResponseEntity.ok(meetupService.transferHost(userId, meetupId, request.newHostId()));
    }

    @PostMapping("/{meetupId}/arrive")
    public ResponseEntity<Void> arrive(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long meetupId
    ) {
        meetupService.arrive(userId, meetupId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{meetupId}/review")
    public ResponseEntity<Void> review(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long meetupId,
            @RequestBody @Valid ReviewRequest request
    ) {
        meetupService.review(userId, meetupId, request);
        return ResponseEntity.ok().build();
    }

    // 비공개 모임 승인 흐름
    @GetMapping("/{meetupId}/join-requests")
    public ResponseEntity<List<JoinRequestSummary>> getJoinRequests(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long meetupId
    ) {
        return ResponseEntity.ok(meetupService.getJoinRequests(userId, meetupId));
    }

    @PostMapping("/{meetupId}/join-requests/{requestId}/approve")
    public ResponseEntity<Void> approveJoinRequest(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long meetupId,
            @PathVariable Long requestId
    ) {
        meetupService.approveJoinRequest(userId, meetupId, requestId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{meetupId}/join-requests/{requestId}/reject")
    public ResponseEntity<Void> rejectJoinRequest(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long meetupId,
            @PathVariable Long requestId
    ) {
        meetupService.rejectJoinRequest(userId, meetupId, requestId);
        return ResponseEntity.ok().build();
    }

    // 참가자 목록
    @GetMapping("/{meetupId}/participants")
    public ResponseEntity<List<ParticipantResponse>> getParticipants(
            @PathVariable Long meetupId
    ) {
        return ResponseEntity.ok(meetupService.getParticipants(meetupId));
    }

    // 장소·일정 확정 카드
    @PostMapping("/{meetupId}/plan")
    public ResponseEntity<PlanResponse> savePlan(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long meetupId,
            @RequestBody PlanRequest request
    ) {
        return ResponseEntity.ok(meetupService.savePlan(userId, meetupId, request));
    }

    @GetMapping("/{meetupId}/plan")
    public ResponseEntity<PlanResponse> getPlan(@PathVariable Long meetupId) {
        return ResponseEntity.ok(meetupService.getPlan(meetupId));
    }
}
