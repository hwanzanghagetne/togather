package com.example.chat.domain.meetup.service;

import com.example.chat.domain.meetup.domain.JoinStatus;
import com.example.chat.domain.meetup.domain.Meetup;
import com.example.chat.domain.meetup.domain.MeetupCategory;
import com.example.chat.domain.meetup.domain.MeetupParticipant;
import com.example.chat.domain.meetup.domain.MeetupStatus;
import com.example.chat.domain.meetup.domain.MeetupVisibility;
import com.example.chat.domain.meetup.domain.TimeMode;
import com.example.chat.domain.meetup.dto.JoinResponse;
import com.example.chat.domain.meetup.dto.MeetupCreateRequest;
import com.example.chat.domain.meetup.dto.MeetupResponse;
import com.example.chat.domain.meetup.repository.MeetupJoinRequestRepository;
import com.example.chat.domain.meetup.repository.MeetupParticipantRepository;
import com.example.chat.domain.meetup.repository.MeetupPlanRepository;
import com.example.chat.domain.meetup.repository.MeetupRepository;
import com.example.chat.domain.meetup.repository.MeetupReviewRepository;
import com.example.chat.domain.user.domain.User;
import com.example.chat.domain.user.repository.UserRepository;
import com.example.chat.global.exception.BusinessException;
import com.example.chat.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MeetupServiceImplTest {

    @Mock private MeetupRepository meetupRepository;
    @Mock private MeetupParticipantRepository meetupParticipantRepository;
    @Mock private MeetupJoinRequestRepository meetupJoinRequestRepository;
    @Mock private MeetupPlanRepository meetupPlanRepository;
    @Mock private MeetupReviewRepository meetupReviewRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private MeetupServiceImpl meetupService;

    private User host;
    private User guest;
    private Meetup meetup;

    @BeforeEach
    void setUp() {
        host = User.create("host@test.com", null, "호스트", null);
        ReflectionTestUtils.setField(host, "id", 1L);

        guest = User.create("guest@test.com", null, "게스트", null);
        ReflectionTestUtils.setField(guest, "id", 2L);

        meetup = Meetup.create(
                host, "같이 저녁 먹을 사람", "설명",
                35.15, 129.12, "부산 수영구",
                MeetupCategory.FOOD, MeetupVisibility.PUBLIC,
                4, null, null, null, TimeMode.FLEXIBLE, null,
                LocalDateTime.now().plusHours(3)
        );
        ReflectionTestUtils.setField(meetup, "id", 1L);
    }

    @Test
    @DisplayName("모임 생성 성공")
    void create_success() {
        MeetupCreateRequest request = new MeetupCreateRequest(
                "같이 저녁 먹을 사람", "설명",
                35.15, 129.12, "부산 수영구",
                MeetupCategory.FOOD, MeetupVisibility.PUBLIC,
                4, null, null, null, TimeMode.FLEXIBLE, null,
                LocalDateTime.now().plusHours(3)
        );

        given(userRepository.findById(1L)).willReturn(Optional.of(host));
        given(meetupRepository.save(any(Meetup.class))).willReturn(meetup);

        MeetupResponse response = meetupService.create(1L, request);

        assertThat(response.title()).isEqualTo("같이 저녁 먹을 사람");
        assertThat(response.currentCount()).isEqualTo(1);
        assertThat(response.status()).isEqualTo(MeetupStatus.OPEN);
        assertThat(response.joinStatus()).isEqualTo(JoinStatus.HOST);
    }

    @Test
    @DisplayName("참가 성공 — PUBLIC 모임은 즉시 JOINED")
    void join_success() {
        given(userRepository.findById(2L)).willReturn(Optional.of(guest));
        given(meetupRepository.findById(1L)).willReturn(Optional.of(meetup));
        given(meetupParticipantRepository.existsByMeetupAndUser(meetup, guest)).willReturn(false);
        given(meetupParticipantRepository.save(any(MeetupParticipant.class)))
                .willReturn(MeetupParticipant.create(meetup, guest));

        JoinResponse response = meetupService.join(2L, 1L);

        assertThat(response.joinStatus()).isEqualTo(JoinStatus.JOINED);
        verify(meetupParticipantRepository).save(any(MeetupParticipant.class));
    }

    @Test
    @DisplayName("참가 실패 — 이미 참가한 모임")
    void join_fail_alreadyJoined() {
        given(userRepository.findById(2L)).willReturn(Optional.of(guest));
        given(meetupRepository.findById(1L)).willReturn(Optional.of(meetup));
        given(meetupParticipantRepository.existsByMeetupAndUser(meetup, guest)).willReturn(true);

        assertThatThrownBy(() -> meetupService.join(2L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.ALREADY_JOINED.getMessage());
    }

    @Test
    @DisplayName("참가 실패 — 정원 초과로 CLOSED된 모임")
    void join_fail_closed() {
        Meetup fullMeetup = Meetup.create(
                host, "꽉 찬 모임", null,
                35.15, 129.12, "부산",
                MeetupCategory.FOOD, MeetupVisibility.PUBLIC,
                2, null, null, null, TimeMode.FLEXIBLE, null,
                LocalDateTime.now().plusHours(3)
        );
        ReflectionTestUtils.setField(fullMeetup, "id", 2L);
        fullMeetup.join(); // currentCount = 2, status = CLOSED

        given(userRepository.findById(2L)).willReturn(Optional.of(guest));
        given(meetupRepository.findById(2L)).willReturn(Optional.of(fullMeetup));

        assertThatThrownBy(() -> meetupService.join(2L, 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.MEETUP_NOT_JOINABLE.getMessage());
    }

    @Test
    @DisplayName("참가 취소 성공 — NOT_JOINED 반환")
    void leave_success() {
        meetup.join();
        MeetupParticipant participant = MeetupParticipant.create(meetup, guest);

        given(userRepository.findById(2L)).willReturn(Optional.of(guest));
        given(meetupRepository.findById(1L)).willReturn(Optional.of(meetup));
        given(meetupParticipantRepository.findByMeetupAndUser(meetup, guest))
                .willReturn(Optional.of(participant));
        given(meetupJoinRequestRepository.findByMeetupAndUser(meetup, guest))
                .willReturn(Optional.empty());

        JoinResponse response = meetupService.leave(2L, 1L);

        assertThat(response.joinStatus()).isEqualTo(JoinStatus.NOT_JOINED);
        verify(meetupParticipantRepository).delete(participant);
    }

    @Test
    @DisplayName("참가 취소 실패 — 호스트는 취소 불가")
    void leave_fail_host() {
        given(userRepository.findById(1L)).willReturn(Optional.of(host));
        given(meetupRepository.findById(1L)).willReturn(Optional.of(meetup));

        assertThatThrownBy(() -> meetupService.leave(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.FORBIDDEN.getMessage());
    }
}
