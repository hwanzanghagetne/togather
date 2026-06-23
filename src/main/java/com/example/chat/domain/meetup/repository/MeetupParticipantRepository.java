package com.example.chat.domain.meetup.repository;

import com.example.chat.domain.meetup.domain.Meetup;
import com.example.chat.domain.meetup.domain.MeetupParticipant;
import com.example.chat.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MeetupParticipantRepository extends JpaRepository<MeetupParticipant, Long> {

    boolean existsByMeetupAndUser(Meetup meetup, User user);

    Optional<MeetupParticipant> findByMeetupAndUser(Meetup meetup, User user);
}
