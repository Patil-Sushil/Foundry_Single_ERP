-- =====================================================
-- V24: Extend existing tables for scrap and internal returns
-- =====================================================

-- 1. Extend items table
ALTER TABLE items ADD COLUMN grade VARCHAR(50);
ALTER TABLE items ADD COLUMN is_scrap BOOLEAN DEFAULT FALSE;
COMMENT ON COLUMN items.grade IS 'For scrap items, indicates the material grade.';
COMMENT ON COLUMN items.is_scrap IS 'Flag to identify items that are scrap materials.';

-- 2. Extend material_inwards table
ALTER TABLE material_inwards ADD COLUMN inward_type VARCHAR(30) DEFAULT 'VENDOR_PURCHASE';
ALTER TABLE material_inwards ADD COLUMN scrap_entry_id BIGINT;
ALTER TABLE material_inwards ADD CONSTRAINT fk_inward_scrap_entry FOREIGN KEY (scrap_entry_id) REFERENCES scrap_entries(id);
COMMENT ON COLUMN material_inwards.inward_type IS 'VENDOR_PURCHASE or INTERNAL_RETURN (from scrap/rejection)';

-- 3. Extend quality_inspections table
ALTER TABLE quality_inspections ADD COLUMN scrap_entry_id BIGINT;
ALTER TABLE quality_inspections ADD COLUMN disposition_status VARCHAR(30) DEFAULT 'PENDING';
ALTER TABLE quality_inspections ADD CONSTRAINT fk_inspection_scrap_entry FOREIGN KEY (scrap_entry_id) REFERENCES scrap_entries(id);
COMMENT ON COLUMN quality_inspections.disposition_status IS 'PENDING, SCRAPPED, REWORK, ACCEPTED_WITH_DEVIATION';

-- 4. Extend inspection_defects table
ALTER TABLE inspection_defects ADD COLUMN scrap_item_id BIGINT;
ALTER TABLE inspection_defects ADD COLUMN disposition VARCHAR(30);
ALTER TABLE inspection_defects ADD CONSTRAINT fk_defect_scrap_item FOREIGN KEY (scrap_item_id) REFERENCES scrap_items(id);
COMMENT ON COLUMN inspection_defects.disposition IS 'SCRAP, REWORK, SALVAGE';
