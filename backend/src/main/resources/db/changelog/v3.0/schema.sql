-- =============================================
-- DMS Schema v3.0 — Notifications + Sales tables
-- =============================================

-- ── Notifications ──

CREATE TABLE notifications (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    title         VARCHAR(200)  NOT NULL,
    message       TEXT          NOT NULL,
    module        VARCHAR(50),
    priority      ENUM('LOW','MEDIUM','HIGH','CRITICAL') NOT NULL DEFAULT 'LOW',
    recipient_id  BIGINT        NOT NULL,
    is_read       BOOLEAN       NOT NULL DEFAULT FALSE,
    deep_link     VARCHAR(300),
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by    VARCHAR(100),
    updated_by    VARCHAR(100),
    PRIMARY KEY (id),
    CONSTRAINT fk_notification_recipient FOREIGN KEY (recipient_id) REFERENCES users(id),
    INDEX idx_notification_recipient_read (recipient_id, is_read),
    INDEX idx_notification_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Sales — Customers ──

CREATE TABLE customers (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    mobile      VARCHAR(10)  NOT NULL UNIQUE,
    email       VARCHAR(150),
    dob         DATE,
    occupation  VARCHAR(100),
    branch_id   BIGINT       NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    PRIMARY KEY (id),
    CONSTRAINT fk_customer_branch FOREIGN KEY (branch_id) REFERENCES branches(id),
    INDEX idx_customer_mobile (mobile),
    INDEX idx_customer_branch (branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Sales — Leads ──

CREATE TABLE leads (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    customer_id      BIGINT       NOT NULL,
    assigned_to      BIGINT       NOT NULL,
    model_interested VARCHAR(100),
    source           ENUM('WALK_IN','PHONE','REFERRAL','ONLINE','CAMP') NOT NULL,
    stage            ENUM('NEW_LEAD','TEST_DRIVE','QUOTATION','BOOKING','DELIVERY_READY','DELIVERED','LOST') NOT NULL DEFAULT 'NEW_LEAD',
    lost_reason      VARCHAR(255),
    vehicle_id       BIGINT,
    branch_id        BIGINT       NOT NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100),
    PRIMARY KEY (id),
    CONSTRAINT fk_lead_customer   FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_lead_assigned   FOREIGN KEY (assigned_to) REFERENCES users(id),
    CONSTRAINT fk_lead_vehicle    FOREIGN KEY (vehicle_id)  REFERENCES vehicles(id),
    CONSTRAINT fk_lead_branch     FOREIGN KEY (branch_id)   REFERENCES branches(id),
    INDEX idx_lead_customer (customer_id),
    INDEX idx_lead_assigned (assigned_to),
    INDEX idx_lead_stage (stage),
    INDEX idx_lead_branch (branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Sales — Quotations ──

CREATE TABLE quotations (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    lead_id          BIGINT        NOT NULL UNIQUE,
    list_price       DECIMAL(12,2) NOT NULL,
    discount_amount  DECIMAL(10,2) NOT NULL DEFAULT 0,
    discount_percent DECIMAL(5,2)  NOT NULL DEFAULT 0,
    final_price      DECIMAL(12,2) NOT NULL,
    status           ENUM('DRAFT','PENDING_APPROVAL','APPROVED','REJECTED','EXPIRED') NOT NULL DEFAULT 'DRAFT',
    valid_until      DATE,
    approved_by      BIGINT,
    approval_remarks VARCHAR(255),
    created_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100),
    PRIMARY KEY (id),
    CONSTRAINT fk_quotation_lead     FOREIGN KEY (lead_id)     REFERENCES leads(id),
    CONSTRAINT fk_quotation_approved FOREIGN KEY (approved_by) REFERENCES users(id),
    INDEX idx_quotation_lead (lead_id),
    INDEX idx_quotation_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Sales — Bookings ──

CREATE TABLE bookings (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    lead_id           BIGINT        NOT NULL UNIQUE,
    vehicle_id        BIGINT        NOT NULL,
    token_amount      DECIMAL(10,2) NOT NULL,
    booking_date      DATE          NOT NULL,
    expected_delivery DATE,
    status            ENUM('ACTIVE','CANCELLED') NOT NULL DEFAULT 'ACTIVE',
    created_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by        VARCHAR(100),
    updated_by        VARCHAR(100),
    PRIMARY KEY (id),
    CONSTRAINT fk_booking_lead    FOREIGN KEY (lead_id)    REFERENCES leads(id),
    CONSTRAINT fk_booking_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id),
    INDEX idx_booking_lead (lead_id),
    INDEX idx_booking_vehicle (vehicle_id),
    INDEX idx_booking_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
