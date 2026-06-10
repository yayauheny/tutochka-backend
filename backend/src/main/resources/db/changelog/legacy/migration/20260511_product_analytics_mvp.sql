-- liquibase formatted sql

-- changeset yayauheny:create-users-table
CREATE TABLE users
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tg_user_id TEXT NOT NULL UNIQUE,
    tg_chat_id TEXT NOT NULL,

    source TEXT NOT NULL DEFAULT 'telegram_bot',

    username TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    is_blocked BOOLEAN NOT NULL DEFAULT false,
    blocked_at TIMESTAMPTZ
);

CREATE INDEX idx_users_created_at
    ON users (created_at);

CREATE INDEX idx_users_updated_at
    ON users (updated_at);
-- rollback DROP TABLE users;

-- changeset yayauheny:create-user-analytics-table
CREATE TABLE user_analytics
(
    user_id UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,

    searches_count INTEGER NOT NULL DEFAULT 0,
    successful_searches_count INTEGER NOT NULL DEFAULT 0,
    empty_searches_count INTEGER NOT NULL DEFAULT 0,

    details_opened_count INTEGER NOT NULL DEFAULT 0,
    routes_clicked_count INTEGER NOT NULL DEFAULT 0,

    last_event TEXT,
    last_event_at TIMESTAMPTZ
);

CREATE INDEX idx_user_analytics_searches_count
    ON user_analytics (searches_count DESC);

CREATE INDEX idx_user_analytics_last_event_at
    ON user_analytics (last_event_at);
-- rollback DROP TABLE user_analytics;

-- changeset yayauheny:create-analytics-events-table
CREATE TABLE analytics_events
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    event TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    user_id UUID REFERENCES users (id) ON DELETE SET NULL,

    source TEXT NOT NULL DEFAULT 'telegram_bot',

    lat NUMERIC(5, 2),
    lon NUMERIC(5, 2),

    results_count INTEGER,
    duration_ms INTEGER,

    metadata JSONB NOT NULL DEFAULT '{}'::jsonb
);

CREATE INDEX idx_analytics_events_created_at
    ON analytics_events (created_at);

CREATE INDEX idx_analytics_events_event_created_at
    ON analytics_events (event, created_at);

CREATE INDEX idx_analytics_events_user_created_at
    ON analytics_events (user_id, created_at);

CREATE INDEX idx_analytics_events_coordinates_created_at
    ON analytics_events (lat, lon, created_at);
-- rollback DROP TABLE analytics_events;
