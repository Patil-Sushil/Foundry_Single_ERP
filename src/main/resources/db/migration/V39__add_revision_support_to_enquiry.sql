-------------------------------------------------------
-- V39__add_revision_support_to_enquiry.sql
-------------------------------------------------------

-- Add revision columns to enquiry table
ALTER TABLE enquiry 
    ADD COLUMN revision_no INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN revision_note TEXT;

-- Drop existing unique constraint on enquiry_no if it exists (it was defined as UNIQUE in V3)
-- In V3: enquiry_no VARCHAR(50) NOT NULL UNIQUE
ALTER TABLE enquiry DROP CONSTRAINT IF EXISTS enquiry_enquiry_no_key;

-- Add new unique constraint on (enquiry_no, revision_no)
-- Since we are currently only incrementing on the same row, 
-- this constraint might not be strictly necessary if we only ever have one row per enquiry_no,
-- but the requirement implies we might want multiple rows in the future or 
-- at least we want to ensure this combination is unique.
-- However, if we only update the same row, enquiry_no itself remains unique.
-- The requirement said: "Add unique constraint: UNIQUE(enquiry_no, revision_no)"
ALTER TABLE enquiry ADD CONSTRAINT uk_enquiry_no_revision UNIQUE (enquiry_no, revision_no);

-- Update status check constraint to include REVISED if we decide to use it.
-- The prompt said: "Update status column to support new value REVISED (if using enum string)"
ALTER TABLE enquiry DROP CONSTRAINT IF EXISTS chk_enquiry_status;
ALTER TABLE enquiry ADD CONSTRAINT chk_enquiry_status 
    CHECK (status IN ('PENDING', 'REVISED', 'QUOTED', 'CLOSED'));
