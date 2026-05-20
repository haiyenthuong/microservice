-- Add payment-related fields to orders table
ALTER TABLE orders ADD COLUMN payment_status INT DEFAULT 0 COMMENT '0=PENDING, 1=PROCESSING, 2=PAID, 3=FAILED, 4=REFUNDED';
ALTER TABLE orders ADD COLUMN payment_method VARCHAR(50) COMMENT 'Payment method: CREDIT_CARD, BANK_TRANSFER, CASH, PAYPAL, MOMO, VNPAY';
ALTER TABLE orders ADD COLUMN transaction_id VARCHAR(100) COMMENT 'Transaction ID from payment gateway';
ALTER TABLE orders ADD COLUMN payment_date DATETIME COMMENT 'Payment completion date';
ALTER TABLE orders ADD COLUMN payment_failure_reason VARCHAR(500) COMMENT 'Reason for payment failure';
