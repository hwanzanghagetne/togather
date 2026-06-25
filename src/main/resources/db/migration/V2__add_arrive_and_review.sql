-- address를 nullable로 변경 (프론트에서 reverse geocoding 없이 보낼 수 있음)
ALTER TABLE meetups MODIFY address VARCHAR(255) NULL;

-- 참가자 도착 여부
ALTER TABLE meetup_participants ADD COLUMN arrived TINYINT(1) NOT NULL DEFAULT 0;

-- 모임 후기
CREATE TABLE meetup_reviews
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    meetup_id   BIGINT     NOT NULL,
    reviewer_id BIGINT     NOT NULL,
    rating      INT        NOT NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    UNIQUE (meetup_id, reviewer_id),
    FOREIGN KEY (meetup_id) REFERENCES meetups (id),
    FOREIGN KEY (reviewer_id) REFERENCES users (id)
);

CREATE TABLE meetup_review_tags
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    review_id BIGINT       NOT NULL,
    tag       VARCHAR(50)  NOT NULL,
    FOREIGN KEY (review_id) REFERENCES meetup_reviews (id)
);
