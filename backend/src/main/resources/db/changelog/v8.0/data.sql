-- =============================================
-- DMS Seed Data v8.0 - Mock Data for all Modules
-- =============================================

-- ----------------------------------------------------------------------------
-- 1. CLEANUP (Optional for local dev, ensure idempotent execution)
-- ----------------------------------------------------------------------------
DELETE FROM test_drive_bookings;
DELETE FROM test_drive_fleet;
DELETE FROM service_records;
DELETE FROM service_bookings;
DELETE FROM payments;
DELETE FROM bookings;
DELETE FROM leads;
DELETE FROM customers;
DELETE FROM stock_transfers;
DELETE FROM vehicle_accessories;
DELETE FROM grn_records;
DELETE FROM vehicles;

-- Branch 2 (Mumbai) and 3 (Chennai) are now seeded in v1.0/data.sql

-- ----------------------------------------------------------------------------
-- 2. INVENTORY MODULE (Vehicles, Accessories, GRN, PDI, Stock Transfers)
-- ----------------------------------------------------------------------------

-- Insert 20 Vehicles (branch_id = 1)
INSERT INTO vehicles (id, vin, brand, model, variant, colour, fuel_type, transmission, manufactured_date, msrp, status, branch_id, engine_number, chassis_number, key_number, exterior_colour_code, interior_colour_code, created_by) VALUES
(1, 'MH12VIN0000000001', 'Hyundai', 'Creta', 'SX Opt', 'Abyss Black', 'PETROL', 'AUTOMATIC', '2023-11-01', 1900000.00, 'AVAILABLE', 1, 'ENGCRE001', 'CHSCRE001', 'KEYCRE001', 'BLK01', 'INTBLK01', 'system'),
(2, 'MH12VIN0000000002', 'Hyundai', 'Venue', 'SX', 'Titan Grey', 'DIESEL', 'MANUAL', '2023-11-05', 1300000.00, 'AVAILABLE', 1, 'ENGVEN001', 'CHSVEN001', 'KEYVEN001', 'GRY01', 'INTBLK02', 'system'),
(3, 'MH12VIN0000000003', 'Hyundai', 'i20', 'Asta (O)', 'Fiery Red', 'PETROL', 'AUTOMATIC', '2023-11-10', 1150000.00, 'AVAILABLE', 1, 'ENGI20001', 'CHSI20001', 'KEYI20001', 'RED01', 'INTGRY01', 'system'),
(4, 'MH12VIN0000000004', 'Hyundai', 'Tucson', 'Platinum', 'Polar White', 'DIESEL', 'AUTOMATIC', '2023-10-15', 3000000.00, 'AVAILABLE', 1, 'ENGTUC001', 'CHSTUC001', 'KEYTUC001', 'WHT01', 'INTBEI01', 'system'),
(5, 'MH12VIN0000000005', 'Hyundai', 'Verna', 'SX (O)', 'Starry Night', 'PETROL', 'AUTOMATIC', '2023-12-01', 1700000.00, 'GRN_RECEIVED', 1, 'ENGVER001', 'CHSVER001', 'KEYVER001', 'BLU01', 'INTBLK03', 'system'),
(6, 'MH12VIN0000000006', 'Hyundai', 'Aura', 'SX (+)', 'Titan Grey', 'PETROL', 'MANUAL', '2023-11-20', 850000.00, 'BOOKED', 1, 'ENGAUR001', 'CHSAUR001', 'KEYAUR001', 'GRY02', 'INTBEI02', 'system'),
(7, 'MH12VIN0000000007', 'Hyundai', 'Grand i10 Nios', 'Asta', 'Aqua Teal', 'PETROL', 'MANUAL', '2023-12-05', 750000.00, 'INVOICED', 1, 'ENGG10001', 'CHSG10001', 'KEYG10001', 'BLU02', 'INTGRY02', 'system'),
(8, 'MH12VIN0000000008', 'Hyundai', 'IONIQ 5', 'Premium', 'Gravity Gold Matte', 'ELECTRIC', 'AUTOMATIC', '2023-09-10', 4500000.00, 'AVAILABLE', 1, 'ENGION001', 'CHSION001', 'KEYION001', 'GLD01', 'INTBLK04', 'system'),
(9, 'MH12VIN0000000009', 'Hyundai', 'Alcazar', 'Signature', 'Typhoon Silver', 'DIESEL', 'AUTOMATIC', '2023-11-25', 2100000.00, 'AVAILABLE', 1, 'ENGALC001', 'CHSALC001', 'KEYALC001', 'SLV01', 'INTGBR01', 'system'),
(10, 'MH12VIN0000000010', 'Hyundai', 'Creta', 'E', 'Polar White', 'PETROL', 'MANUAL', '2023-12-10', 1100000.00, 'IN_TRANSIT', 1, 'ENGCRE002', 'CHSCRE002', 'KEYCRE002', 'WHT02', 'INTBLK05', 'system'),
(11, 'MH12VIN0000000011', 'Hyundai', 'Venue N Line', 'N8', 'Shadow Grey', 'PETROL', 'AUTOMATIC', '2023-12-08', 1400000.00, 'AVAILABLE', 1, 'ENGVNN001', 'CHSVNN001', 'KEYVNN001', 'GRY03', 'INTBLK06', 'system'),
(12, 'MH12VIN0000000012', 'Hyundai', 'i20 N Line', 'N8', 'Thunder Blue', 'PETROL', 'AUTOMATIC', '2023-11-30', 1250000.00, 'AVAILABLE', 1, 'ENGI2N001', 'CHSI2N001', 'KEYI2N001', 'BLU03', 'INTBLK07', 'system'),
(13, 'MH12VIN0000000013', 'Hyundai', 'Kona Electric', 'Premium Dual Tone', 'Phantom Black', 'ELECTRIC', 'AUTOMATIC', '2023-10-20', 2400000.00, 'HOLD', 1, 'ENGKON001', 'CHSKON001', 'KEYKON001', 'BLK02', 'INTBLK08', 'system'),
(14, 'MH12VIN0000000014', 'Hyundai', 'Creta', 'S', 'Abyss Black', 'DIESEL', 'MANUAL', '2024-01-05', 1450000.00, 'IN_TRANSIT', 1, 'ENGCRE003', 'CHSCRE003', 'KEYCRE003', 'BLK03', 'INTBLK09', 'system'),
(15, 'MH12VIN0000000015', 'Hyundai', 'Venue', 'S (O)', 'Fiery Red', 'PETROL', 'AUTOMATIC', '2024-01-10', 1100000.00, 'AVAILABLE', 1, 'ENGVEN002', 'CHSVEN002', 'KEYVEN002', 'RED02', 'INTBLK10', 'system'),
(16, 'MH12VIN0000000016', 'Hyundai', 'Tucson', 'Signature AWD', 'Amazon Grey', 'DIESEL', 'AUTOMATIC', '2023-08-15', 3500000.00, 'AVAILABLE', 1, 'ENGTUC002', 'CHSTUC002', 'KEYTUC002', 'GRY04', 'INTGBR02', 'system');

