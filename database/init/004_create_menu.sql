USE empiricus_db;

-- =====================================================
-- 1. CATEGORIAS DO CARDÁPIO
-- =====================================================

CREATE TABLE menu_categories (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    restaurant_id BIGINT UNSIGNED NOT NULL,
    name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,

    UNIQUE KEY uk_category_restaurant_id (restaurant_id, id),
    UNIQUE KEY uk_category_name (restaurant_id, name),

    CONSTRAINT fk_category_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id)
) ENGINE=InnoDB;

-- =====================================================
-- 2. ITENS DO CARDÁPIO
-- =====================================================

CREATE TABLE menu_items (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    restaurant_id BIGINT UNSIGNED NOT NULL,
    category_id BIGINT UNSIGNED NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    price DECIMAL(12,2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    UNIQUE KEY uk_menu_item_restaurant_id (restaurant_id, id),
    KEY idx_menu_category (restaurant_id, category_id, active),

    CONSTRAINT chk_menu_price CHECK (price >= 0),
    CONSTRAINT fk_menu_category
        FOREIGN KEY (restaurant_id, category_id)
        REFERENCES menu_categories(restaurant_id, id)
) ENGINE=InnoDB;