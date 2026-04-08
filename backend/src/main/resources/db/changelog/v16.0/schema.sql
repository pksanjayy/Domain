-- v16.0: Multi-role support — replace single role_id FK with user_roles join table

-- Step 1: Create the join table
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Step 2: Migrate existing single-role assignments into the join table
INSERT INTO user_roles (user_id, role_id)
SELECT id, role_id FROM users WHERE role_id IS NOT NULL;

-- Step 3: Drop the old FK constraint and column
ALTER TABLE users DROP FOREIGN KEY fk_users_role;
ALTER TABLE users DROP COLUMN role_id;
