CREATE TABLE notifications (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    type         VARCHAR(30)  NOT NULL,
    meetup_id    BIGINT       NOT NULL,
    actor_nickname VARCHAR(50) NOT NULL,
    is_read      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   DATETIME(6)  NOT NULL,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_notification_meetup FOREIGN KEY (meetup_id) REFERENCES meetups (id)
);
