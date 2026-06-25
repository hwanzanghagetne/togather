package com.example.chat.domain.meetup.service;

import com.example.chat.domain.meetup.domain.Meetup;
import com.example.chat.domain.meetup.domain.MeetupCategory;
import com.example.chat.domain.meetup.domain.MeetupParticipant;
import com.example.chat.domain.meetup.domain.MeetupStatus;
import com.example.chat.domain.meetup.dto.MeetupCreateRequest;
import com.example.chat.domain.meetup.dto.MeetupResponse;
import com.example.chat.domain.meetup.repository.MeetupParticipantRepository;
import com.example.chat.domain.meetup.repository.MeetupRepository;
import com.example.chat.domain.user.domain.Role;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MeetupServiceImplTest {

    @Mock
    private MeetupRepository meetupRepository;

    @Mock
    private MeetupParticipantRepository meetupParticipantRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MeetupServiceImpl meetupService;

    private User host;
    private User guest;
    private Meetup meetup;

    @BeforeEach
    void setUp() {
        // 테스트마다 반복 사용할 객체 미리 생성
        host = User.create("host@test.com", null, "호스트", null);
        guest = User.create("guest@test.com", null, "게스트", null);

        meetup = Meetup.create(
                host,
                "같이 저녁 먹을 사람",
                "설명",
                35.15, 129.12,
                "부산 수영구",
                MeetupCategory.FOOD,
                4,
                LocalDateTime.now().plusHours(3)
        );
    }

    @Test
    @DisplayName("모임 생성 성공")
    void create_success() {
        // given — 이 메서드가 호출되면 이 값을 반환해라
        MeetupCreateRequest request = new MeetupCreateRequest(
                "같이 저녁 먹을 사람", "설명",
                35.15, 129.12, "부산 수영구",
                MeetupCategory.FOOD, 4,
                LocalDateTime.now().plusHours(3)
        );

        given(userRepository.findById(1L)).willReturn(Optional.of(host));
        given(meetupRepository.save(any(Meetup.class))).willReturn(meetup);

        // when — 실제 메서드 실행
        MeetupResponse response = meetupService.create(1L, request);

        // then — 결과 검증
        assertThat(response.title()).isEqualTo("같이 저녁 먹을 사람");
        assertThat(response.currentCount()).isEqualTo(1);
        assertThat(response.status()).isEqualTo(MeetupStatus.OPEN);
    }

    @Test
    @DisplayName("참가 성공 — currentCount 증가")
    void join_success() {
        given(userRepository.findById(2L)).willReturn(Optional.of(guest));
        given(meetupRepository.findById(1L)).willReturn(Optional.of(meetup));
        given(meetupParticipantRepository.existsByMeetupAndUser(meetup, guest)).willReturn(false);
        given(meetupParticipantRepository.save(any(MeetupParticipant.class)))
                .willReturn(MeetupParticipant.create(meetup, guest));

        MeetupResponse response = meetupService.join(2L, 1L);

        assertThat(response.currentCount()).isEqualTo(2); // 1 → 2
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
        // 정원 2명짜리 모임에 이미 2명 참가한 상태 만들기
        Meetup fullMeetup = Meetup.create(
                host, "꽉 찬 모임", null,
                35.15, 129.12, "부산",
                MeetupCategory.FOOD, 2,
                LocalDateTime.now().plusHours(3)
        );
        fullMeetup.join(); // currentCount = 2, status = CLOSED
        fullMeetup.join();

        given(userRepository.findById(2L)).willReturn(Optional.of(guest));
        given(meetupRepository.findById(1L)).willReturn(Optional.of(fullMeetup));

        assertThatThrownBy(() -> meetupService.join(2L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.MEETUP_NOT_JOINABLE.getMessage());
    }

    @Test
    @DisplayName("참가 취소 성공 — currentCount 감소")
    void leave_success() {
        meetup.join(); // 미리 참가 상태로 만들기 (currentCount = 2)
        MeetupParticipant participant = MeetupParticipant.create(meetup, guest);

        given(userRepository.findById(2L)).willReturn(Optional.of(guest));
        given(meetupRepository.findById(1L)).willReturn(Optional.of(meetup));
        given(meetupParticipantRepository.findByMeetupAndUser(meetup, guest))
                .willReturn(Optional.of(participant));

        MeetupResponse response = meetupService.leave(2L, 1L);

        assertThat(response.currentCount()).isEqualTo(1); // 2 → 1
        verify(meetupParticipantRepository).delete(participant);
    }

    @Test
    @DisplayName("참가 취소 실패 — 호스트는 취소 불가")
    void leave_fail_host() {
        // id 주입을 먼저 해야 meetup 안의 host.getId()도 1L이 됨
        org.springframework.test.util.ReflectionTestUtils.setField(host, "id", 1L);

        given(userRepository.findById(1L)).willReturn(Optional.of(host));
        given(meetupRepository.findById(1L)).willReturn(Optional.of(meetup));

        assertThatThrownBy(() -> meetupService.leave(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.FORBIDDEN.getMessage());
    }
}
