USE empiricus_db;

-- =====================================================
-- 1. RESTAURANTES
-- =====================================================

CREATE TABLE restaurants (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    legal_name VARCHAR(200),
    tax_id VARCHAR(30),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_restaurant_tax_id (tax_id)
) ENGINE=InnoDB;

-- =====================================================
-- 2. USUÁRIOS
-- =====================================================

CREATE TABLE users (
   id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(190) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB;

-- =====================================================
-- 3. VÍNCULO ENTRE USUÁRIOS E RESTAURANTES
-- =====================================================

CREATE TABLE user_restaurants (
    restaurant_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    role ENUM(
        'ADMIN',
        'RECEPTIONIST',
        'WAITER',
        'KITCHEN',
        'STOCK_MANAGER'
    ) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (restaurant_id, user_id),
    CONSTRAINT fk_ur_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id),
    CONSTRAINT fk_ur_user
        FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;