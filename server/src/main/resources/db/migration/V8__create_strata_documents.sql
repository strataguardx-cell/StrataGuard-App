CREATE TABLE strata_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    strata_plan_id UUID REFERENCES strata_plans(id),
    doc_type VARCHAR(50) NOT NULL,
    title VARCHAR(255),
    original_filename VARCHAR(255),
    file_size_bytes BIGINT,
    ocr_status VARCHAR(20) NOT NULL DEFAULT 'pending',
    ocr_extracted_text TEXT,
    ocr_risk_flags JSONB,
    uploaded_at TIMESTAMPTZ DEFAULT now(),
    processed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_strata_documents_strata ON strata_documents(strata_plan_id);
CREATE INDEX idx_strata_documents_ocr_status ON strata_documents(ocr_status)
    WHERE ocr_status IN ('pending', 'processing');
