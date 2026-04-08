USE dms_db;

-- Insert the missing vehicle model
INSERT IGNORE INTO vehicle_models (brand, model, is_active, vehicle_count) 
VALUES ('Hyundai', 'Tucson lemon', TRUE, 1);

-- Update the vehicle with explicit collation
UPDATE vehicles v
INNER JOIN vehicle_models vm 
    ON v.brand COLLATE utf8mb4_0900_ai_ci = vm.brand 
    AND v.model COLLATE utf8mb4_0900_ai_ci = vm.model
SET v.vehicle_model_id = vm.id
WHERE v.id = 103;

-- Verify
SELECT id, vin, brand, model, vehicle_model_id FROM vehicles WHERE id = 103;
