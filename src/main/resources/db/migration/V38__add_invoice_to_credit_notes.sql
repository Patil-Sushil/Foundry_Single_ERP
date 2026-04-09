-- ============================================
-- V38__add_invoice_to_credit_notes.sql
-- ============================================

ALTER TABLE credit_notes ADD COLUMN IF NOT EXISTS invoice_id UUID;
ALTER TABLE credit_notes ADD COLUMN IF NOT EXISTS original_invoice_number VARCHAR(50);
ALTER TABLE credit_notes ADD CONSTRAINT fk_cn_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id);
