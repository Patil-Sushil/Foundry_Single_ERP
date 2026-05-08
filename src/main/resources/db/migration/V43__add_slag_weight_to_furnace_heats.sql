-- V43: Add slag_weight to furnace_heats
-- ===================================

ALTER TABLE furnace_heats ADD COLUMN slag_weight DECIMAL(15,3) DEFAULT 0;

COMMENT ON COLUMN furnace_heats.slag_weight IS 'Weight of slag/impurity waste removed during melting in kg. Non-recoverable.';
