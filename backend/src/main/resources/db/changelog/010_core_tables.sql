CREATE TABLE countries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(10) NOT NULL,
    name_ru TEXT NOT NULL,
    name_en TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Stable country code for lookup, deduplication, and CSV upserts.
CREATE UNIQUE INDEX idx_countries_code ON countries (code);

CREATE TABLE cities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    country_id UUID NOT NULL,
    name_ru TEXT NOT NULL,
    name_en TEXT NOT NULL,
    slug TEXT NOT NULL,
    region TEXT,
    coordinates geometry(Point, 4326) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE cities
    ADD CONSTRAINT fk_cities_country
        FOREIGN KEY (country_id) REFERENCES countries (id) ON DELETE CASCADE;

-- Stable city slug and nearest-city resolution during import and location search.
CREATE UNIQUE INDEX idx_cities_slug ON cities (slug);
CREATE INDEX idx_cities_coordinates ON cities USING GIST (coordinates);

CREATE TABLE subway_lines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    city_id UUID NOT NULL,
    code TEXT NOT NULL,
    name_ru TEXT NOT NULL,
    name_en TEXT NOT NULL,
    hex_color TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE subway_lines
    ADD CONSTRAINT fk_subway_lines_city
        FOREIGN KEY (city_id) REFERENCES cities (id) ON DELETE RESTRICT;

-- Stable line code within a city for import idempotency.
CREATE UNIQUE INDEX idx_subway_lines_city_code ON subway_lines (city_id, code);

CREATE TABLE subway_stations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subway_line_id UUID NOT NULL,
    line_order INT NOT NULL,
    slug TEXT NOT NULL,
    name_ru TEXT NOT NULL,
    name_en TEXT NOT NULL,
    is_transfer BOOLEAN NOT NULL DEFAULT false,
    coordinates geometry(Point, 4326) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE subway_stations
    ADD CONSTRAINT fk_subway_stations_line
        FOREIGN KEY (subway_line_id) REFERENCES subway_lines (id) ON DELETE CASCADE;

-- Station slug and line order stay stable inside one subway line.
CREATE UNIQUE INDEX idx_subway_stations_line_slug ON subway_stations (subway_line_id, slug);
CREATE UNIQUE INDEX idx_subway_stations_line_order ON subway_stations (subway_line_id, line_order);

CREATE TABLE buildings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    city_id UUID NOT NULL,
    name TEXT,
    address TEXT NOT NULL,
    building_type TEXT,
    work_time JSONB,
    coordinates geometry(Point, 4326) NOT NULL,
    two_gis_id TEXT,
    yandex_id TEXT,
    google_id TEXT,
    apple_id TEXT,
    building_match_key TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE buildings
    ADD CONSTRAINT fk_buildings_city
        FOREIGN KEY (city_id) REFERENCES cities (id) ON DELETE RESTRICT;

-- Provider ids and match key enforce deduplication across import sources.
CREATE UNIQUE INDEX idx_buildings_two_gis_id ON buildings (two_gis_id) WHERE two_gis_id IS NOT NULL;
CREATE UNIQUE INDEX idx_buildings_yandex_id ON buildings (yandex_id) WHERE yandex_id IS NOT NULL;
CREATE UNIQUE INDEX idx_buildings_google_id ON buildings (google_id) WHERE google_id IS NOT NULL;
CREATE UNIQUE INDEX idx_buildings_apple_id ON buildings (apple_id) WHERE apple_id IS NOT NULL;
CREATE UNIQUE INDEX idx_buildings_match_key ON buildings (building_match_key) WHERE building_match_key IS NOT NULL;

