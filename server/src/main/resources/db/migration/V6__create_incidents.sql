CREATE TABLE incidents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    strata_plan_id UUID REFERENCES strata_plans(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    incident_date DATE NOT NULL,
    category VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'open',
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    deleted_at TIMESTAMPTZ
);

CREATE TABLE incident_evidence (
    incident_id UUID NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
    evidence_id UUID NOT NULL REFERENCES evidence_items(id) ON DELETE CASCADE,
    PRIMARY KEY (incident_id, evidence_id)
);

CREATE INDEX idx_incidents_user ON incidents(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_incidents_strata ON incidents(strata_plan_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_incidents_date ON incidents(incident_date DESC) WHERE deleted_at IS NULL;
