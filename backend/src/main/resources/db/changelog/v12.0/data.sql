-- =============================================
-- DMS Data Seed v12.0
-- Backfill total_amount and amount_paid for Bookings
-- Insert dummy bookings across 2024, 2025, and 2026
-- =============================================

-- Backfill existing old mock data bookings
UPDATE bookings SET total_amount = 850000.00, amount_paid = 50000.00 WHERE id = 1;
UPDATE bookings SET total_amount = 750000.00, amount_paid = 25000.00 WHERE id = 2;
UPDATE bookings SET total_amount = 1900000.00, amount_paid = 100000.00 WHERE id = 3;


-- Insert Customers for dummy
INSERT INTO customers (id, name, mobile, email, dob, branch_id, location, created_by) VALUES
(100, 'Rahul 2025 Dummy', '9000000100', 'dummy1@example.com', '1990-01-01', 1, 'Pune', 'system'),
(101, 'Priya 2025 Dummy', '9000000101', 'dummy2@example.com', '1992-05-15', 1, 'Pune', 'system'),
(102, 'Amit 2026 Dummy', '9000000102', 'dummy3@example.com', '1988-11-20', 1, 'Pune', 'system'),
(103, 'Sneha 2026 Cancelled', '9000000103', 'dummy4@example.com', '1995-03-10', 1, 'Pune', 'system');

-- Insert Vehicles
INSERT INTO vehicles (id, vin, brand, model, variant, colour, fuel_type, transmission, manufactured_date, msrp, status, branch_id, engine_number, chassis_number, key_number, exterior_colour_code, interior_colour_code, created_by) VALUES
(100, 'MH12VIN0000000100', 'Hyundai', 'Creta', 'SX Opt', 'Abyss Black', 'PETROL', 'AUTOMATIC', '2025-01-01', 1900000.00, 'BOOKED', 1, 'ENGCRE100', 'CHSCRE100', 'KEYCRE100', 'BLK01', 'INTBLK01', 'system'),
(101, 'MH12VIN0000000101', 'Hyundai', 'Venue', 'SX', 'Titan Grey', 'DIESEL', 'MANUAL', '2025-05-05', 1300000.00, 'INVOICED', 1, 'ENGVEN101', 'CHSVEN101', 'KEYVEN101', 'GRY01', 'INTBLK02', 'system'),
(102, 'MH12VIN0000000102', 'Hyundai', 'i20', 'Asta (O)', 'Fiery Red', 'PETROL', 'AUTOMATIC', '2026-02-10', 1150000.00, 'AVAILABLE', 1, 'ENGI20102', 'CHSI20102', 'KEYI20102', 'RED01', 'INTGRY01', 'system'),
(103, 'MH12VIN0000000103', 'Hyundai', 'Tucson', 'Platinum', 'Polar White', 'DIESEL', 'AUTOMATIC', '2026-01-15', 3000000.00, 'AVAILABLE', 1, 'ENGTUC103', 'CHSTUC103', 'KEYTUC103', 'WHT01', 'INTBEI01', 'system');

-- Insert Leads
INSERT INTO leads (id, customer_id, assigned_to, model_interested, source, stage, lost_reason, vehicle_id, branch_id, created_by) VALUES
(100, 100, 4, 'Creta', 'WALK_IN', 'BOOKING', NULL, 100, 1, 'system'),
(101, 101, 4, 'Venue', 'ONLINE', 'DELIVERED', NULL, 101, 1, 'system'),
(102, 102, 4, 'i20', 'REFERRAL', 'BOOKING', NULL, 102, 1, 'system'),
(103, 103, 4, 'Tucson', 'PHONE', 'LOST', 'Finance rejected', 103, 1, 'system');

-- Insert Bookings using existing lead/vehicle IDs
-- Mix of ACTIVE and CANCELLED
INSERT INTO bookings (id, lead_id, vehicle_id, total_amount, amount_paid, booking_date, expected_delivery, status, created_by) VALUES
(100, 100, 100, 1900000.00, 50000.00, '2025-01-15', '2025-02-15', 'ACTIVE', 'system'),
(101, 101, 101, 1300000.00, 1300000.00, '2025-06-10', '2025-06-15', 'ACTIVE', 'system'),
(102, 102, 102, 1150000.00, 10000.00, '2026-02-14', '2026-03-01', 'ACTIVE', 'system'),
(103, 103, 103, 3000000.00, 100000.00, '2026-01-20', '2026-02-28', 'CANCELLED', 'system');
