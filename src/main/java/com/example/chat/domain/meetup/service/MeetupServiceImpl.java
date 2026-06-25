package com.example.chat.domain.meetup.service;

import com.example.chat.domain.meetup.domain.Meetup;
import com.example.chat.domain.meetup.domain.MeetupParticipant;
import com.example.chat.domain.meetup.domain.MeetupReview;
import com.example.chat.domain.meetup.dto.MeetupCreateRequest;
import com.example.chat.domain.meetup.dto.MeetupResponse;
import com.example.chat.domain.meetup.dto.ReviewRequest;
import com.example.chat.domain.meetup.repository.MeetupParticipantRepository;
import com.example.chat.domain.meetup.repository.MeetupRepository;
import com.example.chat.domain.meetup.repository.MeetupReviewRepository;
import com.example.chat.domain.user.domain.User;
import com.example.chat.domain.user.repository.UserRepository;
import com.example.chat.global.exception.BusinessException;
import com.example.chat.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetupServiceImpl implements MeetupService {

    private final MeetupRepository meetupRepository;
    private final MeetupParticipantRepository meetupParticipantRepository;
    private final MeetupReviewRepository meetupReviewRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public MeetupResponse create(Long userId, MeetupCreateRequest request) {
        User host = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String address = request.address() != null ? request.address() : "";
        LocalDateTime expiresAt = request.expiresAt() != null ? request.expiresAt() : LocalDateTime.now().plusHours(2);

        Meetup meetup = Meetup.create(
                host,
                request.title(),
                request.description(),
                request.latitude(),
                request.longitude(),
                address,
                request.category(),
                request.maxParticipants(),
                expiresAt
        );

        return MeetupResponse.from(meetupRepository.save(meetup));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeetupResponse> findNearby(double lat, double lng, double radius) {
        return meetupRepository.findNearby(lat, lng, radius)
                .stream()
                .map(MeetupResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MeetupResponse findById(Long meetupId) {
        Meetup meetup = meetupRepository.findById(meetupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETUP_NOT_FOUND));

        return MeetupResponse.from(meetup);
    }

    @Override
    @Transactional
    public MeetupResponse join(Long userId, Long meetupId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Meetup meetup = meetupRepository.findById(meetupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETUP_NOT_FOUND));

        // OPEN이고 정원 안 찼는지 확인
        if (!meetup.isJoinable()) {
            throw new BusinessException(ErrorCode.MEETUP_NOT_JOINABLE);
        }

        // 중복 참가 확인
        if (meetupParticipantRepository.existsByMeetupAndUser(meetup, user)) {
            throw new BusinessException(ErrorCode.ALREADY_JOINED);
        }

        meetupParticipantRepository.save(MeetupParticipant.create(meetup, user));
        meetup.join(); // currentCount++ + 정원 차면 CLOSED

        return MeetupResponse.from(meetup);
    }

    @Override
    @Transactional
    public MeetupResponse leave(Long userId, Long meetupId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Meetup meetup = meetupRepository.findById(meetupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETUP_NOT_FOUND));

        // 호스트는 참가 취소 불가 (모임 해체는 별도 기능)
        if (userId.equals(meetup.getHost().getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        MeetupParticipant participant = meetupParticipantRepository.findByMeetupAndUser(meetup, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        meetupParticipantRepository.delete(participant);
        meetup.leave(); // currentCount-- + CLOSED였으면 OPEN 복구

        return MeetupResponse.from(meetup);
    }

    @Override
    @Transactional
    public void arrive(Long userId, Long meetupId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Meetup meetup = meetupRepository.findById(meetupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETUP_NOT_FOUND));

        MeetupParticipant participant = meetupParticipantRepository.findByMeetupAndUser(meetup, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));

        participant.arrive();
    }

    @Override
    @Transactional
    public void review(Long userId, Long meetupId, ReviewRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Meetup meetup = meetupRepository.findById(meetupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETUP_NOT_FOUND));

        if (!meetupParticipantRepository.existsByMeetupAndUser(meetup, user)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (meetupReviewRepository.existsByMeetupIdAndReviewerId(meetupId, userId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_REVIEW);
        }

        meetupReviewRepository.save(MeetupReview.create(meetup, user, request.rating(), request.tags()));
    }
}
