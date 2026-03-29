-- =============================================
-- DMS Seed Data v1.0
-- =============================================

-- Roles
INSERT INTO roles (id, name, description, created_by) VALUES
(1, 'SUPER_ADMIN',     'Full system access — manages users, roles, config',   'system'),
(2, 'MASTER_USER',     'Branch-level admin — manages branch operations',      'system'),
(3, 'SALES_CRM_EXEC',  'Sales executive — leads, quotations, bookings',       'system'),
(4, 'WORKSHOP_EXEC',   'Workshop executive — service jobs, parts',            'system'),
(5, 'MANAGER_VIEWER',  'Read-only manager — dashboards and reports',          'system');

-- Branches
INSERT INTO branches (id, code, name, region, gstin, is_active, created_by) VALUES
(1, 'HYD-001', 'Hyundai Hyderabad Central', 'South',  '36AAACH1234Q1ZV', TRUE, 'system'),
(2, 'MUM-001', 'Hyundai Mumbai Central',    'West',   '27AAACH5678R2ZW', TRUE, 'system'),
(3, 'CHN-001', 'Hyundai Chennai Central',   'South',  '33AAACH9012S3ZX', TRUE, 'system');

-- ─────────────────────────────────────────────────────────
-- Users (password hash = BCrypt strength 12)
-- ─────────────────────────────────────────────────────────
-- Admin@123
-- $2a$12$nl6JumhkNiaGZdQGr7N69u4/EJ9stIAjnTOsCfPEMSJrCVPIz4wVO

-- Sales@123
-- $2a$12$8KxGJx6v9SMrVQ5g3vQKCOvP7RAJfN.5eE3Uy/hCiXCRfL6fkXKGC

-- Workshop@123
-- $2a$12$oW9X0F5.iS0r0KLRTFiVyOIqIv3RN6ZmDJz5L0gq5QZ3/bLLJbMjG

-- Manager@123
-- $2a$12$5fmRxGP0MYiO8qRKhVdVEeQU4DDpXK.K/0Bj7Rj5Yb.S6X7cN.kPC

-- Master@123
-- $2a$12$7eUxKQc.0M0I0bQ8WPKZjegPj7OFhVB/Hm4F.c6pYx9q8X4kSbJlW

-- SUPER_ADMIN (no branch — sees all branches)
INSERT INTO users (id, username, email, password_hash, role_id, branch_id, is_active, created_by) VALUES
(1, 'admin', 'admin@hyundai-dms.com',
 '$2a$12$nl6JumhkNiaGZdQGr7N69u4/EJ9stIAjnTOsCfPEMSJrCVPIz4wVO',
 1, NULL, TRUE, 'system');

-- ─── Hyderabad Branch (ID 1) ───
INSERT INTO users (id, username, email, password_hash, role_id, branch_id, is_active, created_by) VALUES
(2,  'master_hyd_1',    'master.hyd1@hyundai-dms.com',    '$2a$12$nl6JumhkNiaGZdQGr7N69u4/EJ9stIAjnTOsCfPEMSJrCVPIz4wVO', 2, 1, TRUE, 'system'),
(3,  'master_hyd_2',    'master.hyd2@hyundai-dms.com',    '$2a$12$nl6JumhkNiaGZdQGr7N69u4/EJ9stIAjnTOsCfPEMSJrCVPIz4wVO', 2, 1, TRUE, 'system'),
(4,  'sales_hyd_1',     'sales.hyd1@hyundai-dms.com',     '$2a$12$nl6JumhkNiaGZdQGr7N69u4/EJ9stIAjnTOsCfPEMSJrCVPIz4wVO', 3, 1, TRUE, 'system'),
(5,  'sales_hyd_2',     'sales.hyd2@hyundai-dms.com',     '$2a$12$nl6JumhkNiaGZdQGr7N69u4/EJ9stIAjnTOsCfPEMSJrCVPIz4wVO', 3, 1, TRUE, 'system'),
(6,  'workshop_hyd_1',  'workshop.hyd1@hyundai-dms.com',  '$2a$12$nl6JumhkNiaGZdQGr7N69u4/EJ9stIAjnTOsCfPEMSJrCVPIz4wVO', 4, 1, TRUE, 'system'),
(7,  'workshop_hyd_2',  'workshop.hyd2@hyundai-dms.com',  '$2a$12$nl6JumhkNiaGZdQGr7N69u4/EJ9stIAjnTOsCfPEMSJrCVPIz4wVO', 4, 1, TRUE, 'system'),
(8,  'manager_hyd_1',   'manager.hyd1@hyundai-dms.com',   '$2a$12$nl6JumhkNiaGZdQGr7N69u4/EJ9stIAjnTOsCfPEMSJrCVPIz4wVO', 5, 1, TRUE, 'system'),
(9,  'manager_hyd_2',   'manager.hyd2@hyundai-dms.com',   '$2a$12$nl6JumhkNiaGZdQGr7N69u4/EJ9stIAjnTOsCfPEMSJrCVPIz4wVO', 5, 1, TRUE, 'system');

