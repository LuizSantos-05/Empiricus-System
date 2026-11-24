USE empiricus_db;

CREATE TABLE user_sessions (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    created_at DATETIME(3) NOT NULL,
    expires_at DATETIME(3) NOT NULL,
    revoked_at DATETIME(3),
    UNIQUE KEY uk_user_sessions_token_hash (token_hash),
    KEY idx_user_sessions_user_active (user_id, revoked_at, expires_at),
    CONSTRAINT fk_user_sessions_user
        FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;
