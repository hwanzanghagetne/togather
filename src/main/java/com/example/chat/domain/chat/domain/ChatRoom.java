package com.example.chat.domain.chat.domain;

import com.example.chat.domain.meetup.domain.Meetup;
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

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "chat_rooms")
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meetup_id", nullable = false, unique = true)
    private Meetup meetup;

    @Builder
    private ChatRoom(Meetup meetup) {
        this.meetup = meetup;
    }

    public static ChatRoom create(Meetup meetup) {
        return ChatRoom.builder()
                .meetup(meetup)
                .build();
    }
}