-- Insert GRN Records (only for received status variants: GRN_RECEIVED, PDI_PENDING, PDI_DONE, AVAILABLE, BOOKED, INVOICED, HOLD)
-- (Exclude IN_TRANSIT IDs: 10, 14)
INSERT INTO grn_records (id, grn_number, vehicle_id, transporter_name, dispatch_date, received_date, condition_on_arrival, remarks, branch_id, created_by) VALUES
(1, 'GRN-2023-0001', 1, 'VRL Logistics', '2023-11-05', '2023-11-10', 'GOOD', 'Received safely', 1, 'system'),
(2, 'GRN-2023-0002', 2, 'Safexpress', '2023-11-10', '2023-11-15', 'GOOD', 'Minor scratch on bumper identified, reported', 1, 'system'),
(3, 'GRN-2023-0003', 3, 'Gati', '2023-11-15', '2023-11-20', 'GOOD', 'PDI pending', 1, 'system'),
(4, 'GRN-2023-0004', 4, 'Delhivery', '2023-10-20', '2023-10-25', 'GOOD', 'Prime condition', 1, 'system'),
(5, 'GRN-2023-0005', 5, 'VRL Logistics', '2023-12-05', '2023-12-10', 'GOOD', 'Just arrived', 1, 'system'),
(6, 'GRN-2023-0006', 6, 'Gati', '2023-11-25', '2023-11-30', 'GOOD', 'All documents okay', 1, 'system'),
(7, 'GRN-2023-0007', 7, 'Safexpress', '2023-12-10', '2023-12-15', 'GOOD', 'Clean', 1, 'system'),
(8, 'GRN-2023-0008', 8, 'Bluedart', '2023-09-15', '2023-09-20', 'GOOD', 'Electric vehicle specific offloading done safely', 1, 'system'),
(9, 'GRN-2023-0009', 9, 'VRL Logistics', '2023-11-30', '2023-12-05', 'GOOD', 'PDI assigned to tech', 1, 'system'),
(10, 'GRN-2023-0010', 11, 'Gati', '2023-12-15', '2023-12-20', 'GOOD', 'N Line arrived', 1, 'system'),
(11, 'GRN-2023-0011', 12, 'Safexpress', '2023-12-05', '2023-12-10', 'GOOD', 'i20 N line perfect', 1, 'system'),
(12, 'GRN-2023-0012', 13, 'Delhivery', '2023-10-25', '2023-10-30', 'GOOD', 'Held for VIP client check', 1, 'system'),
(13, 'GRN-2024-0013', 15, 'VRL Logistics', '2024-01-15', '2024-01-20', 'GOOD', 'New year batch', 1, 'system'),
(14, 'GRN-2023-0014', 16, 'Bluedart', '2023-08-20', '2023-08-25', 'GOOD', 'Premium SUV allocated', 1, 'system');

