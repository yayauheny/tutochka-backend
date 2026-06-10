-- liquibase formatted sql

-- changeset yayauheny:fix-building-external-id-indexes
DROP INDEX IF EXISTS uq_buildings_2gis;
CREATE INDEX IF NOT EXISTS idx_buildings_2gis
    ON buildings ((external_ids->>'2gis'));
CREATE INDEX IF NOT EXISTS idx_buildings_yandex
    ON buildings ((external_ids->>'yandex'));
-- rollback DROP INDEX IF EXISTS idx_buildings_yandex;
-- rollback DROP INDEX IF EXISTS idx_buildings_2gis;
