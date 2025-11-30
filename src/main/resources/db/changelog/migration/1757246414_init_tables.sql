-- liquibase formatted sql

-- changeset yayauheny:init-extensions
CREATE
EXTENSION IF NOT EXISTS pgcrypto;
CREATE
EXTENSION IF NOT EXISTS postgis;
CREATE
EXTENSION IF NOT EXISTS btree_gist;
-- rollback DROP EXTENSION IF EXISTS pgcrypto;
-- rollback DROP EXTENSION IF EXISTS postgis;
-- rollback DROP EXTENSION IF EXISTS btree_gist;

-- changeset yayauheny:init-countries-table
CREATE TABLE countries
(
    id         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    code       VARCHAR(10)  NOT NULL UNIQUE,
    name_ru    VARCHAR(255) NOT NULL,
    name_en    VARCHAR(255) NOT NULL,
    is_deleted BOOLEAN               DEFAULT false,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP             DEFAULT NULL
);
-- rollback DROP TABLE countries;


-- changeset yayauheny:init-cities-table
CREATE TABLE cities
(
    id          UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    country_id  UUID         NOT NULL REFERENCES countries (id) ON DELETE CASCADE,
    name_ru     VARCHAR(255) NOT NULL,
    name_en     VARCHAR(255) NOT NULL,
    region      VARCHAR(255),
    city_bounds GEOMETRY(Polygon, 4326) DEFAULT NULL,
    coordinates GEOMETRY(POINT, 4326) NOT NULL UNIQUE,
    is_deleted  BOOLEAN               DEFAULT false,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at  TIMESTAMP             DEFAULT NULL,
    CONSTRAINT cities_unique_country_name_ru UNIQUE (country_id, name_ru),
    CONSTRAINT cities_unique_country_name_en UNIQUE (country_id, name_en)
);
-- rollback DROP TABLE cities;


-- changeset yayauheny:init-restrooms-table
CREATE TABLE restrooms
(
    id                      UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    city_id                 UUID         REFERENCES cities (id) ON DELETE SET NULL,
    name                    VARCHAR(255),
    description             VARCHAR(255),
    address                 VARCHAR(255) NOT NULL,
    phones                  JSONB,
    work_time               JSONB,
    fee_type                VARCHAR(20)  NOT NULL,
    accessibility_type      VARCHAR(20)  NOT NULL,
    coordinates             GEOMETRY(POINT, 4326) NOT NULL UNIQUE,
    data_source             VARCHAR(20)  NOT NULL,
    status                  VARCHAR(20)  NOT NULL,
    amenities               JSONB                 DEFAULT '{}'::jsonb,
    parent_place_name       VARCHAR(255),
    parent_place_type       VARCHAR(50),
    inherit_parent_schedule BOOLEAN               DEFAULT false,
    is_deleted              BOOLEAN               DEFAULT false,
    created_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at              TIMESTAMP             DEFAULT NULL
);
-- rollback DROP TABLE restrooms;


-- changeset yayauheny:create-restrooms-coordinates-index
CREATE INDEX idx_restrooms_coordinates ON restrooms USING GIST (coordinates);
CREATE INDEX idx_cities_bounds ON cities USING GIST(city_bounds);
CREATE INDEX idx_cities_coordinates ON cities USING GIST (coordinates);
-- rollback DROP INDEX IF EXISTS idx_restrooms_coordinates;
-- rollback DROP INDEX IF EXISTS idx_cities_bounds;
-- rollback DROP INDEX IF EXISTS idx_cities_coordinates;

-- changeset yayauheny:create-performance-indexes
CREATE INDEX idx_restrooms_status ON restrooms (status) WHERE is_deleted = false;
CREATE INDEX idx_restrooms_city_id ON restrooms (city_id) WHERE is_deleted = false;
CREATE INDEX idx_restrooms_is_deleted ON restrooms (is_deleted);
CREATE INDEX idx_cities_is_deleted ON cities (is_deleted);
CREATE INDEX idx_countries_is_deleted ON countries (is_deleted);
CREATE INDEX idx_cities_country_id ON cities (country_id) WHERE is_deleted = false;
-- rollback DROP INDEX IF EXISTS idx_restrooms_status;
-- rollback DROP INDEX IF EXISTS idx_restrooms_city_id;
-- rollback DROP INDEX IF EXISTS idx_restrooms_is_deleted;
-- rollback DROP INDEX IF EXISTS idx_cities_is_deleted;
-- rollback DROP INDEX IF EXISTS idx_countries_is_deleted;
-- rollback DROP INDEX IF EXISTS idx_cities_country_id;
