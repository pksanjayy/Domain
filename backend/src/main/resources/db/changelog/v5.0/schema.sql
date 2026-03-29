-- =============================================
-- DMS Schema v5.0 — Replace Quotations with Payments
-- =============================================

-- Drop the quotations table entirely
DROP TABLE IF EXISTS quotations;

-- Create payments table
CREATE TABLE payments (
    payment_id      BIGINT        NOT NULL AUTO_INCREMENT,
    customer_id     BIGINT        NOT NULL,
    payment_date    DATE          NOT NULL,
    amount_paid     DECIMAL(12,2) NOT NULL,
    payment_method  ENUM('CASH','CREDIT_CARD','UPI','BANK_TRANSFER') NOT NULL,
    transaction_id  VARCHAR(100),
    payment_status  ENUM('PENDING','PARTIAL','PAID') NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    PRIMARY KEY (payment_id),
    CONSTRAINT fk_payment_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    INDEX idx_payment_customer (customer_id),
    INDEX idx_payment_status (payment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
