-- =============================================
-- DMS Schema v4.0 - Service Module
-- =============================================

CREATE TABLE service_bookings (
    id                     BIGINT       NOT NULL AUTO_INCREMENT,
    branch_id              BIGINT       NOT NULL,
    customer_id            BIGINT       NOT NULL,
    vehicle_id             BIGINT       NOT NULL,
    booking_id             VARCHAR(50)  NOT NULL UNIQUE,
    booking_date           DATE         NOT NULL,
    preferred_service_date DATE,
    service_type           ENUM('FREE', 'PAID', 'REPAIR', 'WARRANTY') NOT NULL,
    complaints             TEXT,
    status                 ENUM('CONFIRMED', 'CANCELLED', 'RESCHEDULED', 'COMPLETED', 'NO_SHOW') NOT NULL DEFAULT 'CONFIRMED',
    created_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by             VARCHAR(100),
    updated_by             VARCHAR(100),
    version                INT          DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_sb_branch FOREIGN KEY (branch_id) REFERENCES branches(id),
    CONSTRAINT fk_sb_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_sb_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id),
    INDEX idx_sb_branch (branch_id),
    INDEX idx_sb_customer (customer_id),
    INDEX idx_sb_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE service_records (
    id                 BIGINT        NOT NULL AUTO_INCREMENT,
    branch_id          BIGINT        NOT NULL,
    service_booking_id BIGINT        NOT NULL UNIQUE,
    service_date       DATE          NOT NULL,
    odometer           DECIMAL(10,2),
    work_performed     TEXT,
    parts_used         TEXT,
    no_of_technicians  INT,
    technician_hours   DECIMAL(6,2),
    notes              TEXT,
    status             ENUM('IN_PROGRESS', 'COMPLETED', 'WAITING_FOR_PARTS') NOT NULL DEFAULT 'IN_PROGRESS',
    payment_status     ENUM('PAID', 'UNPAID', 'PARTIAL') NOT NULL DEFAULT 'UNPAID',
    created_at         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by         VARCHAR(100),
    updated_by         VARCHAR(100),
    version            INT           DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_sr_branch FOREIGN KEY (branch_id) REFERENCES branches(id),
    CONSTRAINT fk_sr_booking FOREIGN KEY (service_booking_id) REFERENCES service_bookings(id),
    INDEX idx_sr_branch (branch_id),
    INDEX idx_sr_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
