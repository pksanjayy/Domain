-- =============================================
-- DMS Seed Data v4.0 - Service Module
-- =============================================

-- Insert Service Menus
INSERT INTO menus (id, name, path, icon, parent_id, display_order, is_active, created_by) VALUES
(19, 'Service',          '/service',          'build',     NULL, 6, TRUE, 'system'),
(20, 'Service Bookings', '/service/bookings', 'event',     19,   1, TRUE, 'system'),
(21, 'Service Records',  '/service/records',  'history',   19,   2, TRUE, 'system');

-- Assign to SUPER_ADMIN (role_id = 1), MASTER_USER (role_id = 2), and WORKSHOP_EXEC (role_id = 4)
INSERT INTO role_menus (role_id, menu_id) VALUES
(1, 19), (1, 20), (1, 21),
(2, 19), (2, 20), (2, 21),
(4, 19), (4, 20), (4, 21);

-- Insert Permission Matrix for Service Module
INSERT INTO permissions (role_id, module_name, can_create, can_read, can_update, can_delete, created_by) VALUES
(1, 'SERVICE_MANAGEMENT', TRUE, TRUE, TRUE, TRUE, 'system'),
(2, 'SERVICE_MANAGEMENT', TRUE, TRUE, TRUE, TRUE, 'system'),
(4, 'SERVICE_MANAGEMENT', TRUE, TRUE, TRUE, FALSE, 'system');