CREATE TABLE restrooms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    city_id UUID NOT NULL,
    building_id UUID,
    subway_station_id UUID,
    created_by_user_id UUID,
    updated_by_user_id UUID,
    name TEXT,
    place_type TEXT NOT NULL,
    address TEXT,
    direction_guide TEXT,
    access_note TEXT,
    fee_type TEXT NOT NULL,
    gender_type TEXT NOT NULL,
    accessibility_type TEXT NOT NULL,
    status TEXT NOT NULL,
    moderation_status TEXT NOT NULL DEFAULT 'PENDING',
    phones TEXT,
    work_time JSONB,
    inherit_building_schedule BOOLEAN NOT NULL DEFAULT false,
    amenities JSONB,
    has_photos BOOLEAN NOT NULL DEFAULT false,
    coordinates geometry(Point, 4326) NOT NULL,
    data_source TEXT NOT NULL,
    location_type TEXT NOT NULL,
    is_hidden BOOLEAN NOT NULL DEFAULT false,
    origin_provider TEXT NOT NULL,
    origin_id TEXT,
    two_gis_id TEXT,
    yandex_id TEXT,
    google_id TEXT,
    apple_id TEXT,
    restroom_match_key TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE restrooms
    ADD CONSTRAINT fk_restrooms_city
        FOREIGN KEY (city_id) REFERENCES cities (id) ON DELETE RESTRICT,
    ADD CONSTRAINT fk_restrooms_building
        FOREIGN KEY (building_id) REFERENCES buildings (id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_restrooms_subway_station
        FOREIGN KEY (subway_station_id) REFERENCES subway_stations (id) ON DELETE SET NULL;

-- Hot path for nearest restroom search and import/provider deduplication.
CREATE INDEX idx_restrooms_visible_coordinates ON restrooms USING GIST (coordinates) WHERE status = 'ACTIVE' AND is_hidden = false;
CREATE UNIQUE INDEX idx_restrooms_two_gis_id ON restrooms (two_gis_id) WHERE two_gis_id IS NOT NULL;
CREATE UNIQUE INDEX idx_restrooms_yandex_id ON restrooms (yandex_id) WHERE yandex_id IS NOT NULL;
CREATE UNIQUE INDEX idx_restrooms_google_id ON restrooms (google_id) WHERE google_id IS NOT NULL;
CREATE UNIQUE INDEX idx_restrooms_apple_id ON restrooms (apple_id) WHERE apple_id IS NOT NULL;
CREATE UNIQUE INDEX idx_restrooms_origin_provider_origin_id ON restrooms (origin_provider, origin_id) WHERE origin_id IS NOT NULL;
CREATE UNIQUE INDEX idx_restrooms_match_key ON restrooms (restroom_match_key) WHERE restroom_match_key IS NOT NULL;

CREATE TABLE restroom_imports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider TEXT NOT NULL,
    payload_type TEXT NOT NULL,
    entity_type TEXT NOT NULL,
    external_id TEXT,
    source_url TEXT,
    scraped_at TIMESTAMPTZ,
    payload_hash TEXT,
    raw_payload JSONB NOT NULL,
    city_id UUID,
    building_id UUID,
    restroom_id UUID,
    provider_reference TEXT,
    city_slug TEXT,
    resolved_building_provider_id TEXT,
    resolved_restroom_origin_key TEXT,
    status TEXT NOT NULL,
    error_message TEXT,
    attempts INT NOT NULL DEFAULT 0,
    last_attempt_at TIMESTAMPTZ,
    next_retry_at TIMESTAMPTZ,
    processed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE restroom_imports
    ADD CONSTRAINT fk_restroom_imports_city
        FOREIGN KEY (city_id) REFERENCES cities (id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_restroom_imports_building
        FOREIGN KEY (building_id) REFERENCES buildings (id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_restroom_imports_restroom
        FOREIGN KEY (restroom_id) REFERENCES restrooms (id) ON DELETE SET NULL;

-- Import pipeline upsert key for source/entity/external-id triples.
CREATE UNIQUE INDEX idx_restroom_imports_provider_entity_external ON restroom_imports (provider, entity_type, external_id) WHERE external_id IS NOT NULL;

CREATE TABLE restroom_duplicate_suspicions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    existing_restroom_id UUID NOT NULL,
    candidate_restroom_id UUID NOT NULL,
    distance_m DOUBLE PRECISION NOT NULL,
    reason TEXT NOT NULL,
    status TEXT NOT NULL,
    provider TEXT NOT NULL,
    external_id TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE restroom_duplicate_suspicions
    ADD CONSTRAINT fk_restroom_duplicate_suspicions_existing
        FOREIGN KEY (existing_restroom_id) REFERENCES restrooms (id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_restroom_duplicate_suspicions_candidate
        FOREIGN KEY (candidate_restroom_id) REFERENCES restrooms (id) ON DELETE CASCADE;
