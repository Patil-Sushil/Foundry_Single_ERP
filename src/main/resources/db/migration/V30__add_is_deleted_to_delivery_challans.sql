-- V31: Add is_deleted column to delivery_challans for soft-delete support
-- ====================================================================

ALTER TABLE delivery_challans ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE;

COMMENT ON COLUMN delivery_challans.is_deleted IS 'Flag for soft-delete support in Billing module.';