-- ─── Mumbai Branch (ID 2) ───
INSERT INTO users (id, username, email, password_hash, role_id, branch_id, is_active, created_by) VALUES
(10, 'master_mum_1',    'master.mum1@hyundai-dms.com',    '$2a$12$nl6JumhkNiaGZdQGr7N69u4/EJ9stIAjnTOsCfPEMSJrCVPIz4wVO', 2, 2, TRUE, 'system'),
(11, 'master_mum_2',    'master.mum2@hyundai-dms.com',    '$2a$12$nl6JumhkNiaGZdQGr7N69u4/EJ9stIAjnTOsCfPEMSJrCVPIz4wVO', 2, 2, TRUE, 'system'),
(12, 'sales_mum_1',     'sales.mum1@hyundai-dms.com',     '$2a$12$nl6JumhkNiaGZdQGr7N69u4/EJ9stIAjnTOsCfPEMSJrCVPIz4wVO', 3, 2, TRUE, 'system'),
(13, 'sales_mum_2',     'sales.mum2@hyundai-dms.com',     '$2a$12$nl6JumhkNiaGZdQGr7N69u4/EJ9stIAjnTOsCfPEMSJrCVPIz4wVO', 3, 2, TRUE, 'system'),
(14, 'workshop_mum_1',  'workshop.mum1@hyundai-dms.com',  '$2a$12$nl6JumhkNiaGZdQGr7N69u4/EJ9stIAjnTOsCfPEMSJrCVPIz4wVO', 4, 2, TRUE, 'system'),
(15, 'workshop_mum_2',  'workshop.mum2@hyundai-dms.com',  '$2a$12$nl6JumhkNiaGZdQGr7N69u4/EJ9stIAjnTOsCfPEMSJrCVPIz4wVO', 4, 2, TRUE, 'system'),
(16, 'manager_mum_1',   'manager.mum1@hyundai-dms.com',   '$2a$12$nl6JumhkNiaGZdQGr7N69u4/EJ9stIAjnTOsCfPEMSJrCVPIz4wVO', 5, 2, TRUE, 'system'),
(17, 'manager_mum_2',   'manager.mum2@hyundai-dms.com',   '$2a$12$nl6JumhkNiaGZdQGr7N69u4/EJ9stIAjnTOsCfPEMSJrCVPIz4wVO', 5, 2, TRUE, 'system');

-- ─── Chennai Branch (ID 3) ───
INSERT INTO users (id, username, email, password_hash, role_id, branch_id, is_active, created_by) VALUES
(18, 'master_chn_1',    'master.chn1@hyundai-dms.com',    '$2a$12$nl6JumhkNiaGZdQGr7N69u4/EJ9stIAjnTOsCfPEMSJrCVPIz4wVO', 2, 3, TRUE, 'system'),
(19, 'master_chn_2',    'master.chn2@hyundai-dms.com',    '$2a$12$nl6JumhkNiaGZdQGr7N69u4/EJ9stIAjnTOsCfPEMSJrCVPIz4wVO', 2, 3, TRUE, 'system'),
(20, 'sales_chn_1',     'sales.chn1@hyundai-dms.com',     '$2a$12$nl6JumhkNiaGZdQGr7N69u4/EJ9stIAjnTOsCfPEMSJrCVPIz4wVO', 3, 3, TRUE, 'system'),
(21, 'sales_chn_2',     'sales.chn2@hyundai-dms.com',     '$2a$12$nl6JumhkNiaGZdQGr7N69u4/EJ9stIAjnTOsCfPEMSJrCVPIz4wVO', 3, 3, TRUE, 'system'),
(22, 'workshop_chn_1',  'workshop.chn1@hyundai-dms.com',  '$2a$12$nl6JumhkNiaGZdQGr7N69u4/EJ9stIAjnTOsCfPEMSJrCVPIz4wVO', 4, 3, TRUE, 'system'),
(23, 'workshop_chn_2',  'workshop.chn2@hyundai-dms.com',  '$2a$12$nl6JumhkNiaGZdQGr7N69u4/EJ9stIAjnTOsCfPEMSJrCVPIz4wVO', 4, 3, TRUE, 'system'),
(24, 'manager_chn_1',   'manager.chn1@hyundai-dms.com',   '$2a$12$nl6JumhkNiaGZdQGr7N69u4/EJ9stIAjnTOsCfPEMSJrCVPIz4wVO', 5, 3, TRUE, 'system'),
(25, 'manager_chn_2',   'manager.chn2@hyundai-dms.com',   '$2a$12$nl6JumhkNiaGZdQGr7N69u4/EJ9stIAjnTOsCfPEMSJrCVPIz4wVO', 5, 3, TRUE, 'system');

