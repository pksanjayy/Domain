-- =============================================
-- DMS Seed Data v11.0 - Custom Test Drive Role Access
-- =============================================

-- 1. Grant SALES_CRM_EXEC (Role 3) access to "Test Drive" and "Test Drive Bookings"
INSERT INTO role_menus (role_id, menu_id)
SELECT 3, id FROM menus WHERE name = 'Test Drive'
AND NOT EXISTS (SELECT 1 FROM role_menus WHERE role_id = 3 AND menu_id = (SELECT id FROM menus WHERE name = 'Test Drive'));

INSERT INTO role_menus (role_id, menu_id)
SELECT 3, id FROM menus WHERE name = 'Test Drive Bookings'
AND NOT EXISTS (SELECT 1 FROM role_menus WHERE role_id = 3 AND menu_id = (SELECT id FROM menus WHERE name = 'Test Drive Bookings'));

-- 2. Grant WORKSHOP_EXEC (Role 4) access to "Test Drive" and "Test Drive Fleet"
INSERT INTO role_menus (role_id, menu_id)
SELECT 4, id FROM menus WHERE name = 'Test Drive'
AND NOT EXISTS (SELECT 1 FROM role_menus WHERE role_id = 4 AND menu_id = (SELECT id FROM menus WHERE name = 'Test Drive'));

INSERT INTO role_menus (role_id, menu_id)
SELECT 4, id FROM menus WHERE name = 'Test Drive Fleet'
AND NOT EXISTS (SELECT 1 FROM role_menus WHERE role_id = 4 AND menu_id = (SELECT id FROM menus WHERE name = 'Test Drive Fleet'));

-- 3. Grant permissions for both roles (Read, Create, Update)
-- Check if permission already exists for TEST_DRIVE
DELETE FROM permissions WHERE role_id IN (3, 4) AND module_name = 'TEST_DRIVE';

INSERT INTO permissions (role_id, module_name, can_create, can_read, can_update, can_delete, created_by) VALUES
(3, 'TEST_DRIVE', TRUE, TRUE, TRUE, FALSE, 'system'),
(4, 'TEST_DRIVE', TRUE, TRUE, TRUE, FALSE, 'system');
