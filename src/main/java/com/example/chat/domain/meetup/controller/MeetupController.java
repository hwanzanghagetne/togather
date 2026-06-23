package com.example.chat.domain.meetup.controller;

import com.example.chat.domain.meetup.dto.MeetupCreateRequest;
import com.example.chat.domain.meetup.dto.MeetupResponse;
import com.example.chat.domain.meetup.service.MeetupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5.0") double radius
    ) {
        return ResponseEntity.ok(meetupService.findNearby(lat, lng, radius));
    }

    @GetMapping("/{meetupId}")
    public ResponseEntity<MeetupResponse> findById(@PathVariable Long meetupId) {
        return ResponseEntity.ok(meetupService.findById(meetupId));
    }

    @PostMapping("/{meetupId}/join")
    public ResponseEntity<MeetupResponse> join(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long meetupId
    ) {
        return ResponseEntity.ok(meetupService.join(userId, meetupId));
    }

    @DeleteMapping("/{meetupId}/join")
    public ResponseEntity<MeetupResponse> leave(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long meetupId
    ) {
        return ResponseEntity.ok(meetupService.leave(userId, meetupId));
    }
}
