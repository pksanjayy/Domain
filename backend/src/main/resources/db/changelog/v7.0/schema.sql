

DROP TABLE IF EXISTS test_drive_bookings;
DROP TABLE IF EXISTS test_drive_fleet;

-- Create test_drive_fleet table
CREATE TABLE test_drive_fleet (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    branch_id BIGINT NOT NULL,
    fleet_id VARCHAR(50) NOT NULL UNIQUE,
    vin VARCHAR(50) NOT NULL UNIQUE,
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    variant VARCHAR(100) NOT NULL,
    fuel_type VARCHAR(20) NOT NULL,
    transmission VARCHAR(20) NOT NULL,
    registration_number VARCHAR(50) NOT NULL,
    insurance_expiry DATE,
    rc_expiry DATE,
    current_odometer INT,
    status VARCHAR(20) NOT NULL,
    last_service_date DATE,
    next_service_due DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    version INT DEFAULT 0,
    CONSTRAINT fk_test_drive_fleet_branch FOREIGN KEY (branch_id) REFERENCES branches(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_test_drive_fleet_branch ON test_drive_fleet(branch_id);
CREATE INDEX idx_test_drive_fleet_status ON test_drive_fleet(status);

-- Create test_drive_bookings table
CREATE TABLE test_drive_bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    fleet_id BIGINT NOT NULL,
    booking_id VARCHAR(50) NOT NULL UNIQUE,
    sales_executive_id BIGINT,
    booking_date DATE NOT NULL,
    test_drive_date DATE NOT NULL,
    time_slot TIME NOT NULL,
    license_number VARCHAR(100),
    pickup_required BOOLEAN DEFAULT FALSE,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    version INT DEFAULT 0,
    CONSTRAINT fk_tdb_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_tdb_fleet FOREIGN KEY (fleet_id) REFERENCES test_drive_fleet(id),
    CONSTRAINT fk_tdb_sales_exec FOREIGN KEY (sales_executive_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_tdb_customer ON test_drive_bookings(customer_id);
CREATE INDEX idx_tdb_fleet ON test_drive_bookings(fleet_id);
CREATE INDEX idx_tdb_sales_exec ON test_drive_bookings(sales_executive_id);
CREATE INDEX idx_tdb_status ON test_drive_bookings(status);

-- Insert Menus
SET @test_drive_menu_id = (SELECT IFNULL(MAX(id), 0) + 1 FROM menus);
INSERT INTO menus (id, name, icon, path, display_order, is_active, parent_id, created_by)
VALUES (@test_drive_menu_id, 'Test Drive', 'directions_car', '/testdrive', 4, true, null, 'system');

SET @test_drive_fleet_menu_id = (SELECT IFNULL(MAX(id), 0) + 1 FROM menus);
INSERT INTO menus (id, name, icon, path, display_order, is_active, parent_id, created_by)
VALUES (@test_drive_fleet_menu_id, 'Test Drive Fleet', 'local_taxi', '/testdrive/fleet', 1, true, @test_drive_menu_id, 'system');

SET @test_drive_booking_menu_id = (SELECT IFNULL(MAX(id), 0) + 1 FROM menus);
INSERT INTO menus (id, name, icon, path, display_order, is_active, parent_id, created_by)
VALUES (@test_drive_booking_menu_id, 'Test Drive Bookings', 'event_seat', '/testdrive/bookings', 2, true, @test_drive_menu_id, 'system');

-- Insert Role Menus mapping (SUPER_ADMIN=1, MASTER_USER=2, SALES_CRM_EXEC=5)
INSERT INTO role_menus (role_id, menu_id) VALUES
(1, @test_drive_menu_id), (1, @test_drive_fleet_menu_id), (1, @test_drive_booking_menu_id),
(2, @test_drive_menu_id), (2, @test_drive_fleet_menu_id), (2, @test_drive_booking_menu_id),
(5, @test_drive_menu_id), (5, @test_drive_fleet_menu_id), (5, @test_drive_booking_menu_id);

-- Insert Permission Matrix for Test Drive Module
INSERT INTO permissions (role_id, module_name, can_create, can_read, can_update, can_delete, created_by) VALUES
(1, 'TEST_DRIVE', TRUE, TRUE, TRUE, TRUE, 'system'),
(2, 'TEST_DRIVE', TRUE, TRUE, TRUE, TRUE, 'system'),
(5, 'TEST_DRIVE', TRUE, TRUE, TRUE, FALSE, 'system');