-- ─────────────────────────────────────────────────────────
-- Menus (top-level)
-- ─────────────────────────────────────────────────────────
INSERT INTO menus (id, name, path, icon, parent_id, display_order, is_active, created_by) VALUES
(1,  'Dashboard',  '/dashboard',   'dashboard',   NULL, 1,  TRUE, 'system'),
(2,  'Inventory',  '/inventory',   'inventory',   NULL, 2,  TRUE, 'system'),
(3,  'Sales',      '/sales',       'point_of_sale', NULL, 3,  TRUE, 'system'),
(4,  'Admin',      '/admin',       'admin_panel_settings', NULL, 4,  TRUE, 'system'),
(5,  'Reports',    '/reports',     'assessment',  NULL, 5,  TRUE, 'system');

-- Menus (sub-items) — Note: Code Management (old ID 14) is removed
INSERT INTO menus (id, name, path, icon, parent_id, display_order, is_active, created_by) VALUES
(6,  'Vehicle Stock',     '/inventory/vehicles',     'directions_car', 2, 1,  TRUE, 'system'),
(7,  'GRN',               '/inventory/grn',          'receipt_long',   2, 2,  TRUE, 'system'),
(8,  'Leads',             '/sales/leads',            'people',         3, 1,  TRUE, 'system'),
(9,  'Quotations',        '/sales/quotations',       'request_quote',  3, 2,  TRUE, 'system'),
(10, 'Bookings',          '/sales/bookings',         'book_online',    3, 3,  TRUE, 'system'),
(11, 'User Management',   '/admin/users',            'manage_accounts',4, 1,  TRUE, 'system'),
(12, 'Role Management',   '/admin/roles',            'security',       4, 2,  TRUE, 'system'),
(13, 'Menu Management',   '/admin/menus',            'menu_open',      4, 3,  TRUE, 'system'),
(15, 'Branch Management', '/admin/branches',         'store',          4, 4,  TRUE, 'system'),
(16, 'Sales Report',      '/reports/sales',          'bar_chart',      5, 1,  TRUE, 'system'),
(17, 'Inventory Report',  '/reports/inventory',      'analytics',      5, 2,  TRUE, 'system'),
(18, 'Audit Logs',        '/reports/audit-logs',     'history',        5, 3,  TRUE, 'system');

