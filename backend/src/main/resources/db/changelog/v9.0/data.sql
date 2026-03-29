-- =============================================
-- DMS Seed Data v9.0 - Test Drive Additional Data
-- =============================================

-- Insert additional Test Drive Fleet Vehicles
INSERT INTO test_drive_fleet (id, branch_id, fleet_id, vin, brand, model, variant, fuel_type, transmission, registration_number, insurance_expiry, rc_expiry, current_odometer, status, last_service_date, next_service_due, created_by, version) VALUES
(8, 1, 'TD-EXT-01', 'MH12TD0000000008', 'Hyundai', 'Exter', 'SX', 'PETROL', 'MANUAL', 'MH12TD0008', '2025-10-10', '2039-10-10', 500, 'AVAILABLE', '2024-03-01', '2024-09-01', 'system', 0),
(9, 1, 'TD-AURA-01', 'MH12TD0000000009', 'Hyundai', 'Aura', 'S', 'CNG', 'MANUAL', 'MH12TD0009', '2025-05-15', '2039-05-15', 1200, 'AVAILABLE', '2024-02-15', '2024-08-15', 'system', 0);

-- Insert additional Test Drive Bookings
INSERT INTO test_drive_bookings (id, customer_id, fleet_id, booking_id, sales_executive_id, booking_date, test_drive_date, time_slot, pickup_required, status, created_by, version) VALUES
(7, 2, 8, 'TDB-2024-007', 1, '2024-03-29', '2024-04-10', '10:30:00', 0, 'SCHEDULED', 'system', 0),
(8, 3, 9, 'TDB-2024-008', 1, '2024-03-29', '2024-04-12', '14:30:00', 1, 'SCHEDULED', 'system', 0);
