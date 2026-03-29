-- Test seed data for InventoryIntegrationTest
INSERT INTO branches (id, code, name, region, gstin, is_active, created_at) 
VALUES (1, 'TST01', 'Test Branch', 'South', 'GSTIN12345', true, CURRENT_TIMESTAMP);

INSERT INTO roles (id, name, description, created_at) 
VALUES (1, 'SUPER_ADMIN', 'Super Admin Role', CURRENT_TIMESTAMP);

INSERT INTO users (id, username, email, password_hash, role_id, branch_id, is_active, failed_login_attempts, force_password_change, created_at) 
VALUES (1, 'admin', 'admin@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 1, true, 0, false, CURRENT_TIMESTAMP);