-- Role-Menu assignments: SUPER_ADMIN gets all menus (no code management)
INSERT INTO role_menus (role_id, menu_id) VALUES
(1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(1,9),(1,10),(1,11),(1,12),(1,13),(1,15),(1,16),(1,17),(1,18);

-- MASTER_USER: Dashboard, Inventory, Sales, Reports (no Admin)
INSERT INTO role_menus (role_id, menu_id) VALUES
(2,1),(2,2),(2,3),(2,5),(2,6),(2,7),(2,8),(2,9),(2,10),(2,16),(2,17);

-- SALES_CRM_EXEC: Dashboard, Sales
INSERT INTO role_menus (role_id, menu_id) VALUES
(3,1),(3,3),(3,8),(3,9),(3,10);

-- WORKSHOP_EXEC: Dashboard, Inventory
INSERT INTO role_menus (role_id, menu_id) VALUES
(4,1),(4,2),(4,6),(4,7);

-- MANAGER_VIEWER: Dashboard, Reports
INSERT INTO role_menus (role_id, menu_id) VALUES
(5,1),(5,5),(5,16),(5,17),(5,18);

-- Permissions matrix
-- SUPER_ADMIN: full CRUD on all modules
INSERT INTO permissions (role_id, module_name, can_create, can_read, can_update, can_delete, created_by) VALUES
(1, 'USER_MANAGEMENT',      TRUE, TRUE, TRUE, TRUE, 'system'),
(1, 'ROLE_MANAGEMENT',      TRUE, TRUE, TRUE, TRUE, 'system'),
(1, 'INVENTORY_MANAGEMENT', TRUE, TRUE, TRUE, TRUE, 'system'),
(1, 'SALES_MANAGEMENT',     TRUE, TRUE, TRUE, TRUE, 'system'),
(1, 'REPORTS',              TRUE, TRUE, TRUE, TRUE, 'system'),
(1, 'BRANCH_MANAGEMENT',    TRUE, TRUE, TRUE, TRUE, 'system');

-- MASTER_USER: CRUD on inventory+sales, read reports
INSERT INTO permissions (role_id, module_name, can_create, can_read, can_update, can_delete, created_by) VALUES
(2, 'INVENTORY_MANAGEMENT', TRUE, TRUE, TRUE, TRUE,  'system'),
(2, 'SALES_MANAGEMENT',     TRUE, TRUE, TRUE, TRUE,  'system'),
(2, 'REPORTS',              FALSE, TRUE, FALSE, FALSE, 'system');

-- SALES_CRM_EXEC: CRU on sales, read inventory
INSERT INTO permissions (role_id, module_name, can_create, can_read, can_update, can_delete, created_by) VALUES
(3, 'SALES_MANAGEMENT',     TRUE, TRUE, TRUE, FALSE,  'system'),
(3, 'INVENTORY_MANAGEMENT', FALSE, TRUE, FALSE, FALSE, 'system');

-- WORKSHOP_EXEC: CRU on inventory
INSERT INTO permissions (role_id, module_name, can_create, can_read, can_update, can_delete, created_by) VALUES
(4, 'INVENTORY_MANAGEMENT', TRUE, TRUE, TRUE, FALSE, 'system');

-- MANAGER_VIEWER: read only on everything
INSERT INTO permissions (role_id, module_name, can_create, can_read, can_update, can_delete, created_by) VALUES
(5, 'INVENTORY_MANAGEMENT', FALSE, TRUE, FALSE, FALSE, 'system'),
(5, 'SALES_MANAGEMENT',     FALSE, TRUE, FALSE, FALSE, 'system'),
(5, 'REPORTS',              FALSE, TRUE, FALSE, FALSE, 'system');

-- Codes (generic lookup table)
INSERT INTO codes (category, code, label, display_order, created_by) VALUES
('VEHICLE_COLOR',  'WHITE',   'Polar White',      1, 'system'),
('VEHICLE_COLOR',  'SILVER',  'Sleek Silver',     2, 'system'),
('VEHICLE_COLOR',  'BLACK',   'Phantom Black',    3, 'system'),
('VEHICLE_COLOR',  'RED',     'Fiery Red',        4, 'system'),
('VEHICLE_COLOR',  'BLUE',    'Marina Blue',      5, 'system'),
('FUEL_TYPE',      'PETROL',  'Petrol',           1, 'system'),
('FUEL_TYPE',      'DIESEL',  'Diesel',           2, 'system'),
('FUEL_TYPE',      'EV',      'Electric',         3, 'system'),
('FUEL_TYPE',      'HYBRID',  'Hybrid',           4, 'system'),
('LEAD_SOURCE',    'WALK_IN', 'Walk-in',          1, 'system'),
('LEAD_SOURCE',    'ONLINE',  'Online Enquiry',   2, 'system'),
('LEAD_SOURCE',    'REFERRAL','Referral',          3, 'system'),
('LEAD_SOURCE',    'CAMPAIGN','Campaign',          4, 'system'),
('LEAD_STATUS',    'NEW',     'New',              1, 'system'),
('LEAD_STATUS',    'CONTACTED','Contacted',       2, 'system'),
('LEAD_STATUS',    'QUALIFIED','Qualified',       3, 'system'),
('LEAD_STATUS',    'LOST',    'Lost',             4, 'system'),
('LEAD_STATUS',    'WON',     'Won',              5, 'system');
