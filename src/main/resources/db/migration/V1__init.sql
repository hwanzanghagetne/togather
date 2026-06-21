CREATE TABLE users
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    email             VARCHAR(255) NOT NULL UNIQUE,
    password          VARCHAR(255),
    nickname          VARCHAR(50)  NOT NULL,
    profile_image_url VARCHAR(500),
    home_city         VARCHAR(100),
    role              VARCHAR(20)  NOT NULL,
    created_at        DATETIME(6)  NOT NULL,
    updated_at        DATETIME(6)  NOT NULL
);

CREATE TABLE user_oauth
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id          BIGINT       NOT NULL,
    provider         VARCHAR(20)  NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    created_at       DATETIME(6)  NOT NULL,
    updated_at       DATETIME(6)  NOT NULL,
    UNIQUE (provider, provider_user_id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE meetups
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    host_id          BIGINT       NOT NULL,
    title            VARCHAR(100) NOT NULL,
    description      TEXT,
    latitude         DOUBLE       NOT NULL,
    longitude        DOUBLE       NOT NULL,
    address          VARCHAR(255) NOT NULL,
    category         VARCHAR(20)  NOT NULL,
    max_participants INT          NOT NULL,
    current_count    INT          NOT NULL DEFAULT 1,
    status           VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    expires_at       DATETIME(6)  NOT NULL,
    created_at       DATETIME(6)  NOT NULL,
    updated_at       DATETIME(6)  NOT NULL,
    FOREIGN KEY (host_id) REFERENCES users (id),
    INDEX idx_meetup_location (latitude, longitude),
    INDEX idx_meetup_status_expires (status, expires_at)
);

CREATE TABLE meetup_participants
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    meetup_id  BIGINT      NOT NULL,
    user_id    BIGINT      NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    FOREIGN KEY (meetup_id) REFERENCES meetups (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE chat_rooms
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    meetup_id  BIGINT      NOT NULL UNIQUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    FOREIGN KEY (meetup_id) REFERENCES meetups (id)
);

CREATE TABLE refresh_tokens
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT       NOT NULL UNIQUE,
    token      VARCHAR(500) NOT NULL,
    created_at DATETIME(6)  NOT NULL,
    updated_at DATETIME(6)  NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE chat_messages
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    chat_room_id BIGINT      NOT NULL,
    sender_id    BIGINT,
    content      TEXT        NOT NULL,
    type         VARCHAR(20) NOT NULL,
    created_at   DATETIME(6) NOT NULL,
    updated_at   DATETIME(6) NOT NULL,
    FOREIGN KEY (chat_room_id) REFERENCES chat_rooms (id),
    FOREIGN KEY (sender_id) REFERENCES users (id),
    INDEX idx_chat_message_room_created (chat_room_id, created_at)
);
