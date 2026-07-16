-- Phase 2: Authentication & User Management
-- Extends the Phase 1 foundation (V1 is never edited) with authentication,
-- profile, and refresh-token storage.

-- Extend app_users with authentication and profile columns.
ALTER TABLE app_users
    ADD COLUMN password_hash      VARCHAR(255),
    ADD COLUMN role               VARCHAR(32) NOT NULL DEFAULT 'USER',
    ADD COLUMN account_status     VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN preferred_currency VARCHAR(8)  NOT NULL DEFAULT 'USD',
    ADD COLUMN timezone           VARCHAR(64),
    ADD COLUMN avatar_url         VARCHAR(512),
    ADD COLUMN last_login_at      TIMESTAMPTZ;

-- Refresh tokens are stored hashed (SHA-256 of the issued raw token). The raw
-- value is returned to the client exactly once and never persisted.
CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES app_users (id) ON DELETE CASCADE,
    token_hash  VARCHAR(255) NOT NULL,
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked     BOOLEAN NOT NULL DEFAULT FALSE,
    replaced_by UUID,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens (token_hash);