-- Insert Vehicle Accessories
INSERT INTO vehicle_accessories (vehicle_id, name, cost, fitted_at, created_by) VALUES
(6, 'Floor Mats', 2500.00, '2023-12-02 10:00:00', 'system'),
(6, 'Mud Flaps', 1200.00, '2023-12-02 10:30:00', 'system'),
(7, 'Premium Seat Covers', 15000.00, '2023-12-17 11:00:00', 'system'),
(7, 'Sun Visors', 3500.00, '2023-12-17 11:30:00', 'system'),
(1, 'Dashcam', 8000.00, '2023-11-12 14:00:00', 'system');

-- Insert Stock Transfers (assuming we have branch_id 1 and maybe 2 exists in DB normally, let's use to_branch 2 if exists, or just say it's pending)
INSERT INTO stock_transfers (vehicle_id, from_branch_id, to_branch_id, requested_by, status, request_date, remarks, created_by) VALUES
(2, 1, 2, 1, 'PENDING', '2023-12-01 10:00:00', 'Requested by secondary branch for display', 'system'),
(8, 2, 1, 1, 'COMPLETED', '2023-09-01 10:00:00', 'Received from secondary branch', 'system');

-- ----------------------------------------------------------------------------
-- 3. SALES MODULE (Customers, Leads, Bookings, Payments)
-- ----------------------------------------------------------------------------

