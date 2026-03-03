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

-- V13: Furnace Heat Materials and Upgrades
-- =============================================

-- ALTER furnace_heats table
ALTER TABLE furnace_heats
    ADD COLUMN pouring_start_time TIME NULL,
    ADD COLUMN pouring_end_time TIME NULL,
    ADD COLUMN order_id VARCHAR(50) NULL;

COMMENT ON COLUMN furnace_heats.order_id IS 'TODO: Convert to foreign key when orders table is created';

-- CREATE TABLE heat_material_items
CREATE TABLE heat_material_items (
                                     id BIGSERIAL PRIMARY KEY,
                                     heat_id BIGINT NOT NULL,
                                     item_id BIGINT NOT NULL,
                                     item_name VARCHAR(255) NOT NULL,
                                     material_type VARCHAR(20) NOT NULL DEFAULT 'RAW_MATERIAL',
                                     quantity_used DOUBLE PRECISION NOT NULL,
                                     unit_rate DOUBLE PRECISION,
                                     total_cost DOUBLE PRECISION,
                                     created_at TIMESTAMP DEFAULT NOW(),
                                     updated_at TIMESTAMP DEFAULT NOW(),
                                     CONSTRAINT fk_heat_material_heat FOREIGN KEY (heat_id) REFERENCES furnace_heats(id) ON DELETE CASCADE
);

CREATE INDEX idx_heat_material_heat_id ON heat_material_items(heat_id);
