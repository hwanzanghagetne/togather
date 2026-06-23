package com.example.chat.domain.meetup.domain;

import com.example.chat.domain.user.domain.User;
import com.example.chat.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "meetups")
public class Meetup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeetupCategory category;

    @Column(nullable = false)
    private int maxParticipants;

    @Column(nullable = false)
    private int currentCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeetupStatus status;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Builder
    private Meetup(User host, String title, String description, Double latitude, Double longitude,
                   String address, MeetupCategory category, int maxParticipants, LocalDateTime expiresAt) {
        this.host = host;
        this.title = title;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.category = category;
        this.maxParticipants = maxParticipants;
        this.currentCount = 1;
        this.status = MeetupStatus.OPEN;
        this.expiresAt = expiresAt;
    }

    public static Meetup create(User host, String title, String description, Double latitude, Double longitude,
                                String address, MeetupCategory category, int maxParticipants, LocalDateTime expiresAt) {
        return Meetup.builder()
                .host(host)
                .title(title)
                .description(description)
                .latitude(latitude)
                .longitude(longitude)
                .address(address)
                .category(category)
                .maxParticipants(maxParticipants)
                .expiresAt(expiresAt)
                .build();
    }

    // 참가: 인원 증가 + 정원 다 차면 자동 CLOSED
    public void join() {
        this.currentCount++;
        if (this.currentCount >= this.maxParticipants) {
            this.status = MeetupStatus.CLOSED;
        }
    }

    // 취소: 인원 감소 + CLOSED였으면 다시 OPEN
    public void leave() {
        this.currentCount--;
        if (this.status == MeetupStatus.CLOSED) {
            this.status = MeetupStatus.OPEN;
        }
    }

    public boolean isFull() {
        return this.currentCount >= this.maxParticipants;
    }

    public boolean isJoinable() {
        return this.status == MeetupStatus.OPEN && !isFull();
    }

    public void expire() {
        this.status = MeetupStatus.EXPIRED;
    }
}
