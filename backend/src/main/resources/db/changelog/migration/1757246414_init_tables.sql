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
    coordinates GEOMETRY(POINT, 4326) NOT NULL,
    is_deleted  BOOLEAN               DEFAULT false,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at  TIMESTAMP             DEFAULT NULL,
    CONSTRAINT cities_unique_country_name_ru UNIQUE (country_id, name_ru),
    CONSTRAINT cities_unique_country_name_en UNIQUE (country_id, name_en)
);
-- rollback DROP TABLE cities;

-- changeset yayauheny:init-subway-lines
CREATE TABLE subway_lines
(
    id              UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    city_id         UUID         NOT NULL REFERENCES cities (id) ON DELETE CASCADE,
    name_ru         VARCHAR(100) NOT NULL,
    name_en         VARCHAR(100) NOT NULL,
    name_local      VARCHAR(255),
    name_local_lang VARCHAR(10),
    short_code      VARCHAR(20),
    hex_color       VARCHAR(7)   NOT NULL,
    is_deleted      BOOLEAN               DEFAULT false,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at      TIMESTAMP             DEFAULT NULL,
);
-- rollback DROP TABLE subway_lines;

-- changeset yayauheny:init-subway-stations
CREATE TABLE subway_stations
(
    id              UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    subway_line_id  UUID         NOT NULL REFERENCES subway_lines (id) ON DELETE CASCADE,
    name_ru         VARCHAR(255) NOT NULL,
    name_en         VARCHAR(255) NOT NULL,
    name_local      VARCHAR(255),
    name_local_lang VARCHAR(10),
    is_transfer     BOOLEAN      NOT NULL DEFAULT false,
    external_ids    JSONB                 DEFAULT '{}'::jsonb,
    coordinates     GEOMETRY(POINT, 4326) NOT NULL,
    is_deleted      BOOLEAN               DEFAULT false,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at      TIMESTAMP             DEFAULT NULL,
);
CREATE INDEX idx_subway_stations_coordinates ON subway_stations USING GIST (coordinates);
-- rollback DROP TABLE subway_stations;

-- changeset yayauheny:init-buildings-table
CREATE TABLE buildings
(
    id            UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    city_id       UUID         NOT NULL REFERENCES cities (id) ON DELETE CASCADE,
    name          VARCHAR(255),
    address       VARCHAR(255) NOT NULL,
    building_type VARCHAR(50),
    work_time     JSONB,
    coordinates   GEOMETRY(POINT, 4326) NOT NULL,
    external_ids  JSONB                 DEFAULT '{}'::jsonb,
    import_status VARCHAR(20)  NOT NULL DEFAULT 'COMPLETED',
    is_deleted    BOOLEAN               DEFAULT false,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at    TIMESTAMP             DEFAULT NULL,
);
CREATE INDEX idx_buildings_coordinates ON buildings USING GIST (coordinates);
CREATE INDEX idx_buildings_external_ids ON buildings USING GIN (external_ids);
-- rollback DROP TABLE buildings;

-- changeset yayauheny:init-restrooms-release-v1
CREATE TABLE restrooms
(
    id                        UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    city_id                   UUID         REFERENCES cities (id) ON DELETE SET NULL,
    building_id               UUID         REFERENCES buildings (id) ON DELETE SET NULL,
    subway_station_id         UUID         REFERENCES subway_stations (id) ON DELETE SET NULL,
    name                      VARCHAR(255),
    place_type                VARCHAR(50),
    address                   VARCHAR(255) NOT NULL,
    direction_guide           TEXT,
    access_note               TEXT,
    fee_type                  VARCHAR(20)  NOT NULL,
    gender_type               VARCHAR(20)  NOT NULL DEFAULT 'UNKNOWN',
    accessibility_type        VARCHAR(20)  NOT NULL,
    status                    VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    phones                    JSONB,
    work_time                 JSONB,
    inherit_building_schedule BOOLEAN               DEFAULT false,
    amenities                 JSONB                 DEFAULT '{}'::jsonb,
    has_photos                BOOLEAN               DEFAULT false,
    coordinates               GEOMETRY(POINT, 4326) NOT NULL,
    external_maps             JSONB                 DEFAULT '{}'::jsonb,
    data_source               VARCHAR(50)  NOT NULL,
    is_deleted                BOOLEAN               DEFAULT false,
    created_at                TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at                TIMESTAMP             DEFAULT NULL
);

CREATE INDEX idx_restrooms_coordinates ON restrooms USING GIST (coordinates);
CREATE INDEX idx_restrooms_filters ON restrooms (fee_type, gender_type, accessibility_type, place_type) WHERE is_deleted = false;
CREATE INDEX idx_restrooms_building_id ON restrooms (building_id);
CREATE INDEX idx_restrooms_external_maps ON restrooms USING GIN (external_maps);
-- rollback DROP TABLE restrooms;

-- changeset yayauheny:create-geo-performance-indexes
CREATE INDEX idx_cities_bounds ON cities USING GIST (city_bounds);
CREATE INDEX idx_cities_coordinates ON cities USING GIST (coordinates);
-- rollback DROP INDEX IF EXISTS idx_cities_bounds;
-- rollback DROP INDEX IF EXISTS idx_cities_coordinates;

-- changeset yayauheny:create-status-performance-indexes
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

-- changeset yayauheny:init-restroom-imports-table
CREATE TABLE restroom_imports
(
    id            UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    provider      VARCHAR(50) NOT NULL,
    payload_type  VARCHAR(50) NOT NULL,
    city_id       UUID        REFERENCES cities (id) ON DELETE SET NULL,
    raw_payload   JSONB       NOT NULL,
    building_id   UUID        REFERENCES buildings (id) ON DELETE SET NULL,
    restroom_id   UUID        REFERENCES restrooms (id) ON DELETE SET NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'pending',
    error_message TEXT,
    created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at  TIMESTAMP            DEFAULT NULL
);
CREATE INDEX idx_restroom_imports_provider_status ON restroom_imports (provider, status);
CREATE INDEX idx_restroom_imports_restroom_id ON restroom_imports (restroom_id);
CREATE INDEX idx_restroom_imports_building_id ON restroom_imports (building_id);
-- rollback DROP TABLE restroom_imports;