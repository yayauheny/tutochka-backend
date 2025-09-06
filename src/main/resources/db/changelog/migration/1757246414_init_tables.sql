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
    name_ru VARCHAR(255) NOT NULL UNIQUE,
    name_en VARCHAR(255) NOT NULL UNIQUE
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
    lat        DOUBLE PRECISION,
    lon        DOUBLE PRECISION,
    CONSTRAINT cities_unique_country_name UNIQUE (country_id, name)
);
-- rollback DROP TABLE cities;


-- changeset yayauheny:init-restrooms-table
CREATE TABLE restrooms
(
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    city_id            UUID                   REFERENCES cities (id) ON DELETE SET NULL,
    code               VARCHAR(255)           NOT NULL,
    description        VARCHAR(255),
    name               VARCHAR(255),
    work_time          VARCHAR(255),
    fee_type           VARCHAR(20)            NOT NULL,
    accessibility_type VARCHAR(20)            NOT NULL,
    coordinates        GEOGRAPHY(Point, 4326) NOT NULL,
    data_source        VARCHAR(50)            NOT NULL,
    amenities          JSONB            DEFAULT '[]'::jsonb,
    created_at         TIMESTAMP              NOT NULL,
    updated_at         TIMESTAMP              NOT NULL,
    CONSTRAINT restrooms_unique_code_in_city UNIQUE (city_id, code)
);
-- rollback DROP TABLE restrooms;


-- changeset yayauheny:create-restrooms-coordinates-index
CREATE INDEX idx_restrooms_coordinates ON restrooms USING GIST (coordinates);
-- rollback DROP INDEX IF EXISTS idx_restrooms_coordinates;


-- changeset yayauheny:create-restrooms-city-id-index
CREATE INDEX idx_restrooms_city_id ON restrooms (city_id);
-- rollback DROP INDEX IF EXISTS idx_restrooms_city_id;


-- changeset yayauheny:create-restrooms-fee-type-index
CREATE INDEX idx_restrooms_fee_type ON restrooms (fee_type);
-- rollback DROP INDEX IF EXISTS idx_restrooms_fee_type;


-- changeset yayauheny:create-restrooms-accessibility-type-index
CREATE INDEX idx_restrooms_accessibility ON restrooms (accessibility_type);
-- rollback DROP INDEX IF EXISTS idx_restrooms_accessibility;


-- changeset yayauheny:create-cities-country-id-index
CREATE INDEX idx_cities_country_id ON cities (country_id);
-- rollback DROP INDEX IF EXISTS idx_cities_country_id;