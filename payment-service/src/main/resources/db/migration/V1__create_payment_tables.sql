-- Create payments table
CREATE TABLE payments (
    id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    payment_status INT NOT NULL DEFAULT 0 COMMENT '0=PENDING, 1=PROCESSING, 2=PAID, 3=FAILED, 4=REFUNDED',
    payment_method VARCHAR(50) NOT NULL COMMENT 'CREDIT_CARD, BANK_TRANSFER, CASH, PAYPAL, MOMO, VNPAY',
    transaction_id VARCHAR(100),
    payment_date DATETIME,
    failure_reason VARCHAR(500),
    created_date DATETIME NOT NULL,
    updated_date DATETIME NOT NULL,
    INDEX idx_order_id (order_id),
    INDEX idx_user_id (user_id),
    INDEX idx_payment_status (payment_status),
    INDEX idx_transaction_id (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