-- Insert Customers (id, name, mobile, email, dob, branch_id, location, created_by)
INSERT INTO customers (id, name, mobile, email, dob, branch_id, location, created_by) VALUES
(1, 'Rahul Sharma', '9876543210', 'rahul.sharma@example.com', '1990-01-01', 1, 'Baner, Pune, Maharashtra 411045', 'system'),
(2, 'Priya Patel', '9876543211', 'priya.patel@example.com', '1992-05-15', 1, 'Kothrud, Pune, Maharashtra 411038', 'system'),
(3, 'Amit Deshmukh', '9876543212', 'amit.d@example.com', '1988-11-20', 1, 'Wakad, Pune, Maharashtra 411057', 'system'),
(4, 'Sneha Joshi', '9876543213', 'sneha.j@example.com', '1995-03-10', 1, 'Aundh, Pune, Maharashtra 411007', 'system'),
(5, 'Vikram Singh', '9876543214', 'vikram.singh@example.com', '1985-08-25', 1, 'Viman Nagar, Pune, Maharashtra 411014', 'system'),
(6, 'Neha Gupta', '9876543215', 'neha.g@example.com', '1991-12-05', 1, 'Kalyani Nagar, Pune, Maharashtra 411006', 'system'),
(7, 'Ramesh Rao', '9876543216', 'ramesh.rao@example.com', '1982-04-30', 1, 'Hinjewadi, Pune, Maharashtra 411057', 'system'),
(8, 'Anjali Verma', '9876543217', 'anjali.v@example.com', '1993-09-15', 1, 'Koregaon Park, Pune, Maharashtra 411001', 'system'),
(9, 'Suresh Nair', '9876543218', 'suresh.nair@example.com', '1987-07-20', 1, 'Magarpatta, Pune, Maharashtra 411028', 'system'),
(10, 'Kavita Menon', '9876543219', 'kavita.m@example.com', '1994-02-18', 1, 'Pimpri, Pune, Maharashtra 411018', 'system'),
(11, 'Raj Malhotra', '9876543220', 'raj.m@example.com', '1989-10-12', 1, 'Baner, Pune, Maharashtra 411021', 'system'),
(12, 'Swati Kulkarni', '9876543221', 'swati.k@example.com', '1996-06-25', 1, 'Kothrud, Pune, Maharashtra 411038', 'system'),
(13, 'Deepak Chavan', '9876543222', 'deepak.c@example.com', '1984-01-30', 1, 'Swargate, Pune, Maharashtra 411002', 'system'),
(14, 'Pooja Hegde', '9876543223', 'pooja.h@example.com', '1997-11-08', 1, 'Shivajinagar, Pune, Maharashtra 411005', 'system'),
(15, 'Akash Shinde', '9876543224', 'akash.s@example.com', '1990-05-22', 1, 'Hadapsar, Pune, Maharashtra 411028', 'system');

-- Insert Leads (id, customer_id, assigned_to, model_interested, source, stage, lost_reason, vehicle_id, branch_id, created_by)
INSERT INTO leads (id, customer_id, assigned_to, model_interested, source, stage, lost_reason, vehicle_id, branch_id, created_by) VALUES
(1, 1, 4, 'Creta', 'WALK_IN', 'NEW_LEAD', NULL, NULL, 1, 'system'),
(2, 2, 4, 'Venue', 'ONLINE', 'DELIVERED', NULL, 6, 1, 'system'),
(3, 3, 4, 'i20', 'REFERRAL', 'NEW_LEAD', NULL, NULL, 1, 'system'),
(4, 4, 4, 'IONIQ 5', 'PHONE', 'TEST_DRIVE', NULL, NULL, 1, 'system'),
(5, 5, 4, 'Tucson', 'PHONE', 'LOST', 'Bought another brand', NULL, 1, 'system'),
(6, 6, 5, 'Verna', 'WALK_IN', 'TEST_DRIVE', NULL, NULL, 1, 'system'),
(7, 7, 5, 'Grand i10 Nios', 'ONLINE', 'DELIVERED', NULL, 7, 1, 'system'),
(8, 8, 5, 'Alcazar', 'REFERRAL', 'QUOTATION', NULL, NULL, 1, 'system'),
(9, 9, 4, 'Creta', 'ONLINE', 'NEW_LEAD', NULL, NULL, 1, 'system'),
(10, 10, 5, 'Aura', 'CAMP', 'BOOKING', NULL, 1, 1, 'system');

