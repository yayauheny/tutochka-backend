-- liquibase formatted sql

-- changeset yayauheny:fix-building-match-key-unique-index
DROP INDEX IF EXISTS uq_buildings_match_key;
CREATE UNIQUE INDEX IF NOT EXISTS uq_buildings_match_key
    ON buildings (building_match_key);
-- rollback DROP INDEX IF EXISTS uq_buildings_match_key;
