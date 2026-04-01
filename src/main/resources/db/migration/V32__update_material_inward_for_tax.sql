-- V33: Update Material Inward for Tax and GST
-- =====================================================

-- 1. Add tax fields to material_inwards
ALTER TABLE material_inwards ADD COLUMN total_taxable_amount DECIMAL(15,2) DEFAULT 0;
ALTER TABLE material_inwards ADD COLUMN total_tax_amount DECIMAL(15,2) DEFAULT 0;
ALTER TABLE material_inwards ADD COLUMN grand_total DECIMAL(15,2) DEFAULT 0;

-- 2. Modify received_items
-- Drop the generated column first
ALTER TABLE received_items DROP COLUMN amount;

-- Add new columns
ALTER TABLE received_items ADD COLUMN gst_rate DECIMAL(5,2);
ALTER TABLE received_items ADD COLUMN tax_amount DECIMAL(15,2);
ALTER TABLE received_items ADD COLUMN amount DECIMAL(15,2);

-- Comment for documentation
COMMENT ON COLUMN material_inwards.grand_total IS 'Total amount including tax, used for vendor ledger';
COMMENT ON COLUMN received_items.gst_rate IS 'GST percentage applied to this item';
COMMENT ON COLUMN received_items.tax_amount IS 'Calculated tax amount for this item line';
COMMENT ON COLUMN received_items.amount IS 'Total amount for this item (Taxable + Tax)';
