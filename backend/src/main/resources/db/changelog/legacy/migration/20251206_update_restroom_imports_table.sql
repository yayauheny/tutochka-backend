-- changeset yayauheny:extend-restroom-imports-v2
ALTER TABLE restroom_imports
    ADD COLUMN IF NOT EXISTS entity_type VARCHAR(30) NOT NULL DEFAULT 'place', -- place|building|restroom
    ADD COLUMN IF NOT EXISTS external_id VARCHAR(128),
    ADD COLUMN IF NOT EXISTS source_url  TEXT,
    ADD COLUMN IF NOT EXISTS scraped_at  TIMESTAMP,
    ADD COLUMN IF NOT EXISTS payload_hash CHAR(64),
    ADD COLUMN IF NOT EXISTS attempts INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS last_attempt_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS next_retry_at TIMESTAMP;

-- дедуп “один внешний объект = одна запись в inbox”
CREATE UNIQUE INDEX IF NOT EXISTS uq_restroom_imports_provider_entity_external
    ON restroom_imports (provider, entity_type, external_id)
    WHERE external_id IS NOT NULL;

-- очередь обработки
CREATE INDEX IF NOT EXISTS idx_restroom_imports_queue
    ON restroom_imports (status, next_retry_at, created_at);

CREATE INDEX IF NOT EXISTS idx_restroom_imports_provider_city_status
    ON restroom_imports (provider, city_id, status);
