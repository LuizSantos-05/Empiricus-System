USE empiricus_db;

-- =====================================================
-- 1. MESAS
-- =====================================================

CREATE TABLE dining_tables (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    restaurant_id BIGINT UNSIGNED NOT NULL,
    table_number VARCHAR(30) NOT NULL,
    capacity SMALLINT UNSIGNED NOT NULL,
    status ENUM('FREE', 'OCCUPIED', 'INACTIVE')
        NOT NULL DEFAULT 'FREE',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    UNIQUE KEY uk_table_restaurant_id (restaurant_id, id),
    UNIQUE KEY uk_table_number (restaurant_id, table_number),

    CONSTRAINT chk_table_capacity CHECK (capacity > 0),
    CONSTRAINT fk_table_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id)
) ENGINE=InnoDB;
-- =====================================================
-- 2. SESSÕES DE ATENDIMENTO
-- =====================================================

CREATE TABLE service_sessions (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    restaurant_id BIGINT UNSIGNED NOT NULL,
    table_id BIGINT UNSIGNED NOT NULL,
    opened_by BIGINT UNSIGNED NOT NULL,
    opened_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    closed_at DATETIME(3),
    status ENUM('OPEN', 'CLOSED', 'CANCELLED')
        NOT NULL DEFAULT 'OPEN',

    UNIQUE KEY uk_session_restaurant_id (restaurant_id, id),
    KEY idx_session_table_status (restaurant_id, table_id, status),

    CONSTRAINT chk_session_dates CHECK (
        closed_at IS NULL OR closed_at >= opened_at
    ),
    CONSTRAINT fk_session_table
        FOREIGN KEY (restaurant_id, table_id)
        REFERENCES dining_tables(restaurant_id, id),
    CONSTRAINT fk_session_user
        FOREIGN KEY (opened_by) REFERENCES users(id)
) ENGINE=InnoDB;

-- =====================================================
-- 3. FILA DE ESPERA
-- =====================================================

CREATE TABLE waitlist_entries (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    restaurant_id BIGINT UNSIGNED NOT NULL,
    customer_name VARCHAR(150) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    whatsapp VARCHAR(30),
    party_size SMALLINT UNSIGNED NOT NULL,
    status ENUM(
        'WAITING',
        'NOTIFIED',
        'SEATED',
        'CANCELLED',
        'NO_SHOW'
    ) NOT NULL DEFAULT 'WAITING',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    notified_at DATETIME(3),
    seated_at DATETIME(3),
    notes VARCHAR(500),

    UNIQUE KEY uk_waitlist_restaurant_id (restaurant_id, id),
    KEY idx_waitlist_status (restaurant_id, status, created_at),

    CONSTRAINT chk_waitlist_party CHECK (party_size > 0),
    CONSTRAINT fk_waitlist_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id)
) ENGINE=InnoDB;

-- =====================================================
-- 4. NOTIFICAÇÕES
-- =====================================================

CREATE TABLE notifications (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    restaurant_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED,
    waitlist_entry_id BIGINT UNSIGNED,
    table_id BIGINT UNSIGNED NULL,
    type ENUM('TABLE_AVAILABLE', 'ORDER_READY', 'SYSTEM')
        NOT NULL,
    message VARCHAR(500) NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    read_at DATETIME(3),

    KEY idx_notifications_user (restaurant_id, user_id, read_at, created_at),

    CONSTRAINT fk_notification_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id),
    CONSTRAINT fk_notification_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_notification_waitlist
        FOREIGN KEY (restaurant_id, waitlist_entry_id)
        REFERENCES waitlist_entries(restaurant_id, id)
) ENGINE=InnoDB;
