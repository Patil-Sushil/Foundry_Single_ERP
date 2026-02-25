-- V12: Add missing audit columns to vendor_ledger
-- =============================================

ALTER TABLE vendor_ledger 
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT NOW(),
ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
