package com.example.chat.domain.meetup.repository;

import com.example.chat.domain.meetup.domain.JoinRequestStatus;
import com.example.chat.domain.meetup.domain.Meetup;
import com.example.chat.domain.meetup.domain.MeetupJoinRequest;
import com.example.chat.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeetupJoinRequestRepository extends JpaRepository<MeetupJoinRequest, Long> {

    Optional<MeetupJoinRequest> findByMeetupAndUser(Meetup meetup, User user);

    boolean existsByMeetupAndUserAndStatus(Meetup meetup, User user, JoinRequestStatus status);

    List<MeetupJoinRequest> findByMeetupAndStatus(Meetup meetup, JoinRequestStatus status);
}
