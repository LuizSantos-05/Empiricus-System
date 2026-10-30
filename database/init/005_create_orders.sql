USE empiricus_db;

-- =====================================================
-- 1. PEDIDOS
-- =====================================================

CREATE TABLE orders (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    restaurant_id BIGINT UNSIGNED NOT NULL,
    session_id BIGINT UNSIGNED NOT NULL,
    created_by BIGINT UNSIGNED NOT NULL,
    status ENUM(
        'RECEIVED',
        'PREPARING',
        'READY',
        'DELIVERED',
        'CANCELLED'
    ) NOT NULL DEFAULT 'RECEIVED',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    ready_at DATETIME(3),
    delivered_at DATETIME(3),
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    notes VARCHAR(500),

    UNIQUE KEY uk_order_restaurant_id (restaurant_id, id),
    KEY idx_orders_kitchen (restaurant_id, status, created_at),
    KEY idx_orders_session (restaurant_id, session_id, created_at),

    CONSTRAINT chk_order_total CHECK (total_amount >= 0),
    CONSTRAINT fk_order_session
        FOREIGN KEY (restaurant_id, session_id)
        REFERENCES service_sessions(restaurant_id, id),
    CONSTRAINT fk_order_user
        FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB;

-- =====================================================
-- 2. ITENS DOS PEDIDOS
-- =====================================================

CREATE TABLE order_items (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    restaurant_id BIGINT UNSIGNED NOT NULL,
    order_id BIGINT UNSIGNED NOT NULL,
    menu_item_id BIGINT UNSIGNED NOT NULL,
    item_name VARCHAR(150) NOT NULL,
    quantity INT UNSIGNED NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    notes VARCHAR(300),

    KEY idx_order_items_order (restaurant_id, order_id),

    CONSTRAINT chk_order_item_quantity CHECK (quantity > 0),
    CONSTRAINT chk_order_item_price CHECK (unit_price >= 0),
    CONSTRAINT fk_item_order
        FOREIGN KEY (restaurant_id, order_id)
        REFERENCES orders(restaurant_id, id),
    CONSTRAINT fk_item_menu
        FOREIGN KEY (restaurant_id, menu_item_id)
        REFERENCES menu_items(restaurant_id, id)
) ENGINE=InnoDB;