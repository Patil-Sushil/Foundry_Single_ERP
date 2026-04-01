-- =====================================================
-- QUALITY INSPECTIONS - Create tables from existing schema reference
-- =====================================================

CREATE TABLE IF NOT EXISTS quality_inspections (
    id                      BIGSERIAL PRIMARY KEY,
    inspection_number       VARCHAR(50) UNIQUE NOT NULL,
    inspection_date         DATE NOT NULL,
    inspection_type         VARCHAR(30) NOT NULL,
    heat_id                 BIGINT REFERENCES furnace_heats(id),
    order_id                UUID REFERENCES orders(id),
    total_quantity          INTEGER,
    total_weight            DECIMAL(15,3),
    accepted_quantity       INTEGER DEFAULT 0,
    rejected_quantity       INTEGER DEFAULT 0,
    accepted_weight         DECIMAL(15,3) DEFAULT 0,
    rejected_weight         DECIMAL(15,3) DEFAULT 0,
    inspector_name          VARCHAR(100),
    status                  VARCHAR(20) DEFAULT 'PENDING'
);

CREATE TABLE IF NOT EXISTS inspection_defects (
    id                      BIGSERIAL PRIMARY KEY,
    inspection_id           BIGINT REFERENCES quality_inspections(id) ON DELETE CASCADE,
    item_name               VARCHAR(255),
    defect_type             VARCHAR(50),
    severity                VARCHAR(20),
    defect_location         VARCHAR(100),
    defect_quantity         INTEGER NOT NULL,
    defect_weight           DECIMAL(15,3) NOT NULL,
    photo_urls              TEXT[],
    measurement_data        JSONB
);

CREATE INDEX idx_quality_inspections_heat ON quality_inspections(heat_id);
CREATE INDEX idx_quality_inspections_order ON quality_inspections(order_id);
CREATE INDEX idx_inspection_defects_id ON inspection_defects(inspection_id);
