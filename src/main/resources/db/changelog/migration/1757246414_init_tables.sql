-- liquibase formatted sql

-- changeset yayauheny:init-extensions
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS postgis;
-- rollback DROP EXTENSION IF EXISTS postgis;
-- rollback DROP EXTENSION IF EXISTS pgcrypto;


-- changeset yayauheny:init-countries-table
CREATE TABLE countries
(
    id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code    VARCHAR(10)  NOT NULL UNIQUE,
    name_ru VARCHAR(255) NOT NULL,
    name_en VARCHAR(255) NOT NULL
);
-- rollback DROP TABLE countries;


-- changeset yayauheny:init-cities-table
CREATE TABLE cities
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    country_id UUID         NOT NULL REFERENCES countries (id) ON DELETE CASCADE,
    name_ru    VARCHAR(255) NOT NULL,
    name_en    VARCHAR(255) NOT NULL,
    region     VARCHAR(255),
    lat        DOUBLE PRECISION NOT NULL,
    lon        DOUBLE PRECISION NOT NULL,
    CONSTRAINT cities_unique_country_name_ru UNIQUE (country_id, name_ru),
    CONSTRAINT cities_unique_country_name_en UNIQUE (country_id, name_en)
);
-- rollback DROP TABLE cities;


-- changeset yayauheny:init-restrooms-table
CREATE TABLE restrooms
(
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    city_id            UUID                   REFERENCES cities (id) ON DELETE SET NULL,
    name               VARCHAR(255),
    description        VARCHAR(255),
    address            VARCHAR(255)           NOT NULL,
    phones             JSONB,
    work_time          JSONB,
    fee_type           VARCHAR(20)            NOT NULL,
    accessibility_type VARCHAR(20)            NOT NULL,
    coordinates        GEOGRAPHY(Point, 4326) NOT NULL,
    data_source        VARCHAR(20)            NOT NULL,
    status             VARCHAR(20)            NOT NULL,
    amenities          JSONB            DEFAULT '{}'::jsonb,
    created_at         TIMESTAMP              NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP              NOT NULL DEFAULT CURRENT_TIMESTAMP
);
-- rollback DROP TABLE restrooms;


-- changeset yayauheny:create-restrooms-coordinates-index
CREATE INDEX idx_restrooms_coordinates ON restrooms USING GIST (coordinates);
-- rollback DROP INDEX IF EXISTS idx_restrooms_coordinates;
