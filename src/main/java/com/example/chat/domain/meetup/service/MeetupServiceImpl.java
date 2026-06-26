package com.example.chat.domain.meetup.service;

import com.example.chat.domain.meetup.domain.JoinRequestStatus;
import com.example.chat.domain.meetup.domain.JoinStatus;
import com.example.chat.domain.meetup.domain.Meetup;
import com.example.chat.domain.meetup.domain.MeetupJoinRequest;
import com.example.chat.domain.meetup.domain.MeetupParticipant;
import com.example.chat.domain.meetup.domain.MeetupPlan;
import com.example.chat.domain.meetup.domain.MeetupReview;
import com.example.chat.domain.meetup.domain.MeetupVisibility;
import com.example.chat.domain.meetup.domain.TimeMode;
import com.example.chat.domain.meetup.dto.JoinRequestSummary;
import com.example.chat.domain.meetup.dto.JoinResponse;
import com.example.chat.domain.meetup.dto.MeetupCreateRequest;
import com.example.chat.domain.meetup.dto.MeetupResponse;
import com.example.chat.domain.meetup.dto.ParticipantResponse;
import com.example.chat.domain.meetup.dto.PlanRequest;
import com.example.chat.domain.meetup.dto.PlanResponse;
import com.example.chat.domain.meetup.dto.ReviewRequest;
import com.example.chat.domain.meetup.repository.MeetupJoinRequestRepository;
import com.example.chat.domain.meetup.repository.MeetupParticipantRepository;
import com.example.chat.domain.meetup.repository.MeetupPlanRepository;
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
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetupServiceImpl implements MeetupService {

    private final MeetupRepository meetupRepository;
    private final MeetupParticipantRepository meetupParticipantRepository;
    private final MeetupJoinRequestRepository meetupJoinRequestRepository;
    private final MeetupPlanRepository meetupPlanRepository;
    private final MeetupReviewRepository meetupReviewRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public MeetupResponse create(Long userId, MeetupCreateRequest request) {
        User host = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String address = request.address() != null ? request.address() : "";
        LocalDateTime visibleUntil = request.visibleUntil() != null
                ? request.visibleUntil()
                : LocalDateTime.now().plusHours(2);

        Meetup meetup = Meetup.create(
                host,
                request.title(),
                request.description(),
                request.latitude(),
                request.longitude(),
                address,
                request.category(),
                request.visibility(),
                request.maxParticipants(),
                request.minAge(),
                request.maxAge(),
                request.meetingDate(),
                request.timeMode() != null ? request.timeMode() : TimeMode.FLEXIBLE,
                request.timeMode() == TimeMode.EXACT ? request.meetingTime() : null,
                visibleUntil
        );

        Meetup saved = meetupRepository.save(meetup);
        return MeetupResponse.of(saved, JoinStatus.HOST, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeetupResponse> findNearby(Long userId, double lat, double lng, double radius) {
        return meetupRepository.findNearby(lat, lng, radius)
                .stream()
                .map(m -> {
                    double distKm = haversine(lat, lng, m.getLatitude(), m.getLongitude());
                    JoinStatus status = resolveJoinStatus(userId, m);
                    return MeetupResponse.of(m, status, distKm * 1000);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MeetupResponse findById(Long userId, Long meetupId) {
        Meetup meetup = meetupRepository.findById(meetupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETUP_NOT_FOUND));
        return MeetupResponse.of(meetup, resolveJoinStatus(userId, meetup), null);
    }

    @Override
    @Transactional
    public JoinResponse join(Long userId, Long meetupId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Meetup meetup = meetupRepository.findById(meetupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETUP_NOT_FOUND));

        if (!meetup.isJoinable()) {
            throw new BusinessException(ErrorCode.MEETUP_NOT_JOINABLE);
        }

        if (meetupParticipantRepository.existsByMeetupAndUser(meetup, user)) {
            throw new BusinessException(ErrorCode.ALREADY_JOINED);
        }

        if (meetup.getVisibility() == MeetupVisibility.PRIVATE) {
            // 이미 대기 중인지 확인
            if (meetupJoinRequestRepository.existsByMeetupAndUserAndStatus(
                    meetup, user, JoinRequestStatus.PENDING)) {
                return new JoinResponse(meetupId, JoinStatus.PENDING);
            }
            meetupJoinRequestRepository.save(MeetupJoinRequest.create(meetup, user));
            return new JoinResponse(meetupId, JoinStatus.PENDING);
        }

        // PUBLIC: 즉시 참가
        meetupParticipantRepository.save(MeetupParticipant.create(meetup, user));
        meetup.join();
        return new JoinResponse(meetupId, JoinStatus.JOINED);
    }

    @Override
    @Transactional
    public JoinResponse leave(Long userId, Long meetupId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Meetup meetup = meetupRepository.findById(meetupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETUP_NOT_FOUND));

        if (userId.equals(meetup.getHost().getId())) {
            if (meetup.getCurrentCount() <= 1) {
                // 혼자인 경우 모임 종료
                meetup.closeByHost();
                return new JoinResponse(meetupId, JoinStatus.NOT_JOINED);
            }
            throw new BusinessException(ErrorCode.HOST_TRANSFER_REQUIRED);
        }

        // 참가 취소
        meetupParticipantRepository.findByMeetupAndUser(meetup, user).ifPresent(p -> {
            meetupParticipantRepository.delete(p);
            meetup.leave();
        });

        // 대기 취소
        meetupJoinRequestRepository.findByMeetupAndUser(meetup, user).ifPresent(
                meetupJoinRequestRepository::delete
        );

        return new JoinResponse(meetupId, JoinStatus.NOT_JOINED);
    }

    @Override
    @Transactional
    public void arrive(Long userId, Long meetupId) {
        Meetup meetup = meetupRepository.findById(meetupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETUP_NOT_FOUND));

        // 호스트는 별도 participant 레코드 없이 도착 처리 skip
        if (userId.equals(meetup.getHost().getId())) return;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

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

        if (!meetupParticipantRepository.existsByMeetupAndUser(meetup, user)
                && !userId.equals(meetup.getHost().getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (meetupReviewRepository.existsByMeetupIdAndReviewerId(meetupId, userId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_REVIEW);
        }

        meetupReviewRepository.save(MeetupReview.create(meetup, user, request.rating(), request.tags()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<JoinRequestSummary> getJoinRequests(Long hostId, Long meetupId) {
        Meetup meetup = meetupRepository.findById(meetupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETUP_NOT_FOUND));

        if (!hostId.equals(meetup.getHost().getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        return meetupJoinRequestRepository.findByMeetupAndStatus(meetup, JoinRequestStatus.PENDING)
                .stream()
                .map(JoinRequestSummary::from)
                .toList();
    }

    @Override
    @Transactional
    public void approveJoinRequest(Long hostId, Long meetupId, Long requestId) {
        Meetup meetup = meetupRepository.findById(meetupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETUP_NOT_FOUND));

        if (!hostId.equals(meetup.getHost().getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        MeetupJoinRequest req = meetupJoinRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        req.approve();
        meetupParticipantRepository.save(MeetupParticipant.create(meetup, req.getUser()));
        meetup.join();
    }

    @Override
    @Transactional
    public void rejectJoinRequest(Long hostId, Long meetupId, Long requestId) {
        Meetup meetup = meetupRepository.findById(meetupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETUP_NOT_FOUND));

        if (!hostId.equals(meetup.getHost().getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        MeetupJoinRequest req = meetupJoinRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        req.reject();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipantResponse> getParticipants(Long meetupId) {
        Meetup meetup = meetupRepository.findById(meetupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETUP_NOT_FOUND));

        List<ParticipantResponse> result = new ArrayList<>();
        // 호스트는 항상 첫 번째
        result.add(new ParticipantResponse(
                meetup.getHost().getId(), meetup.getHost().getNickname(), true));

        meetupParticipantRepository.findAllByMeetup(meetup).forEach(p ->
                result.add(new ParticipantResponse(
                        p.getUser().getId(), p.getUser().getNickname(), p.isArrived()))
        );
        return result;
    }

    @Override
    @Transactional
    public PlanResponse savePlan(Long userId, Long meetupId, PlanRequest request) {
        Meetup meetup = meetupRepository.findById(meetupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETUP_NOT_FOUND));

        if (!userId.equals(meetup.getHost().getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        MeetupPlan plan = meetupPlanRepository.findByMeetupId(meetupId)
                .orElse(null);

        if (plan == null) {
            plan = meetupPlanRepository.save(
                    MeetupPlan.create(meetup, request.placeName(), request.address(), request.meetingAt()));
        } else {
            plan.update(request.placeName(), request.address(), request.meetingAt());
        }

        return PlanResponse.from(plan);
    }

    @Override
    @Transactional(readOnly = true)
    public PlanResponse getPlan(Long meetupId) {
        MeetupPlan plan = meetupPlanRepository.findByMeetupId(meetupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        return PlanResponse.from(plan);
    }

    @Override
    @Transactional
    public JoinResponse transferHost(Long userId, Long meetupId, Long newHostId) {
        Meetup meetup = meetupRepository.findById(meetupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETUP_NOT_FOUND));

        if (!userId.equals(meetup.getHost().getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        User newHost = userRepository.findById(newHostId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        MeetupParticipant newHostParticipant = meetupParticipantRepository.findByMeetupAndUser(meetup, newHost)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_A_PARTICIPANT));

        // 신규 방장: participants → host 역할 전환
        meetupParticipantRepository.delete(newHostParticipant);
        meetup.transferHost(newHost);

        return new JoinResponse(meetupId, JoinStatus.NOT_JOINED);
    }

    // JoinStatus 계산: DB 조회 2회 (참가자 + 대기열)
    private JoinStatus resolveJoinStatus(Long userId, Meetup meetup) {
        if (userId == null) return JoinStatus.NOT_JOINED;
        if (userId.equals(meetup.getHost().getId())) return JoinStatus.HOST;

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return JoinStatus.NOT_JOINED;

        if (meetupParticipantRepository.existsByMeetupAndUser(meetup, user)) return JoinStatus.JOINED;
        if (meetupJoinRequestRepository.existsByMeetupAndUserAndStatus(
                meetup, user, JoinRequestStatus.PENDING)) return JoinStatus.PENDING;

        return JoinStatus.NOT_JOINED;
    }

    private double haversine(double lat1, double lng1, double lat2, double lng2) {
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
