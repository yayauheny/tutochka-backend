-- liquibase formatted sql

-- changeset yayauheny:refine-analytics-storage
ALTER TABLE user_analytics
    DROP COLUMN routes_clicked_count;

ALTER TABLE analytics_events
    ADD COLUMN location geometry(Point,4326);

UPDATE analytics_events
SET location = ST_SetSRID(ST_MakePoint(lon::double precision, lat::double precision), 4326)
WHERE lat IS NOT NULL
  AND lon IS NOT NULL;

DROP INDEX IF EXISTS idx_analytics_events_coordinates_created_at;

CREATE INDEX idx_analytics_events_location
    ON analytics_events
    USING GIST (location);

ALTER TABLE analytics_events
    DROP COLUMN lat,
    DROP COLUMN lon,
    DROP COLUMN duration_ms;
