package com.example.chat.domain.meetup.repository;

import com.example.chat.domain.meetup.domain.Meetup;
import com.example.chat.domain.meetup.domain.MeetupStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MeetupRepository extends JpaRepository<Meetup, Long> {

    @Query(value = """
            SELECT * FROM meetups m
            WHERE m.status = 'OPEN'
            AND m.visible_until > NOW()
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

    List<Meetup> findByStatusAndVisibleUntilBefore(MeetupStatus status, LocalDateTime dateTime);

    @Query("""
            SELECT DISTINCT m FROM Meetup m
            LEFT JOIN MeetupParticipant mp ON mp.meetup = m AND mp.user.id = :userId
            WHERE m.host.id = :userId OR mp.user.id = :userId
            ORDER BY m.createdAt DESC
            """)
    List<Meetup> findAllByHostOrParticipant(@Param("userId") Long userId);
}
