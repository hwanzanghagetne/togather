package com.example.chat.domain.meetup.domain;

import com.example.chat.domain.user.domain.User;
import com.example.chat.global.common.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "meetup_reviews")
public class MeetupReview extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meetup_id", nullable = false)
    private Meetup meetup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @Column(nullable = false)
    private int rating;

    @ElementCollection
    @CollectionTable(name = "meetup_review_tags", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "tag")
    private List<String> tags;

    @Builder
    private MeetupReview(Meetup meetup, User reviewer, int rating, List<String> tags) {
        this.meetup = meetup;
        this.reviewer = reviewer;
        this.rating = rating;
        this.tags = tags;
    }

    public static MeetupReview create(Meetup meetup, User reviewer, int rating, List<String> tags) {
        return MeetupReview.builder()
                .meetup(meetup)
                .reviewer(reviewer)
                .rating(rating)
                .tags(tags != null ? tags : List.of())
                .build();
    }
}
