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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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

    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeetupCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeetupVisibility visibility;

    private Integer maxParticipants;

    @Column(nullable = false)
    private int currentCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeetupStatus status;

    private Integer minAge;
    private Integer maxAge;

    private LocalDate meetingDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TimeMode timeMode;

    private LocalTime meetingTime;

    @Column(nullable = false)
    private LocalDateTime visibleUntil;

    @Builder
    private Meetup(User host, String title, String description, Double latitude, Double longitude,
                   String address, MeetupCategory category, MeetupVisibility visibility,
                   Integer maxParticipants, Integer minAge, Integer maxAge,
                   LocalDate meetingDate, TimeMode timeMode, LocalTime meetingTime,
                   LocalDateTime visibleUntil) {
        this.host = host;
        this.title = title;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.category = category;
        this.visibility = visibility != null ? visibility : MeetupVisibility.PUBLIC;
        this.maxParticipants = maxParticipants;
        this.currentCount = 1;
        this.status = MeetupStatus.OPEN;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.meetingDate = meetingDate;
        this.timeMode = timeMode != null ? timeMode : TimeMode.FLEXIBLE;
        this.meetingTime = meetingTime;
        this.visibleUntil = visibleUntil;
    }

    public static Meetup create(User host, String title, String description,
                                Double latitude, Double longitude, String address,
                                MeetupCategory category, MeetupVisibility visibility,
                                Integer maxParticipants, Integer minAge, Integer maxAge,
                                LocalDate meetingDate, TimeMode timeMode, LocalTime meetingTime,
                                LocalDateTime visibleUntil) {
        return Meetup.builder()
                .host(host)
                .title(title)
                .description(description)
                .latitude(latitude)
                .longitude(longitude)
                .address(address)
                .category(category)
                .visibility(visibility)
                .maxParticipants(maxParticipants)
                .minAge(minAge)
                .maxAge(maxAge)
                .meetingDate(meetingDate)
                .timeMode(timeMode)
                .meetingTime(meetingTime)
                .visibleUntil(visibleUntil)
                .build();
    }

    public void join() {
        this.currentCount++;
        if (this.maxParticipants != null && this.currentCount >= this.maxParticipants) {
            this.status = MeetupStatus.CLOSED;
        }
    }

    public void leave() {
        if (this.currentCount > 0) {
            this.currentCount--;
        }
        if (this.status == MeetupStatus.CLOSED) {
            this.status = MeetupStatus.OPEN;
        }
    }

    public boolean isFull() {
        return this.maxParticipants != null && this.currentCount >= this.maxParticipants;
    }

    public boolean isJoinable() {
        return this.status == MeetupStatus.OPEN && !isFull();
    }

    public boolean isHost(Long userId) {
        return this.host.getId().equals(userId);
    }

    public void transferHost(User newHost) {
        this.host = newHost;
        this.currentCount--;
        if (this.status == MeetupStatus.CLOSED) {
            this.status = MeetupStatus.OPEN;
        }
    }

    public void closeByHost() {
        this.currentCount = 0;
        this.status = MeetupStatus.CLOSED;
    }

    public void expire() {
        this.status = MeetupStatus.EXPIRED;
    }
}
