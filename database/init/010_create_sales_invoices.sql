USE empiricus_db;

CREATE TABLE sales_invoices (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    restaurant_id BIGINT UNSIGNED NOT NULL,
    order_id BIGINT UNSIGNED NOT NULL,
    invoice_number VARCHAR(30) NOT NULL,
    series VARCHAR(10) NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    status ENUM('ISSUED', 'CANCELLED') NOT NULL DEFAULT 'ISSUED',
    issued_at DATETIME(3) NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    UNIQUE KEY uk_sales_invoice_number
        (restaurant_id, series, invoice_number),
    KEY idx_sales_invoices_order (restaurant_id, order_id),

    CONSTRAINT chk_sales_invoice_total CHECK (total_amount >= 0),
    CONSTRAINT fk_sales_invoice_order
        FOREIGN KEY (restaurant_id, order_id)
        REFERENCES orders(restaurant_id, id)
) ENGINE=InnoDB;
