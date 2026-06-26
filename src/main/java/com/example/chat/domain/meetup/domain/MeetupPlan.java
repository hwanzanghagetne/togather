package com.example.chat.domain.meetup.domain;

import com.example.chat.global.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "meetup_plans")
public class MeetupPlan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meetup_id", nullable = false)
    private Meetup meetup;

    private String placeName;
    private String address;
    private LocalDateTime meetingAt;

    @Builder
    private MeetupPlan(Meetup meetup, String placeName, String address, LocalDateTime meetingAt) {
        this.meetup = meetup;
        this.placeName = placeName;
        this.address = address;
        this.meetingAt = meetingAt;
    }

    public static MeetupPlan create(Meetup meetup, String placeName, String address, LocalDateTime meetingAt) {
        return MeetupPlan.builder()
                .meetup(meetup)
                .placeName(placeName)
                .address(address)
                .meetingAt(meetingAt)
                .build();
    }

    public void update(String placeName, String address, LocalDateTime meetingAt) {
        this.placeName = placeName;
        this.address = address;
        this.meetingAt = meetingAt;
    }
}
