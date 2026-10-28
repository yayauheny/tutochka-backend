CREATE TABLE analytics_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    user_id UUID,
    source TEXT NOT NULL,
    location geometry(Point, 4326),
    results_count INT,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb
);

ALTER TABLE analytics_events
    ADD CONSTRAINT fk_analytics_events_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL;

CREATE TABLE analytics_aggregates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_date DATE NOT NULL,
    city_id UUID,
    event_type TEXT NOT NULL,
    event_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE analytics_aggregates
    ADD CONSTRAINT fk_analytics_aggregates_city
        FOREIGN KEY (city_id) REFERENCES cities (id) ON DELETE SET NULL;
