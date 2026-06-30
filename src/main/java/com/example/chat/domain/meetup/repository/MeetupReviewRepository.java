package com.example.chat.domain.meetup.repository;

import com.example.chat.domain.meetup.domain.MeetupReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetupReviewRepository extends JpaRepository<MeetupReview, Long> {

    boolean existsByMeetupIdAndReviewerId(Long meetupId, Long reviewerId);

    long countByReviewerId(Long reviewerId);
}
