CREATE TABLE building_defects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    strata_plan_id UUID NOT NULL REFERENCES strata_plans(id) ON DELETE CASCADE,
    category VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    severity VARCHAR(20) NOT NULL DEFAULT 'minor',
    order_type VARCHAR(30),
    reported_date DATE,
    resolved_date DATE,
    resolution_notes TEXT,
    source_document VARCHAR(255),
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_building_defects_strata ON building_defects(strata_plan_id);
CREATE INDEX idx_building_defects_severity ON building_defects(severity);
