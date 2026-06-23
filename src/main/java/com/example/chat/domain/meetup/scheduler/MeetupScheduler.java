package com.example.chat.domain.meetup.scheduler;

import com.example.chat.domain.meetup.domain.Meetup;
import com.example.chat.domain.meetup.domain.MeetupStatus;
import com.example.chat.domain.meetup.repository.MeetupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MeetupScheduler {

    private final MeetupRepository meetupRepository;

    // 매일 자정(00:00)에 실행 — cron = "초 분 시 일 월 요일"
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expireOutdatedMeetups() {
        List<Meetup> expiredMeetups = meetupRepository
                .findByStatusAndExpiresAtBefore(MeetupStatus.OPEN, LocalDateTime.now());

        // 조회된 모임들을 하나씩 EXPIRED로 변경
        // meetup.expire()는 Meetup 엔티티 안에 status = EXPIRED 하는 메서드
        expiredMeetups.forEach(Meetup::expire);

        // @Transactional이 있어서 별도 save() 없이 트랜잭션 끝날 때 자동으로 UPDATE 쿼리 실행
    }
}
