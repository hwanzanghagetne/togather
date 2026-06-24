package com.example.chat.domain.chat.repository;

import com.example.chat.domain.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByMeetupId(Long meetupId);
}
