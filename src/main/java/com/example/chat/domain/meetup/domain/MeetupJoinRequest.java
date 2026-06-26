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

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "meetup_join_requests")
public class MeetupJoinRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meetup_id", nullable = false)
    private Meetup meetup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JoinRequestStatus status;

    @Builder
    private MeetupJoinRequest(Meetup meetup, User user) {
        this.meetup = meetup;
        this.user = user;
        this.status = JoinRequestStatus.PENDING;
    }

    public static MeetupJoinRequest create(Meetup meetup, User user) {
        return MeetupJoinRequest.builder().meetup(meetup).user(user).build();
    }

    public void approve() {
        this.status = JoinRequestStatus.APPROVED;
    }

    public void reject() {
        this.status = JoinRequestStatus.REJECTED;
    }
}
