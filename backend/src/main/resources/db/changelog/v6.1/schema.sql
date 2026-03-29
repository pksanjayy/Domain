-- v6.1: Decouple Service Bookings from Vehicles
ALTER TABLE service_bookings DROP FOREIGN KEY fk_sb_vehicle;
ALTER TABLE service_bookings DROP COLUMN vehicle_id;
