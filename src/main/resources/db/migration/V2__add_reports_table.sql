-- Table des rapports générés
CREATE TABLE reports (
    id           BIGSERIAL    PRIMARY KEY,
    session_id   BIGINT       NOT NULL REFERENCES interview_sessions(id) ON DELETE CASCADE,
    user_id      BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    file_path    VARCHAR(500) NOT NULL,
    email_sent   BOOLEAN      NOT NULL DEFAULT FALSE,
    sent_at      TIMESTAMP,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);