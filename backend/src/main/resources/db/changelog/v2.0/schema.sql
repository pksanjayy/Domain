-- =============================================
-- DMS Schema v2.0 — Inventory Management tables
-- =============================================

CREATE TABLE vehicles (
    id                   BIGINT        NOT NULL AUTO_INCREMENT,
    vin                  VARCHAR(17)   NOT NULL UNIQUE,
    brand                VARCHAR(50)   NOT NULL,
    model                VARCHAR(100)  NOT NULL,
    variant              VARCHAR(100)  NOT NULL,
    colour               VARCHAR(50),
    fuel_type            ENUM('PETROL','DIESEL','ELECTRIC','HYBRID') NOT NULL,
    transmission         ENUM('MANUAL','AUTOMATIC') NOT NULL,
    manufactured_date    DATE          NOT NULL,
    msrp                 DECIMAL(12,2) NOT NULL,
    status               ENUM('IN_TRANSIT','GRN_RECEIVED','PDI_PENDING','PDI_DONE','AVAILABLE','HOLD','BOOKED','INVOICED','TRANSFERRED') NOT NULL DEFAULT 'IN_TRANSIT',
    branch_id            BIGINT        NOT NULL,
    age_days             INT           NOT NULL DEFAULT 0,
    created_at           TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by           VARCHAR(100),
    updated_by           VARCHAR(100),
    version              INT           NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_vehicles_branch FOREIGN KEY (branch_id) REFERENCES branches(id),
    INDEX idx_vehicles_vin (vin),
    INDEX idx_vehicles_branch_status (branch_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE grn_records (
    id                   BIGINT       NOT NULL AUTO_INCREMENT,
    grn_number           VARCHAR(20)  NOT NULL UNIQUE,
    vehicle_id           BIGINT       NOT NULL,
    transporter_name     VARCHAR(100),
    dispatch_date        DATE,
    received_date        DATE         NOT NULL,
    condition_on_arrival ENUM('GOOD','DAMAGED','PARTIAL') NOT NULL,
    remarks              TEXT,
    received_by          BIGINT,
    branch_id            BIGINT       NOT NULL,
    created_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by           VARCHAR(100),
    updated_by           VARCHAR(100),
    PRIMARY KEY (id),
    CONSTRAINT fk_grn_vehicle  FOREIGN KEY (vehicle_id)  REFERENCES vehicles(id),
    CONSTRAINT fk_grn_user     FOREIGN KEY (received_by)  REFERENCES users(id),
    CONSTRAINT fk_grn_branch   FOREIGN KEY (branch_id)    REFERENCES branches(id),
    INDEX idx_grn_vehicle (vehicle_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE pdi_checklists (
    id              BIGINT NOT NULL AUTO_INCREMENT,
    vehicle_id      BIGINT NOT NULL UNIQUE,
    overall_status  ENUM('PENDING','PASSED','FAILED') NOT NULL DEFAULT 'PENDING',
    completed_by    BIGINT,
    completed_at    TIMESTAMP NULL,
    remarks         TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    PRIMARY KEY (id),
    CONSTRAINT fk_pdi_vehicle FOREIGN KEY (vehicle_id)   REFERENCES vehicles(id),
    CONSTRAINT fk_pdi_user    FOREIGN KEY (completed_by)  REFERENCES users(id),
    INDEX idx_pdi_vehicle (vehicle_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE pdi_checklist_items (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    checklist_id  BIGINT       NOT NULL,
    point_name    VARCHAR(100) NOT NULL,
    result        ENUM('PASS','FAIL','NA') NOT NULL DEFAULT 'NA',
    photo_url     VARCHAR(500),
    remark        VARCHAR(255),
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by    VARCHAR(100),
    updated_by    VARCHAR(100),
    PRIMARY KEY (id),
    CONSTRAINT fk_pdi_item_checklist FOREIGN KEY (checklist_id) REFERENCES pdi_checklists(id),
    INDEX idx_pdi_item_checklist (checklist_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE stock_transfers (
    id              BIGINT NOT NULL AUTO_INCREMENT,
    vehicle_id      BIGINT NOT NULL,
    from_branch_id  BIGINT NOT NULL,
    to_branch_id    BIGINT NOT NULL,
    requested_by    BIGINT NOT NULL,
    approved_by     BIGINT,
    status          ENUM('PENDING','APPROVED','REJECTED','COMPLETED') NOT NULL DEFAULT 'PENDING',
    request_date    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approval_date   TIMESTAMP NULL,
    remarks         TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    PRIMARY KEY (id),
    CONSTRAINT fk_transfer_vehicle     FOREIGN KEY (vehicle_id)     REFERENCES vehicles(id),
    CONSTRAINT fk_transfer_from_branch FOREIGN KEY (from_branch_id) REFERENCES branches(id),
    CONSTRAINT fk_transfer_to_branch   FOREIGN KEY (to_branch_id)   REFERENCES branches(id),
    CONSTRAINT fk_transfer_requested   FOREIGN KEY (requested_by)   REFERENCES users(id),
    CONSTRAINT fk_transfer_approved    FOREIGN KEY (approved_by)    REFERENCES users(id),
    INDEX idx_transfer_vehicle (vehicle_id),
    INDEX idx_transfer_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE vehicle_accessories (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    vehicle_id  BIGINT       NOT NULL,
    name        VARCHAR(100) NOT NULL,
    cost        DECIMAL(10,2) NOT NULL,
    fitted_at   TIMESTAMP    NULL,
    fitted_by   BIGINT,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    PRIMARY KEY (id),
    CONSTRAINT fk_accessory_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id),
    CONSTRAINT fk_accessory_user    FOREIGN KEY (fitted_by)  REFERENCES users(id),
    INDEX idx_accessory_vehicle (vehicle_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
