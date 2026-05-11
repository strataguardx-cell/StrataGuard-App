CREATE TABLE strata_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plan_number VARCHAR(50) UNIQUE NOT NULL,
    address TEXT NOT NULL,
    suburb VARCHAR(100),
    state VARCHAR(3) NOT NULL,
    postcode VARCHAR(4),
    total_lots INT,
    year_built INT,
    registration_date VARCHAR(50),
    managing_agent VARCHAR(255),
    managing_agent_licence VARCHAR(50),
    last_agm VARCHAR(50),
    building_class VARCHAR(20),
    sinking_fund_status VARCHAR(20) DEFAULT 'unknown',
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    data_source VARCHAR(20) NOT NULL DEFAULT 'seed',
    contributed_by UUID REFERENCES users(id),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_strata_plans_plan_number ON strata_plans(plan_number);
CREATE INDEX idx_strata_plans_state ON strata_plans(state);
CREATE INDEX idx_strata_plans_suburb ON strata_plans(LOWER(suburb));
CREATE INDEX idx_strata_plans_postcode ON strata_plans(postcode);
CREATE INDEX idx_strata_plans_address_fts
    ON strata_plans USING GIN(to_tsvector('english', address || ' ' || COALESCE(suburb, '')));
