CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tg_user_id TEXT NOT NULL,
    username TEXT,
    source TEXT NOT NULL,
    acquisition_tag TEXT NOT NULL DEFAULT 'direct',
    is_blocked BOOLEAN NOT NULL DEFAULT false,
    account_status TEXT NOT NULL DEFAULT 'ACTIVE',
    last_seen_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Required for tg_user_id upsert and user identity deduplication.
CREATE UNIQUE INDEX idx_users_tg_user_id ON users (tg_user_id);

CREATE TABLE user_privacy_consents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    consent_type TEXT NOT NULL,
    consent_status TEXT NOT NULL,
    source TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE user_privacy_consents
    ADD CONSTRAINT fk_user_privacy_consents_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

CREATE TABLE user_data_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    request_type TEXT NOT NULL,
    status TEXT NOT NULL,
    requested_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE user_data_requests
    ADD CONSTRAINT fk_user_data_requests_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
