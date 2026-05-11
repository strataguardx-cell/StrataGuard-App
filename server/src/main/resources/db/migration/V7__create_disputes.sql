CREATE TABLE disputes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    strata_plan_id UUID REFERENCES strata_plans(id),
    state VARCHAR(3) NOT NULL,
    tribunal VARCHAR(20) NOT NULL,
    dispute_type VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'draft',
    risk_score DECIMAL(3,2),
    risk_factors JSONB,
    filing_deadline DATE,
    hearing_date DATE,
    outcome VARCHAR(30),
    notes TEXT,
    pdf_s3_key VARCHAR(500),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    deleted_at TIMESTAMPTZ
);

CREATE TABLE dispute_incidents (
    dispute_id UUID NOT NULL REFERENCES disputes(id) ON DELETE CASCADE,
    incident_id UUID NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
    PRIMARY KEY (dispute_id, incident_id)
);

CREATE INDEX idx_disputes_user ON disputes(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_disputes_strata ON disputes(strata_plan_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_disputes_status ON disputes(status) WHERE deleted_at IS NULL;
