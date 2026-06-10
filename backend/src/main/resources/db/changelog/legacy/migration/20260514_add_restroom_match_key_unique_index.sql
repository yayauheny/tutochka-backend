-- changeset yayauheny:add-restroom-match-key-unique-index
CREATE UNIQUE INDEX CONCURRENTLY IF NOT EXISTS restrooms_restroom_match_key_unique_idx
    ON restrooms (restroom_match_key)
    WHERE restroom_match_key IS NOT NULL;
