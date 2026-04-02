-- =============================================
-- DMS Seed Data v10.0 - Mock Data for Mumbai (Branch 2) & Chennai (Branch 3)
-- =============================================

-- ======================= MUMBAI (BRANCH 2) ======================= --

-- Inventory: Vehicles
INSERT INTO vehicles (id, vin, brand, model, variant, colour, fuel_type, transmission, manufactured_date, msrp, status, branch_id, engine_number, chassis_number, key_number, exterior_colour_code, interior_colour_code, created_by) VALUES
(20, 'MH12VIN0000000020', 'Hyundai', 'Creta', 'SX Opt', 'Phantom Black', 'PETROL', 'AUTOMATIC', '2023-11-01', 1900000.00, 'AVAILABLE', 2, 'ENGMUM001', 'CHSMUM001', 'KEYMUM001', 'BLK01', 'INTBLK01', 'system'),
(21, 'MH12VIN0000000021', 'Hyundai', 'Venue', 'SX', 'Typhoon Silver', 'DIESEL', 'MANUAL', '2023-11-05', 1300000.00, 'AVAILABLE', 2, 'ENGMUM002', 'CHSMUM002', 'KEYMUM002', 'SLV01', 'INTBLK02', 'system'),
(22, 'MH12VIN0000000022', 'Hyundai', 'i20', 'Asta (O)', 'Fiery Red', 'PETROL', 'AUTOMATIC', '2023-11-10', 1150000.00, 'AVAILABLE', 2, 'ENGMUM003', 'CHSMUM003', 'KEYMUM003', 'RED01', 'INTGRY01', 'system');

-- Inventory: GRN Records
INSERT INTO grn_records (id, grn_number, vehicle_id, transporter_name, dispatch_date, received_date, condition_on_arrival, remarks, branch_id, created_by) VALUES
(20, 'GRN-MUM-001', 20, 'VRL Logistics', '2023-11-05', '2023-11-10', 'GOOD', 'Arrived from plant', 2, 'system'),
(21, 'GRN-MUM-002', 21, 'Safexpress', '2023-11-10', '2023-11-15', 'GOOD', 'Normal shipment', 2, 'system'),
(22, 'GRN-MUM-003', 22, 'Gati', '2023-11-15', '2023-11-20', 'GOOD', 'PDI pending for Mumbai', 2, 'system');

-- Sales: Customers
INSERT INTO customers (id, name, mobile, email, dob, branch_id, location, created_by) VALUES
(20, 'Karan Johar', '9811111110', 'karan@example.com', '1980-01-01', 2, 'Bandra West, Mumbai', 'system'),
(21, 'Priya Tendulkar', '9811111111', 'priya.t@example.com', '1992-05-15', 2, 'Andheri, Mumbai', 'system');

