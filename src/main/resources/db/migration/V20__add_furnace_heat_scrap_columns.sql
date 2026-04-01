-- =====================================================
-- PART 1: Add grade column to furnace_heats
-- =====================================================
-- NOTE: We are NOT adding order_id or order_item_id to furnace_heats
-- Those relationships are handled via the heat_order_items junction table

ALTER TABLE furnace_heats ADD COLUMN grade VARCHAR(50);

-- Migrate existing data: try to get grade from linked orders via production module
UPDATE furnace_heats fh SET grade = (
    SELECT oi.material_grade 
    FROM production_items pi
    JOIN order_items oi ON pi.order_item_id = oi.id
    JOIN production_entries pe ON pi.production_entry_id = pe.id
    WHERE pe.report_date = (
        SELECT fr.date FROM furnace_reports fr WHERE fr.id = fh.furnace_id
    )
    LIMIT 1
) WHERE fh.grade IS NULL;

-- Set default grade for heats without any data
UPDATE furnace_heats SET grade = 'FG260' WHERE grade IS NULL;

-- Make grade NOT NULL after migration
ALTER TABLE furnace_heats ALTER COLUMN grade SET NOT NULL;

-- Add grade constraint
ALTER TABLE furnace_heats ADD CONSTRAINT check_valid_grade 
    CHECK (grade IN (
        -- Cast Iron
        'FG150', 'FG200', 'FG260', 'FG300', 'FG350', 'FG400',
        -- SG Iron
        'SG 400/15', 'SG 400/18', 'SG 500/7', 'SG 600/3', 'SG 700/2', 'SG 800/2',
        -- Carbon Steel
        'WCB', 'WCC', 'WCA', 'LCB', 'LCC',
        -- Stainless Steel
        'CF8 (SS 304)', 'CF8M (SS 316)', 'CF3 (SS 304L)', 'CF3M (SS 316L)', 'CA15 (SS 410)', 'CA40 (SS 420)',
        -- Alloy Steel
        'Mn Steel (11-14%)', 'Cr-Mo Steel', 'Ni-Hard', 'High Chrome Iron',
        -- Aluminum Alloy
        'LM6', 'LM25', 'ADC12', 'A356', 'LM2',
        -- Copper Alloy
        'Gunmetal (GM)', 'Phosphor Bronze (PB)', 'Leaded Bronze', 'Aluminum Bronze', 'Brass',
        -- Special
        'MIXED'
    ));

CREATE INDEX idx_heats_grade ON furnace_heats(grade);

COMMENT ON COLUMN furnace_heats.grade IS 
'Material grade for this heat. All order items in this heat must have matching material_grade. Single grade per heat enforced in application layer.';

-- =====================================================
-- PART 2: Add production tracking columns
-- =====================================================
ALTER TABLE furnace_heats ADD COLUMN liquid_metal_weight DECIMAL(15,3);
ALTER TABLE furnace_heats ADD COLUMN castings_poured_weight DECIMAL(15,3);

COMMENT ON COLUMN furnace_heats.liquid_metal_weight IS 'Actual tapped liquid metal weight in kg';
COMMENT ON COLUMN furnace_heats.castings_poured_weight IS 'Total weight of castings poured (excluding gating). Should equal sum of heat_order_items.weight_produced';

-- =====================================================
-- PART 3: Add process scrap breakdown columns
-- =====================================================
ALTER TABLE furnace_heats ADD COLUMN runner_weight DECIMAL(15,3) DEFAULT 0;
ALTER TABLE furnace_heats ADD COLUMN riser_weight DECIMAL(15,3) DEFAULT 0;
ALTER TABLE furnace_heats ADD COLUMN skull_weight DECIMAL(15,3) DEFAULT 0;
ALTER TABLE furnace_heats ADD COLUMN spillage_weight DECIMAL(15,3) DEFAULT 0;

COMMENT ON COLUMN furnace_heats.runner_weight IS 'Weight of runners (gating system) in kg';
COMMENT ON COLUMN furnace_heats.riser_weight IS 'Weight of risers/feeders in kg';
COMMENT ON COLUMN furnace_heats.skull_weight IS 'Weight of furnace skull residue in kg';
COMMENT ON COLUMN furnace_heats.spillage_weight IS 'Weight of metal spillage during pouring in kg';

-- =====================================================
-- PART 4: Add calculated total_process_scrap
-- =====================================================
ALTER TABLE furnace_heats ADD COLUMN total_process_scrap DECIMAL(15,3) 
    GENERATED ALWAYS AS (
        COALESCE(runner_weight, 0) + COALESCE(riser_weight, 0) + 
        COALESCE(skull_weight, 0) + COALESCE(spillage_weight, 0)
    ) STORED;

COMMENT ON COLUMN furnace_heats.total_process_scrap IS 
'Auto-calculated: sum of runner, riser, skull, spillage weights. This scrap is recyclable and returned to inventory.';

-- =====================================================
-- PART 5: Add scrap tracking columns
-- =====================================================
ALTER TABLE furnace_heats ADD COLUMN process_scrap_entry_id BIGINT;
ALTER TABLE furnace_heats ADD COLUMN auto_return_scrap BOOLEAN DEFAULT TRUE;

CREATE INDEX idx_heats_scrap_entry ON furnace_heats(process_scrap_entry_id);

COMMENT ON COLUMN furnace_heats.process_scrap_entry_id IS 
'Reference to scrap_entries record for process scrap from this heat. Created after verification and approval.';
COMMENT ON COLUMN furnace_heats.auto_return_scrap IS 
'If TRUE, process scrap will auto-create return entry after metallurgist approval. Default TRUE for normal operation.';

-- =====================================================
-- PART 6: Add yield calculations
-- =====================================================
ALTER TABLE furnace_heats ADD COLUMN furnace_yield_percentage DECIMAL(5,2) 
    GENERATED ALWAYS AS (
        CASE WHEN total_weight > 0 AND liquid_metal_weight IS NOT NULL 
        THEN ROUND((liquid_metal_weight / total_weight * 100)::numeric, 2)
        ELSE NULL END
    ) STORED;

ALTER TABLE furnace_heats ADD COLUMN pouring_yield_percentage DECIMAL(5,2) 
    GENERATED ALWAYS AS (
        CASE WHEN liquid_metal_weight > 0 AND castings_poured_weight IS NOT NULL 
        THEN ROUND((castings_poured_weight / liquid_metal_weight * 100)::numeric, 2)
        ELSE NULL END
    ) STORED;

COMMENT ON COLUMN furnace_heats.furnace_yield_percentage IS 
'Auto-calculated: (liquid_metal_weight / total_weight) * 100. Indicates melting efficiency. Target: >80%';
COMMENT ON COLUMN furnace_heats.pouring_yield_percentage IS 
'Auto-calculated: (castings_poured_weight / liquid_metal_weight) * 100. Indicates pouring efficiency. Target: >85%';
