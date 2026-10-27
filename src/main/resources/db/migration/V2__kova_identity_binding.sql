-- Adopt an existing Notes user without changing any notebook, page or attachment.
-- Deployment verifies the personal owner before inserting the one-to-one binding.
CREATE TABLE kova_identity_binding (
    kova_subject UUID PRIMARY KEY,
    legacy_user_id UUID NOT NULL UNIQUE REFERENCES users(user_id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
