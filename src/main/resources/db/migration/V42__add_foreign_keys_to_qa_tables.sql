-- =============================================================================
-- V42: Add Foreign Keys to QA Tables
-- Prevents orphaned records that cause FetchNotFoundException in Hibernate
-- =============================================================================

-- 1. CLEANUP: Delete orphaned inspections before adding constraints
-- (This ensures the migration succeeds even if orphans already exist)
DELETE FROM qa_inspections 
WHERE production_item_id NOT IN (SELECT id FROM production_items)
   OR production_entry_id NOT IN (SELECT id FROM production_entries)
   OR order_id NOT IN (SELECT id FROM orders)
   OR order_item_id NOT IN (SELECT id FROM order_items);

-- 2. Add Foreign Keys to qa_inspections
ALTER TABLE qa_inspections
    ADD CONSTRAINT fk_qa_insp_prod_entry
    FOREIGN KEY (production_entry_id) REFERENCES production_entries(id);

ALTER TABLE qa_inspections
    ADD CONSTRAINT fk_qa_insp_prod_item
    FOREIGN KEY (production_item_id) REFERENCES production_items(id);

ALTER TABLE qa_inspections
    ADD CONSTRAINT fk_qa_insp_order
    FOREIGN KEY (order_id) REFERENCES orders(id);

ALTER TABLE qa_inspections
    ADD CONSTRAINT fk_qa_insp_order_item
    FOREIGN KEY (order_item_id) REFERENCES order_items(id);

-- 3. CLEANUP: Delete orphaned rejections
DELETE FROM qa_rejections
WHERE production_item_id NOT IN (SELECT id FROM production_items)
   OR production_entry_id NOT IN (SELECT id FROM production_entries)
   OR order_id NOT IN (SELECT id FROM orders)
   OR order_item_id NOT IN (SELECT id FROM order_items);

-- 4. Add Foreign Keys to qa_rejections
ALTER TABLE qa_rejections
    ADD CONSTRAINT fk_qa_rej_prod_entry
    FOREIGN KEY (production_entry_id) REFERENCES production_entries(id);

ALTER TABLE qa_rejections
    ADD CONSTRAINT fk_qa_rej_prod_item
    FOREIGN KEY (production_item_id) REFERENCES production_items(id);

ALTER TABLE qa_rejections
    ADD CONSTRAINT fk_qa_rej_order
    FOREIGN KEY (order_id) REFERENCES orders(id);

ALTER TABLE qa_rejections
    ADD CONSTRAINT fk_qa_rej_order_item
    FOREIGN KEY (order_item_id) REFERENCES order_items(id);

-- 5. Add Foreign Keys to qa_customer_returns (optional but recommended)
ALTER TABLE qa_customer_returns
    ADD CONSTRAINT fk_qa_ret_order
    FOREIGN KEY (order_id) REFERENCES orders(id);

ALTER TABLE qa_customer_returns
    ADD CONSTRAINT fk_qa_ret_order_item
    FOREIGN KEY (order_item_id) REFERENCES order_items(id);
