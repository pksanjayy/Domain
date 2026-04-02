-- liquibase formatted sql

-- changeset v15.0-1:add-deleted-soft-delete-columns
ALTER TABLE customers ADD COLUMN deleted BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE leads ADD COLUMN deleted BOOLEAN DEFAULT FALSE NOT NULL;
