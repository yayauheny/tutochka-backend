-- liquibase formatted sql

-- changeset yayauheny:add-users-acquisition-tag-check
ALTER TABLE users
    ADD CONSTRAINT users_acquisition_tag_check
        CHECK (acquisition_tag IN ('direct', 'website', 'other'));
-- rollback ALTER TABLE users DROP CONSTRAINT users_acquisition_tag_check;
