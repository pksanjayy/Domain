-- Create vehicle_models table FIRST (before adding FK constraints)
-- Use utf8mb4_0900_ai_ci to match existing vehicles table collation
CREATE TABLE IF NOT EXISTS vehicle_models (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    vehicle_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_vehicle_model_brand_model (brand, model),
    INDEX idx_vehicle_model_brand (brand),
    INDEX idx_vehicle_model_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Populate vehicle_models table from existing vehicles (if any exist)
INSERT IGNORE INTO vehicle_models (brand, model, is_active, vehicle_count)
SELECT brand, model, TRUE, COUNT(*) as vehicle_count
FROM vehicles
WHERE status != 'DELETED'
GROUP BY brand, model;

-- Check and add vehicle_model_id to vehicles table
SET @dbname = DATABASE();
SET @tablename = 'vehicles';
SET @columnname = 'vehicle_model_id';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE (table_name = @tablename)
   AND (table_schema = @dbname)
   AND (column_name = @columnname)) = 0,
  'ALTER TABLE vehicles ADD COLUMN vehicle_model_id BIGINT NULL AFTER vin;',
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Update vehicles to link to vehicle_models
UPDATE vehicles v
INNER JOIN vehicle_models vm ON v.brand = vm.brand AND v.model = vm.model
SET v.vehicle_model_id = vm.id
WHERE v.vehicle_model_id IS NULL;

-- Add FK constraint if not exists
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
   WHERE CONSTRAINT_SCHEMA = DATABASE()
   AND TABLE_NAME = 'vehicles'
   AND CONSTRAINT_NAME = 'fk_vehicles_vehicle_model') = 0,
  'ALTER TABLE vehicles ADD CONSTRAINT fk_vehicles_vehicle_model FOREIGN KEY (vehicle_model_id) REFERENCES vehicle_models(id);',
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Add index if not exists
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
   WHERE table_schema = DATABASE()
   AND table_name = 'vehicles'
   AND index_name = 'idx_vehicles_vehicle_model') = 0,
  'CREATE INDEX idx_vehicles_vehicle_model ON vehicles(vehicle_model_id);',
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Make vehicle_model_id NOT NULL after data migration
ALTER TABLE vehicles
MODIFY COLUMN vehicle_model_id BIGINT NOT NULL;

-- Check and add vehicle_id column to test_drive_fleet table
SET @tablename = 'test_drive_fleet';
SET @columnname = 'vehicle_id';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE (table_name = @tablename)
   AND (table_schema = @dbname)
   AND (column_name = @columnname)) = 0,
  'ALTER TABLE test_drive_fleet ADD COLUMN vehicle_id BIGINT NULL AFTER branch_id;',
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Add FK constraint for test_drive_fleet if not exists
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
   WHERE CONSTRAINT_SCHEMA = DATABASE()
   AND TABLE_NAME = 'test_drive_fleet'
   AND CONSTRAINT_NAME = 'fk_test_drive_fleet_vehicle') = 0,
  'ALTER TABLE test_drive_fleet ADD CONSTRAINT fk_test_drive_fleet_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id);',
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Add index for test_drive_fleet if not exists
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
   WHERE table_schema = DATABASE()
   AND table_name = 'test_drive_fleet'
   AND index_name = 'idx_test_drive_fleet_vehicle') = 0,
  'CREATE INDEX idx_test_drive_fleet_vehicle ON test_drive_fleet(vehicle_id);',
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Drop brand column from test_drive_fleet (only if it exists)
SET @tablename = 'test_drive_fleet';
SET @columnname = 'brand';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE (table_name = @tablename)
   AND (table_schema = @dbname)
   AND (column_name = @columnname)) > 0,
  'ALTER TABLE test_drive_fleet DROP COLUMN brand;',
  'SELECT 1;'
));
PREPARE alterIfExists FROM @preparedStatement;
EXECUTE alterIfExists;
DEALLOCATE PREPARE alterIfExists;

-- Drop model column from test_drive_fleet (only if it exists)
SET @columnname = 'model';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE (table_name = @tablename)
   AND (table_schema = @dbname)
   AND (column_name = @columnname)) > 0,
  'ALTER TABLE test_drive_fleet DROP COLUMN model;',
  'SELECT 1;'
));
PREPARE alterIfExists FROM @preparedStatement;
EXECUTE alterIfExists;
DEALLOCATE PREPARE alterIfExists;

