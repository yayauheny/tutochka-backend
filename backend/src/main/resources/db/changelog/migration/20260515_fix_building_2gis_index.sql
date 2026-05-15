-- liquibase formatted sql

-- changeset yayauheny:fix-building-2gis-index
DROP INDEX IF EXISTS uq_buildings_2gis;
CREATE INDEX IF NOT EXISTS idx_buildings_2gis
    ON buildings ((external_ids->>'2gis'));
-- rollback DROP INDEX IF EXISTS idx_buildings_2gis;
