-- ============================================
-- V37__add_credit_note_to_customer_returns.sql
-- ============================================

ALTER TABLE qa_customer_returns ADD COLUMN IF NOT EXISTS credit_note_id UUID;
ALTER TABLE qa_customer_returns ADD CONSTRAINT fk_qa_returns_credit_note FOREIGN KEY (credit_note_id) REFERENCES credit_notes(id);
