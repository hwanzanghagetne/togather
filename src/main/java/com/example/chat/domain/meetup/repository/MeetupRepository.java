package com.example.chat.domain.meetup.repository;

import com.example.chat.domain.meetup.domain.Meetup;
import com.example.chat.domain.meetup.domain.MeetupStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MeetupRepository extends JpaRepository<Meetup, Long> {

    // Haversine 공식으로 반경 내 OPEN 상태 + 아직 만료 안 된 모임 검색
    @Query(value = """
            SELECT * FROM meetups m
            WHERE m.status = 'OPEN'
            AND m.expires_at > NOW()
            AND (
                6371 * acos(
                    cos(radians(:lat)) * cos(radians(m.latitude))
                    * cos(radians(m.longitude) - radians(:lng))
                    + sin(radians(:lat)) * sin(radians(m.latitude))
                )
            ) < :radius
            ORDER BY m.created_at DESC
            """, nativeQuery = true)
    List<Meetup> findNearby(@Param("lat") double lat,
                            @Param("lng") double lng,
                            @Param("radius") double radius);

    // 자정 만료 배치용: OPEN 상태이면서 만료 시간이 지난 모임 조회
    List<Meetup> findByStatusAndExpiresAtBefore(MeetupStatus status, LocalDateTime dateTime);
}
