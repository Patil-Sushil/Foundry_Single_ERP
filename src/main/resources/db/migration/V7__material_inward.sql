-- V8: Material Inward
-- =============================================

-- -----------------------------
-- MATERIAL INWARDS
-- -----------------------------
CREATE TABLE material_inwards (
    id BIGSERIAL PRIMARY KEY,
    inward_number VARCHAR(50) NOT NULL UNIQUE,
    po_id BIGINT REFERENCES purchase_orders(id),
    vendor_id BIGINT NOT NULL REFERENCES vendors(id),
    vehicle_number VARCHAR(30),
    driver_name VARCHAR(100),
    driver_phone VARCHAR(20),
    vendor_challan_number VARCHAR(100),
    inward_date DATE NOT NULL DEFAULT CURRENT_DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    notes TEXT,
    created_by_user_id UUID, -- UUID
    confirmed_by_user_id UUID, -- UUID
    confirmed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT chk_inward_status CHECK (status IN ('DRAFT','CONFIRMED'))
);

-- -----------------------------
-- RECEIVED ITEMS
-- -----------------------------
CREATE TABLE received_items (
    id BIGSERIAL PRIMARY KEY,
    material_inward_id BIGINT NOT NULL REFERENCES material_inwards(id) ON DELETE CASCADE,
    item_id BIGINT NOT NULL REFERENCES items(id),
    order_item_id BIGINT REFERENCES purchase_order_items(id),
    po_quantity DECIMAL(15,3),
    received_quantity DECIMAL(15,3) NOT NULL,
    unit_rate DECIMAL(12,2) NOT NULL,
    amount DECIMAL(15,2) GENERATED ALWAYS AS (received_quantity * unit_rate) STORED,
    notes VARCHAR(500)
);

-- -----------------------------
-- INDEXES
-- -----------------------------
CREATE INDEX idx_inward_po ON material_inwards(po_id);
CREATE INDEX idx_inward_vendor ON material_inwards(vendor_id);
CREATE INDEX idx_inward_status ON material_inwards(status);
CREATE INDEX idx_inward_date ON material_inwards(inward_date);
CREATE INDEX idx_received_items_inward ON received_items(material_inward_id);
CREATE INDEX idx_received_items_item ON received_items(item_id);
