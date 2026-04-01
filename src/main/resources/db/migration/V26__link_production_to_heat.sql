-- =====================================================
-- V27: Link Production Module to Heat Module
-- =====================================================

ALTER TABLE production_items ADD COLUMN heat_order_item_id BIGINT;
ALTER TABLE production_items ADD CONSTRAINT fk_prod_item_heat_order_item 
    FOREIGN KEY (heat_order_item_id) REFERENCES heat_order_items(id);

COMMENT ON COLUMN production_items.heat_order_item_id IS 
'Optional link to specific heat_order_item record for material reconciliation.';
