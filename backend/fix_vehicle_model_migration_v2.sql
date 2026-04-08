-- Manual fix for vehicle_model migration - Version 2
-- Handles case where columns already exist

USE dms_db;

-- Step 1: Create vehicle_models table if not exists
CREATE TABLE IF NOT EXISTS vehicle_models (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    brand VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    model VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    vehicle_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_vehicle_model_brand_model (brand, model),
    INDEX idx_vehicle_model_brand (brand),
    INDEX idx_vehicle_model_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Step 2: Populate vehicle_models from existing vehicles
INSERT IGNORE INTO vehicle_models (brand, model, is_active, vehicle_count)
SELECT 
    brand COLLATE utf8mb4_0900_ai_ci, 
    model COLLATE utf8mb4_0900_ai_ci, 
    TRUE, 
    COUNT(*) as vehicle_count
FROM vehicles
WHERE status != 'DELETED'
GROUP BY brand, model;

-- Step 3: Update vehicles to link to vehicle_models (handle collation)
UPDATE vehicles v
INNER JOIN vehicle_models vm 
    ON v.brand COLLATE utf8mb4_0900_ai_ci = vm.brand 
    AND v.model COLLATE utf8mb4_0900_ai_ci = vm.model
SET v.vehicle_model_id = vm.id
WHERE v.vehicle_model_id IS NULL;

-- Step 4: Check if there are any NULL values left
SELECT COUNT(*) AS null_count FROM vehicles WHERE vehicle_model_id IS NULL;

-- Step 5: Make vehicle_model_id NOT NULL (only if no NULLs exist)
-- This will fail if there are still NULL values, which is good - it means data needs attention
ALTER TABLE vehicles MODIFY COLUMN vehicle_model_id BIGINT NOT NULL;

-- Step 6: Add FK constraint if not exists
SET @constraint_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
    WHERE CONSTRAINT_SCHEMA = 'dms_db' 
    AND TABLE_NAME = 'vehicles' 
    AND CONSTRAINT_NAME = 'fk_vehicles_vehicle_model'
);

SET @sql = IF(@constraint_exists = 0, 
    'ALTER TABLE vehicles ADD CONSTRAINT fk_vehicles_vehicle_model FOREIGN KEY (vehicle_model_id) REFERENCES vehicle_models(id)', 
    'SELECT "FK constraint fk_vehicles_vehicle_model already exists" AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 7: Add index if not exists
SET @index_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE table_schema = 'dms_db' 
    AND table_name = 'vehicles' 
    AND index_name = 'idx_vehicles_vehicle_model'
);

SET @sql = IF(@index_exists = 0, 
    'CREATE INDEX idx_vehicles_vehicle_model ON vehicles(vehicle_model_id)', 
    'SELECT "Index idx_vehicles_vehicle_model already exists" AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 8: Add vehicle_id to test_drive_fleet if not exists
SET @column_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE table_schema = 'dms_db' 
    AND table_name = 'test_drive_fleet' 
    AND column_name = 'vehicle_id'
);

SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE test_drive_fleet ADD COLUMN vehicle_id BIGINT NULL AFTER branch_id', 
    'SELECT "Column vehicle_id already exists in test_drive_fleet" AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 9: Add FK for test_drive_fleet if not exists
SET @constraint_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
    WHERE CONSTRAINT_SCHEMA = 'dms_db' 
    AND TABLE_NAME = 'test_drive_fleet' 
    AND CONSTRAINT_NAME = 'fk_test_drive_fleet_vehicle'
);

SET @sql = IF(@constraint_exists = 0, 
    'ALTER TABLE test_drive_fleet ADD CONSTRAINT fk_test_drive_fleet_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)', 
    'SELECT "FK constraint fk_test_drive_fleet_vehicle already exists" AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 10: Add index for test_drive_fleet if not exists
SET @index_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE table_schema = 'dms_db' 
    AND table_name = 'test_drive_fleet' 
    AND index_name = 'idx_test_drive_fleet_vehicle'
);

SET @sql = IF(@index_exists = 0, 
    'CREATE INDEX idx_test_drive_fleet_vehicle ON test_drive_fleet(vehicle_id)', 
    'SELECT "Index idx_test_drive_fleet_vehicle already exists" AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 11: Drop brand column from test_drive_fleet if exists
SET @column_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE table_schema = 'dms_db' 
    AND table_name = 'test_drive_fleet' 
    AND column_name = 'brand'
);

SET @sql = IF(@column_exists > 0, 
    'ALTER TABLE test_drive_fleet DROP COLUMN brand', 
    'SELECT "Column brand does not exist in test_drive_fleet" AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 12: Drop model column from test_drive_fleet if exists
SET @column_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE table_schema = 'dms_db' 
    AND table_name = 'test_drive_fleet' 
    AND column_name = 'model'
);

SET @sql = IF(@column_exists > 0, 
    'ALTER TABLE test_drive_fleet DROP COLUMN model', 
    'SELECT "Column model does not exist in test_drive_fleet" AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 13: Mark the Liquibase changeset as executed
DELETE FROM DATABASECHANGELOG WHERE ID = '25-fixed' AND AUTHOR = 'antigravity';

INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, EXECTYPE, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE, CONTEXTS, LABELS, DEPLOYMENT_ID)
SELECT '25-fixed', 'antigravity', 'db/changelog/db.changelog-master.yaml', NOW(), 
    COALESCE(MAX(ORDEREXECUTED), 0) + 1, 
    'EXECUTED', '9:manual', 'sqlFile', 'Add VehicleModel table and relationships v17.0 (fixed)', NULL, '4.29.2', NULL, NULL, 
    CONCAT(UNIX_TIMESTAMP(NOW()), '000')
FROM DATABASECHANGELOG;

SELECT 'Migration completed successfully!' AS status;
