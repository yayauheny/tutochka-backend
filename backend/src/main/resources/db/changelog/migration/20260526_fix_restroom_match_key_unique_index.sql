-- liquibase formatted sql

-- changeset yayauheny:fix-restroom-match-key-unique-index
DROP INDEX IF EXISTS restrooms_restroom_match_key_unique_idx;
CREATE UNIQUE INDEX IF NOT EXISTS restrooms_restroom_match_key_unique_idx
    ON restrooms (restroom_match_key);
-- rollback DROP INDEX IF EXISTS restrooms_restroom_match_key_unique_idx;
