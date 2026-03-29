-- =============================================
-- DMS Schema Update v12.0
-- Modify Bookings to use total_amount and amount_paid
-- =============================================

ALTER TABLE bookings DROP COLUMN token_amount;
ALTER TABLE bookings ADD COLUMN total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00;
ALTER TABLE bookings ADD COLUMN amount_paid DECIMAL(10, 2) NOT NULL DEFAULT 0.00;
