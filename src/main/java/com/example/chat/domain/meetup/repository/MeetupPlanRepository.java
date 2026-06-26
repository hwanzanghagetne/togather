package com.example.chat.domain.meetup.repository;

import com.example.chat.domain.meetup.domain.MeetupPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MeetupPlanRepository extends JpaRepository<MeetupPlan, Long> {

    Optional<MeetupPlan> findByMeetupId(Long meetupId);
}