-- Insert Bookings (for WON leads / existing customers)
-- (id, lead_id, vehicle_id, token_amount, booking_date, expected_delivery, status, created_by)
INSERT INTO bookings (id, lead_id, vehicle_id, token_amount, booking_date, expected_delivery, status, created_by) VALUES
(1, 2, 6, 50000.00, '2024-03-01', '2024-03-15', 'ACTIVE', 'system'),
(2, 7, 7, 25000.00, '2024-03-05', '2024-03-10', 'ACTIVE', 'system'),
(3, 10, 1, 100000.00, '2024-03-25', '2024-04-10', 'ACTIVE', 'system');

-- Insert Payments
-- (payment_id, customer_id, payment_date, amount_paid, payment_method, transaction_id, payment_status, total_price, created_by)
INSERT INTO payments (payment_id, customer_id, payment_date, amount_paid, payment_method, transaction_id, payment_status, total_price, created_by) VALUES
(1, 2, '2024-03-01', 50000.00, 'CREDIT_CARD', 'TXN123456789', 'PENDING', 850000.00, 'system'),
(2, 7, '2024-03-05', 25000.00, 'UPI', 'UPI987654321', 'PAID', 750000.00, 'system'),
(3, 7, '2024-03-15', 725000.00, 'BANK_TRANSFER', 'NEFT555555', 'PAID', 750000.00, 'system'),
(4, 10, '2024-03-25', 100000.00, 'CASH', 'CHK100200300', 'PENDING', 1900000.00, 'system');

-- ----------------------------------------------------------------------------
-- 4. SERVICE MODULE (Service Bookings, Service Records)
-- ----------------------------------------------------------------------------

-- Insert Service Bookings
-- (id, branch_id, customer_id, booking_id, booking_date, preferred_service_date, service_type, complaints, status, created_by)
INSERT INTO service_bookings (id, branch_id, customer_id, booking_id, booking_date, preferred_service_date, service_type, complaints, status, created_by) VALUES
(1, 1, 3, 'SB-2024-001', '2024-03-20', '2024-04-05', 'FREE', 'First free service', 'CONFIRMED', 'system'),
(2, 1, 7, 'SB-2024-002', '2024-03-22', '2024-04-10', 'PAID', 'Routine oil change required', 'CONFIRMED', 'system'),
(3, 1, 5, 'SB-2024-003', '2024-03-15', '2024-03-25', 'REPAIR', 'Brake pad replacement', 'CONFIRMED', 'system'),
(4, 1, 1, 'SB-2024-004', '2024-03-10', '2024-03-20', 'REPAIR', 'AC cooling issue resolved', 'COMPLETED', 'system'),
(5, 1, 8, 'SB-2024-005', '2024-03-05', '2024-03-15', 'REPAIR', 'Minor dent removal on rear door', 'COMPLETED', 'system'),
(6, 1, 15, 'SB-2024-006', '2024-03-30', '2024-04-12', 'FREE', 'General checkup', 'CONFIRMED', 'system');

-- Insert Service Records
-- (id, branch_id, service_booking_id, service_date, odometer, work_performed, parts_used, no_of_technicians, technician_hours, notes, status, payment_status, created_by)
INSERT INTO service_records (id, branch_id, service_booking_id, service_date, odometer, work_performed, parts_used, no_of_technicians, technician_hours, notes, status, payment_status, created_by) VALUES
(1, 1, 4, '2024-03-21', 12000.00, 'Refilled AC gas, changed filter', 'AC Filter, R134a Gas', 2, 3.5, 'AC cooling efficiently now.', 'COMPLETED', 'PAID', 'system'),
(2, 1, 5, '2024-03-18', 45000.00, 'Dent removal and repainting', 'Paint, Polish', 1, 5.0, 'Color matched perfectly.', 'COMPLETED', 'UNPAID', 'system');

-- ----------------------------------------------------------------------------
-- 5. TEST DRIVE MODULE (Test Drive Fleet, Test Drive Bookings)
-- ----------------------------------------------------------------------------