-- Sales: Leads
-- Assign to sales_mum_1 (assume ID is 12 based on v1.0 but actually role assignment IDs vary. We'll use 1 and it'll fail if foreign key is strict. But `assigned_to` usually points to `users` with ID. User IDs range from 1 to 25. Let's look up user 6 which is usually Sales. Just random existing user is fine as mockup).
-- In seed data: admin is 1. sales_hyd_1 is 4. workshop_mum_1 etc.
-- Actually I will assign to user 1 for safety if users changed.
INSERT INTO leads (id, customer_id, assigned_to, model_interested, source, stage, lost_reason, vehicle_id, branch_id, created_by) VALUES
(20, 20, 1, 'Creta', 'WALK_IN', 'NEW_LEAD', NULL, NULL, 2, 'system'),
(21, 21, 1, 'Venue', 'ONLINE', 'DELIVERED', NULL, 21, 2, 'system');

-- Sales: Bookings 
INSERT INTO bookings (id, lead_id, vehicle_id, token_amount, booking_date, expected_delivery, status, created_by) VALUES
(20, 21, 21, 50000.00, '2024-03-01', '2024-03-15', 'ACTIVE', 'system');

-- Service: Service Bookings
INSERT INTO service_bookings (id, branch_id, customer_id, booking_id, booking_date, preferred_service_date, service_type, complaints, status, created_by) VALUES
(20, 2, 20, 'SB-MUM-001', '2024-04-20', '2024-04-25', 'FREE', 'First free service', 'CONFIRMED', 'system');

-- Test Drive: Fleet
INSERT INTO test_drive_fleet (id, branch_id, fleet_id, vin, brand, model, variant, fuel_type, transmission, registration_number, insurance_expiry, rc_expiry, current_odometer, status, last_service_date, next_service_due, created_by) VALUES
(20, 2, 'TD-MUM-01', 'MH02TD0000000001', 'Hyundai', 'Creta', 'SX Opt', 'PETROL', 'AUTOMATIC', 'MH02TD0001', '2024-12-31', '2038-12-31', 4500, 'ACTIVE', '2024-01-10', '2024-07-10', 'system');


-- ======================= CHENNAI (BRANCH 3) ======================= --

-- Inventory: Vehicles
INSERT INTO vehicles (id, vin, brand, model, variant, colour, fuel_type, transmission, manufactured_date, msrp, status, branch_id, engine_number, chassis_number, key_number, exterior_colour_code, interior_colour_code, created_by) VALUES
(30, 'TN09VIN0000000020', 'Hyundai', 'Tucson', 'Platinum', 'Polar White', 'DIESEL', 'AUTOMATIC', '2023-11-01', 3000000.00, 'AVAILABLE', 3, 'ENGCHN001', 'CHSCHN001', 'KEYCHN001', 'WHT01', 'INTBEI01', 'system'),
(31, 'TN09VIN0000000021', 'Hyundai', 'Verna', 'SX (O)', 'Starry Night', 'PETROL', 'AUTOMATIC', '2023-11-05', 1700000.00, 'GRN_RECEIVED', 3, 'ENGCHN002', 'CHSCHN002', 'KEYCHN002', 'BLU01', 'INTBLK03', 'system');

-- Sales: Customers
INSERT INTO customers (id, name, mobile, email, dob, branch_id, location, created_by) VALUES
(30, 'Vikram Vedha', '9822222220', 'vikram@example.com', '1985-01-01', 3, 'Anna Nagar, Chennai', 'system'),
(31, 'Samantha Prabhu', '9822222221', 'sam@example.com', '1992-05-15', 3, 'T Nagar, Chennai', 'system');

-- Sales: Leads
INSERT INTO leads (id, customer_id, assigned_to, model_interested, source, stage, lost_reason, vehicle_id, branch_id, created_by) VALUES
(30, 30, 1, 'Tucson', 'WALK_IN', 'NEW_LEAD', NULL, NULL, 3, 'system'),
(31, 31, 1, 'Verna', 'ONLINE', 'BOOKING', NULL, 31, 3, 'system');

-- Sales: Bookings 
INSERT INTO bookings (id, lead_id, vehicle_id, token_amount, booking_date, expected_delivery, status, created_by) VALUES
(30, 31, 31, 100000.00, '2024-03-01', '2024-03-15', 'ACTIVE', 'system');

-- Service: Service Bookings
INSERT INTO service_bookings (id, branch_id, customer_id, booking_id, booking_date, preferred_service_date, service_type, complaints, status, created_by) VALUES
(30, 3, 30, 'SB-CHN-001', '2024-04-20', '2024-04-25', 'REPAIR', 'Suspension noise', 'CONFIRMED', 'system');

-- Test Drive: Fleet
INSERT INTO test_drive_fleet (id, branch_id, fleet_id, vin, brand, model, variant, fuel_type, transmission, registration_number, insurance_expiry, rc_expiry, current_odometer, status, last_service_date, next_service_due, created_by) VALUES
(30, 3, 'TD-CHN-01', 'TN09TD0000000001', 'Hyundai', 'Tucson', 'Platinum', 'DIESEL', 'AUTOMATIC', 'TN09TD0001', '2024-12-31', '2038-12-31', 4500, 'ACTIVE', '2024-01-10', '2024-07-10', 'system');
