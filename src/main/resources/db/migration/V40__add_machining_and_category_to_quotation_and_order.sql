-------------------------------------------------------
-- V40__add_machining_and_category_to_quotation_and_order.sql
-------------------------------------------------------

-- Add columns to quotation_items
ALTER TABLE quotation_items 
    ADD COLUMN is_machining_required BOOLEAN DEFAULT FALSE,
    ADD COLUMN metal_category VARCHAR(50);

-- Add columns to order_items
ALTER TABLE order_items 
    ADD COLUMN is_machining_required BOOLEAN DEFAULT FALSE,
    ADD COLUMN metal_category VARCHAR(50);
