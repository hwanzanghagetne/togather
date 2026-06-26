-- expiresAt → visibleUntil (의미 명확화: 지도 노출 종료 시각)
ALTER TABLE meetups CHANGE COLUMN expires_at visible_until DATETIME(6) NOT NULL;

-- max_participants optional (정원 상한 없음 지원)
ALTER TABLE meetups MODIFY COLUMN max_participants INT NULL;

-- 신규 필드
ALTER TABLE meetups ADD COLUMN visibility    VARCHAR(20) NOT NULL DEFAULT 'PUBLIC';
ALTER TABLE meetups ADD COLUMN min_age       INT;
ALTER TABLE meetups ADD COLUMN max_age       INT;
ALTER TABLE meetups ADD COLUMN meeting_date  DATE;
ALTER TABLE meetups ADD COLUMN time_mode     VARCHAR(20) NOT NULL DEFAULT 'FLEXIBLE';
ALTER TABLE meetups ADD COLUMN meeting_time  TIME;

-- 비공개 모임 참가 요청
CREATE TABLE meetup_join_requests
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    meetup_id  BIGINT      NOT NULL,
    user_id    BIGINT      NOT NULL,
    status     VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    UNIQUE (meetup_id, user_id),
    FOREIGN KEY (meetup_id) REFERENCES meetups (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

-- 채팅방 장소·일정 확정 카드
CREATE TABLE meetup_plans
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    meetup_id   BIGINT       NOT NULL UNIQUE,
    place_name  VARCHAR(100),
    address     VARCHAR(255),
    meeting_at  DATETIME(6),
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    FOREIGN KEY (meetup_id) REFERENCES meetups (id)
);
