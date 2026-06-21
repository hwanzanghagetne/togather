package com.example.chat.domain.meetup.domain;

import com.example.chat.domain.user.domain.User;
import com.example.chat.global.common.BaseEntity;
import jakarta.persistence.Entity;
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
@Table(name = "meetup_participants")
public class MeetupParticipant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meetup_id", nullable = false)
    private Meetup meetup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    private MeetupParticipant(Meetup meetup, User user) {
        this.meetup = meetup;
        this.user = user;
    }

    public static MeetupParticipant create(Meetup meetup, User user) {
        return MeetupParticipant.builder()
                .meetup(meetup)
                .user(user)
                .build();
    }
}
