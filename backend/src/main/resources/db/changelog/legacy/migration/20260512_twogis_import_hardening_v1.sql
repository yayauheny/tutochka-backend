ALTER TABLE buildings
    ADD COLUMN IF NOT EXISTS building_match_key VARCHAR(64);

CREATE UNIQUE INDEX IF NOT EXISTS uq_buildings_match_key
    ON buildings (building_match_key)
    WHERE building_match_key IS NOT NULL;

ALTER TABLE restrooms
    ADD COLUMN IF NOT EXISTS restroom_match_key VARCHAR(64);

CREATE INDEX IF NOT EXISTS idx_restrooms_match_key
    ON restrooms (restroom_match_key)
    WHERE restroom_match_key IS NOT NULL;

CREATE TABLE IF NOT EXISTS restroom_duplicate_suspicions
(
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    existing_restroom_id UUID        NOT NULL REFERENCES restrooms (id) ON DELETE CASCADE,
    candidate_restroom_id UUID       NOT NULL REFERENCES restrooms (id) ON DELETE CASCADE,
    distance_m           DOUBLE PRECISION NOT NULL,
    reason               VARCHAR(100) NOT NULL,
    status               VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    provider             VARCHAR(50)  NOT NULL,
    external_id          VARCHAR(128),
    created_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_restroom_duplicate_suspicions_existing
    ON restroom_duplicate_suspicions (existing_restroom_id);

CREATE INDEX IF NOT EXISTS idx_restroom_duplicate_suspicions_candidate
    ON restroom_duplicate_suspicions (candidate_restroom_id);

