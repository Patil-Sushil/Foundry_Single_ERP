-- =====================================================
-- MODIFY PRODUCTION TABLES FOR QA
-- =====================================================

ALTER TABLE production_items ADD COLUMN IF NOT EXISTS inspected_quantity INTEGER NOT NULL DEFAULT 0;
ALTER TABLE production_items ADD COLUMN IF NOT EXISTS accepted_quantity INTEGER NOT NULL DEFAULT 0;
ALTER TABLE production_items ADD COLUMN IF NOT EXISTS rejected_quantity INTEGER NOT NULL DEFAULT 0;
ALTER TABLE production_items ADD COLUMN IF NOT EXISTS rework_quantity     INTEGER NOT NULL DEFAULT 0;

ALTER TABLE production_entries ADD COLUMN IF NOT EXISTS total_inspected_quantity INTEGER NOT NULL DEFAULT 0;
ALTER TABLE production_entries ADD COLUMN IF NOT EXISTS total_accepted_quantity  INTEGER NOT NULL DEFAULT 0;
ALTER TABLE production_entries ADD COLUMN IF NOT EXISTS total_rejected_quantity  INTEGER NOT NULL DEFAULT 0;
ALTER TABLE production_entries ADD COLUMN IF NOT EXISTS total_rework_quantity    INTEGER NOT NULL DEFAULT 0;

-- =====================================================
-- MODIFY SCRAP TABLES FOR QA
-- =====================================================

ALTER TABLE scrap_entries ADD COLUMN IF NOT EXISTS qa_rejection_id   BIGINT;
ALTER TABLE scrap_entries ADD COLUMN IF NOT EXISTS customer_return_id BIGINT;
ALTER TABLE scrap_entries ADD COLUMN IF NOT EXISTS rejection_number  VARCHAR(50);
ALTER TABLE scrap_entries ADD COLUMN IF NOT EXISTS return_number     VARCHAR(50);

CREATE INDEX IF NOT EXISTS idx_scrap_qa_rejection ON scrap_entries(qa_rejection_id);
CREATE INDEX IF NOT EXISTS idx_scrap_customer_return ON scrap_entries(customer_return_id);

-- =====================================================
-- INDEX FOR PENDING QA
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_prod_items_pending_qa ON production_items(order_item_id)
WHERE fettling_quantity > 0 AND inspected_quantity < fettling_quantity AND is_deleted = false;
