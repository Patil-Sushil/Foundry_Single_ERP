-- =============================================
-- Add order_type column to orders
-- =============================================

ALTER TABLE orders
    ADD COLUMN order_type VARCHAR(20);

-- Set default for existing rows (quotation-based)
UPDATE orders
SET order_type = 'QUOTATION'
WHERE quotation_id IS NOT NULL;

UPDATE orders
SET order_type = 'DIRECT'
WHERE quotation_id IS NULL;

-- Make column NOT NULL after updating
ALTER TABLE orders
    ALTER COLUMN order_type SET NOT NULL;