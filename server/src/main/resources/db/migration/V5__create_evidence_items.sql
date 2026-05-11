CREATE TABLE evidence_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    strata_plan_id UUID REFERENCES strata_plans(id),
    type VARCHAR(20) NOT NULL DEFAULT 'photo',
    title VARCHAR(255),
    description TEXT,
    s3_key VARCHAR(500),
    captured_at TIMESTAMPTZ NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    -- AI detection (Layer 1: on-device EXIF; Layer 2+: server-side, post-MVP)
    ai_detection_status VARCHAR(20) NOT NULL DEFAULT 'pending',
    ai_detection_score DECIMAL(5,4),
    ai_detection_verdict VARCHAR(20),
    ai_detection_flags JSONB,
    ai_detection_model VARCHAR(50),
    ai_detected_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT now(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX idx_evidence_user ON evidence_items(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_evidence_strata ON evidence_items(strata_plan_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_evidence_captured ON evidence_items(captured_at DESC) WHERE deleted_at IS NULL;
