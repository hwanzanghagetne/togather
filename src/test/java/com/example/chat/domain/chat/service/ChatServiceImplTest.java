package com.example.chat.domain.chat.service;

import com.example.chat.domain.chat.domain.ChatMessage;
import com.example.chat.domain.chat.domain.ChatRoom;
import com.example.chat.domain.chat.dto.ChatMessageRequest;
import com.example.chat.domain.chat.dto.ChatMessageResponse;
import com.example.chat.domain.chat.repository.ChatMessageRepository;
import com.example.chat.domain.chat.repository.ChatRoomRepository;
import com.example.chat.domain.meetup.domain.Meetup;
import com.example.chat.domain.meetup.domain.MeetupCategory;
import com.example.chat.domain.meetup.domain.MeetupVisibility;
import com.example.chat.domain.meetup.domain.TimeMode;
import com.example.chat.domain.meetup.repository.MeetupRepository;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock private ChatRoomRepository chatRoomRepository;
    @Mock private ChatMessageRepository chatMessageRepository;
    @Mock private MeetupRepository meetupRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ChatServiceImpl chatService;

    private User sender;
    private Meetup meetup;
    private ChatRoom chatRoom;
    private ChatMessage chatMessage;

    @BeforeEach
    void setUp() {
        sender = User.create("test@test.com", null, "이승환", null);
        ReflectionTestUtils.setField(sender, "id", 1L);

        meetup = Meetup.create(
                sender, "테스트 모임", "설명",
                35.1548, 129.1259, "부산 수영구",
                MeetupCategory.FOOD, MeetupVisibility.PUBLIC,
                4, null, null, null, TimeMode.FLEXIBLE, null,
                LocalDateTime.now().plusDays(1)
        );
        ReflectionTestUtils.setField(meetup, "id", 1L);

        chatRoom = ChatRoom.create(meetup);
        ReflectionTestUtils.setField(chatRoom, "id", 1L);

        chatMessage = ChatMessage.create(chatRoom, sender, "안녕");
        ReflectionTestUtils.setField(chatMessage, "id", 1L);
    }

    @Test
    @DisplayName("메시지 전송 성공 - 기존 채팅방 있을 때")
    void sendMessage_success_existingChatRoom() {
        given(userRepository.findById(1L)).willReturn(Optional.of(sender));
        given(chatRoomRepository.findByMeetupId(1L)).willReturn(Optional.of(chatRoom));
        given(chatMessageRepository.save(any())).willReturn(chatMessage);

        ChatMessageResponse response = chatService.sendMessage(1L, 1L, new ChatMessageRequest("안녕"));

        assertThat(response.content()).isEqualTo("안녕");
        assertThat(response.senderNickname()).isEqualTo("이승환");
        verify(meetupRepository, never()).findById(any());
    }

    @Test
    @DisplayName("메시지 전송 성공 - 채팅방 없으면 자동 생성")
    void sendMessage_success_createsChatRoom() {
        given(userRepository.findById(1L)).willReturn(Optional.of(sender));
        given(chatRoomRepository.findByMeetupId(1L)).willReturn(Optional.empty());
        given(meetupRepository.findById(1L)).willReturn(Optional.of(meetup));
        given(chatRoomRepository.save(any())).willReturn(chatRoom);
        given(chatMessageRepository.save(any())).willReturn(chatMessage);

        ChatMessageResponse response = chatService.sendMessage(1L, 1L, new ChatMessageRequest("안녕"));

        assertThat(response.content()).isEqualTo("안녕");
        verify(chatRoomRepository).save(any());
    }

    @Test
    @DisplayName("메시지 전송 실패 - 존재하지 않는 유저")
    void sendMessage_fail_userNotFound() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.sendMessage(999L, 1L, new ChatMessageRequest("안녕")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("메시지 전송 실패 - 채팅방 없고 모임도 없을 때")
    void sendMessage_fail_meetupNotFound() {
        given(userRepository.findById(1L)).willReturn(Optional.of(sender));
        given(chatRoomRepository.findByMeetupId(999L)).willReturn(Optional.empty());
        given(meetupRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.sendMessage(1L, 999L, new ChatMessageRequest("안녕")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.MEETUP_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("메시지 이력 조회 성공")
    void getMessages_success() {
        given(chatRoomRepository.findByMeetupId(1L)).willReturn(Optional.of(chatRoom));
        given(chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(any(), any(PageRequest.class)))
                .willReturn(List.of(chatMessage));

        List<ChatMessageResponse> responses = chatService.getMessages(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).content()).isEqualTo("안녕");
    }

    @Test
    @DisplayName("메시지 이력 조회 실패 - 채팅방 없을 때")
    void getMessages_fail_chatRoomNotFound() {
        given(chatRoomRepository.findByMeetupId(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.getMessages(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.NOT_FOUND.getMessage());
    }
}
