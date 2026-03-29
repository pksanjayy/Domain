-- v6.0: Add total_price column to payments table
ALTER TABLE payments ADD COLUMN total_price DECIMAL(12, 2);