-- Insert Test Drive Fleet Vehicles
-- (id, branch_id, fleet_id, vin, brand, model, variant, fuel_type, transmission, registration_number, insurance_expiry, rc_expiry, current_odometer, status, last_service_date, next_service_due, created_by)
INSERT INTO test_drive_fleet (id, branch_id, fleet_id, vin, brand, model, variant, fuel_type, transmission, registration_number, insurance_expiry, rc_expiry, current_odometer, status, last_service_date, next_service_due, created_by) VALUES
(1, 1, 'TD-CRE-01', 'MH12TD0000000001', 'Hyundai', 'Creta', 'SX Opt', 'PETROL', 'AUTOMATIC', 'MH12TD0001', '2024-12-31', '2038-12-31', 4500, 'ACTIVE', '2024-01-10', '2024-07-10', 'system'),
(2, 1, 'TD-VEN-01', 'MH12TD0000000002', 'Hyundai', 'Venue', 'SX', 'DIESEL', 'MANUAL', 'MH12TD0002', '2024-11-30', '2038-11-30', 8200, 'ACTIVE', '2024-02-15', '2024-08-15', 'system'),
(3, 1, 'TD-I20-01', 'MH12TD0000000003', 'Hyundai', 'i20 N Line', 'N8', 'PETROL', 'AUTOMATIC', 'MH12TD0003', '2024-10-15', '2038-10-15', 3100, 'ACTIVE', '2023-12-05', '2024-06-05', 'system'),
(4, 1, 'TD-TUC-01', 'MH12TD0000000004', 'Hyundai', 'Tucson', 'Signature AWD', 'DIESEL', 'AUTOMATIC', 'MH12TD0004', '2024-09-20', '2038-09-20', 12000, 'MAINTENANCE', '2024-03-25', '2024-09-25', 'system'),
(5, 1, 'TD-ION-01', 'MH12TD0000000005', 'Hyundai', 'IONIQ 5', 'Premium', 'ELECTRIC', 'AUTOMATIC', 'MH12TD0005', '2025-01-15', '2039-01-15', 1800, 'ACTIVE', '2024-01-20', '2024-07-20', 'system'),
(6, 1, 'TD-VER-01', 'MH12TD0000000006', 'Hyundai', 'Verna', 'SX (O)', 'PETROL', 'AUTOMATIC', 'MH12TD0006', '2024-08-10', '2038-08-10', 5600, 'ACTIVE', '2024-02-01', '2024-08-01', 'system'),
(7, 1, 'TD-ALC-01', 'MH12TD0000000007', 'Hyundai', 'Alcazar', 'Signature', 'DIESEL', 'AUTOMATIC', 'MH12TD0007', '2024-07-05', '2038-07-05', 9000, 'RETIRED', '2023-11-15', '2024-05-15', 'system');

-- Insert Test Drive Bookings
-- (id, customer_id, fleet_id, booking_id, sales_executive_id, booking_date, test_drive_date, time_slot, pickup_required, status, created_by)
INSERT INTO test_drive_bookings (id, customer_id, fleet_id, booking_id, sales_executive_id, booking_date, test_drive_date, time_slot, pickup_required, status, created_by) VALUES
(1, 1, 1, 'TDB-2024-001', 4, '2024-03-26', '2024-03-30', '10:00:00', 0, 'SCHEDULED', 'system'),
(2, 4, 5, 'TDB-2024-002', 4, '2024-03-25', '2024-03-28', '11:30:00', 1, 'COMPLETED', 'system'),
(3, 6, 6, 'TDB-2024-003', 5, '2024-03-27', '2024-04-02', '14:00:00', 0, 'SCHEDULED', 'system'),
(4, 9, 1, 'TDB-2024-004', 4, '2024-03-28', '2024-03-29', '16:00:00', 0, 'CANCELLED', 'system'),
(5, 8, 7, 'TDB-2024-005', 5, '2023-11-20', '2023-11-25', '09:30:00', 0, 'COMPLETED', 'system'),
(6, 13, 2, 'TDB-2024-006', 5, '2024-03-28', '2024-04-05', '12:00:00', 1, 'SCHEDULED', 'system');
