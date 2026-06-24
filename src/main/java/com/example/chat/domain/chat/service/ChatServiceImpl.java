package com.example.chat.domain.chat.service;

import com.example.chat.domain.chat.domain.ChatMessage;
import com.example.chat.domain.chat.domain.ChatRoom;
import com.example.chat.domain.chat.dto.ChatMessageRequest;
import com.example.chat.domain.chat.dto.ChatMessageResponse;
import com.example.chat.domain.chat.repository.ChatMessageRepository;
import com.example.chat.domain.chat.repository.ChatRoomRepository;
import com.example.chat.domain.meetup.domain.Meetup;
import com.example.chat.domain.meetup.repository.MeetupRepository;
import com.example.chat.domain.user.domain.User;
import com.example.chat.domain.user.repository.UserRepository;
import com.example.chat.global.exception.BusinessException;
import com.example.chat.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MeetupRepository meetupRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(Long userId, Long meetupId, ChatMessageRequest request) {
        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 채팅방 없으면 자동 생성
        ChatRoom chatRoom = getOrCreateChatRoom(meetupId);

        // 메시지 DB 저장
        ChatMessage message = chatMessageRepository.save(
                ChatMessage.create(chatRoom, sender, request.content())
        );

        return ChatMessageResponse.from(message);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(Long meetupId) {
        ChatRoom chatRoom = chatRoomRepository.findByMeetupId(meetupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 최근 100개만 가져옴
        return chatMessageRepository
                .findByChatRoomIdOrderByCreatedAtAsc(chatRoom.getId(), PageRequest.of(0, 100))
                .stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    // 있으면 가져오고 없으면 새로 만드는 패턴
    private ChatRoom getOrCreateChatRoom(Long meetupId) {
        return chatRoomRepository.findByMeetupId(meetupId)
                .orElseGet(() -> {
                    Meetup meetup = meetupRepository.findById(meetupId)
                            .orElseThrow(() -> new BusinessException(ErrorCode.MEETUP_NOT_FOUND));
                    return chatRoomRepository.save(ChatRoom.create(meetup));
                });
    }
}
