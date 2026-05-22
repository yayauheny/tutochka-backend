-- liquibase formatted sql

-- changeset yayauheny:add-users-acquisition-tag
ALTER TABLE users
    ADD COLUMN acquisition_tag TEXT NOT NULL DEFAULT 'direct';
-- rollback ALTER TABLE users DROP COLUMN acquisition_tag;
