-- V32: Add GST and Tax columns to Purchase Order module
-- =====================================================

-- 1. Add state to vendors
ALTER TABLE vendors ADD COLUMN state VARCHAR(100);

-- 2. Add tax fields to purchase_orders
ALTER TABLE purchase_orders ADD COLUMN total_taxable_amount DECIMAL(15,2) DEFAULT 0;
ALTER TABLE purchase_orders ADD COLUMN total_tax_amount DECIMAL(15,2) DEFAULT 0;
ALTER TABLE purchase_orders ADD COLUMN grand_total DECIMAL(15,2) DEFAULT 0;

-- 3. Add tax fields to purchase_order_items
ALTER TABLE purchase_order_items ADD COLUMN gst_rate DECIMAL(5,2);
ALTER TABLE purchase_order_items ADD COLUMN hsn_code VARCHAR(20);
ALTER TABLE purchase_order_items ADD COLUMN tax_amount DECIMAL(15,2);

-- Comment for documentation
COMMENT ON COLUMN vendors.state IS 'State of the vendor for GST calculation (e.g., Maharashtra)';
COMMENT ON COLUMN purchase_order_items.gst_rate IS 'GST percentage applied to this item';
COMMENT ON COLUMN purchase_order_items.tax_amount IS 'Calculated tax amount for this item line';
