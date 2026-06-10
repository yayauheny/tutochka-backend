ALTER TABLE restroom_imports
    ALTER COLUMN payload_hash TYPE VARCHAR(64)
    USING payload_hash::VARCHAR(64);
