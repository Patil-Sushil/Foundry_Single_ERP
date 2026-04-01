-- =====================================================
-- ADD MISSING AUDIT COLUMNS TO QA INSPECTION FINDINGS
-- =====================================================

ALTER TABLE qa_inspection_findings ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE qa_inspection_findings ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE qa_inspection_findings ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
