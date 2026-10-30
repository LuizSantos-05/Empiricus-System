USE empiricus_db;

-- =====================================================
-- 1. ITENS DO ESTOQUE
-- =====================================================

CREATE TABLE inventory_items (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    restaurant_id BIGINT UNSIGNED NOT NULL,
    name VARCHAR(150) NOT NULL,
    unit ENUM('UN', 'KG', 'G', 'L', 'ML')
        NOT NULL DEFAULT 'UN',
    quantity DECIMAL(12,3) NOT NULL DEFAULT 0,
    minimum_quantity DECIMAL(12,3) NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    UNIQUE KEY uk_inventory_restaurant_id (restaurant_id, id),
    UNIQUE KEY uk_inventory_name (restaurant_id, name),

    CONSTRAINT chk_inventory_quantity CHECK (quantity >= 0),
    CONSTRAINT chk_inventory_minimum CHECK (minimum_quantity >= 0),
    CONSTRAINT fk_inventory_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id)
) ENGINE=InnoDB;

-- =====================================================
-- 2. MOVIMENTAÇÕES DO ESTOQUE
-- =====================================================

CREATE TABLE stock_movements (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    restaurant_id BIGINT UNSIGNED NOT NULL,
    inventory_item_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    movement_type ENUM('ENTRY', 'EXIT', 'ADJUSTMENT')
        NOT NULL,
    quantity DECIMAL(12,3) NOT NULL,
    reason VARCHAR(300) NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    KEY idx_stock_movement_history
        (restaurant_id, inventory_item_id, created_at),

    CONSTRAINT chk_stock_movement_quantity CHECK (quantity > 0),
    CONSTRAINT fk_movement_inventory
        FOREIGN KEY (restaurant_id, inventory_item_id)
        REFERENCES inventory_items(restaurant_id, id),
    CONSTRAINT fk_movement_user
        FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;