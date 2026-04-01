-- V33: Add detailed GST breakdown to purchase_orders
-- =====================================================

ALTER TABLE purchase_orders ADD COLUMN cgst DECIMAL(15,2) DEFAULT 0;
ALTER TABLE purchase_orders ADD COLUMN sgst DECIMAL(15,2) DEFAULT 0;
ALTER TABLE purchase_orders ADD COLUMN igst DECIMAL(15,2) DEFAULT 0;
ALTER TABLE purchase_orders ADD COLUMN gst_type VARCHAR(20);

COMMENT ON COLUMN purchase_orders.cgst IS 'Central GST component';
COMMENT ON COLUMN purchase_orders.sgst IS 'State GST component';
COMMENT ON COLUMN purchase_orders.igst IS 'Integrated GST component (for inter-state)';
COMMENT ON COLUMN purchase_orders.gst_type IS 'Type of GST applied: CGST_SGST or IGST';
