-- Create Orders table
CREATE TABLE IF NOT EXISTS orders (
    id VARCHAR(36) PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    user_id VARCHAR(36) NOT NULL,
    customer_name VARCHAR(200),
    customer_email VARCHAR(100),
    customer_phone VARCHAR(20),
    status INT NOT NULL DEFAULT 0 COMMENT '0: Pending, 1: Confirmed, 2: Processing, 3: Shipped, 4: Delivered, 5: Cancelled, 6: Refunded',
    total_amount DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(19,2) DEFAULT 0.00,
    tax_amount DECIMAL(19,2) DEFAULT 0.00,
    shipping_amount DECIMAL(19,2) DEFAULT 0.00,
    final_amount DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'USD',
    shipping_address VARCHAR(500),
    billing_address VARCHAR(500),
    customer_notes VARCHAR(1000),
    admin_notes VARCHAR(1000),
    order_date DATETIME NOT NULL,
    confirmed_date DATETIME,
    shipped_date DATETIME,
    delivered_date DATETIME,
    cancelled_date DATETIME,
    created_date DATETIME NOT NULL,
    updated_date DATETIME NOT NULL,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_order_date (order_date),
    INDEX idx_order_number (order_number),
    INDEX idx_customer_email (customer_email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Order Items table
CREATE TABLE IF NOT EXISTS order_items (
    id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    product_sku VARCHAR(100),
    product_image VARCHAR(500),
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(19,2) DEFAULT 0.00,
    tax_amount DECIMAL(19,2) DEFAULT 0.00,
    total_price DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'USD',
    created_date DATETIME NOT NULL,
    updated_date DATETIME NOT NULL,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id),
    INDEX idx_product_sku (product_sku)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample data for testing (optional)
-- Note: These are just sample orders. In production, this should be empty or populated by seed scripts.

-- INSERT INTO orders (id, order_number, user_id, customer_name, customer_email, customer_phone, status, total_amount, final_amount, currency, shipping_address, order_date, created_date, updated_date)
-- VALUES
-- ('550e8400-e29b-41d4-a716-446655440001', 'ORD-20250119120000-ABC12345', 'user-001', 'John Doe', 'john@example.com', '1234567890', 0, 100.00, 110.00, 'USD', '123 Main St, City, Country', NOW(), NOW(), NOW());
