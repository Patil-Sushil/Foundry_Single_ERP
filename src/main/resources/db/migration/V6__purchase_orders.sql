-- V7: Purchase Orders
-- =============================================

-- -----------------------------
-- PURCHASE ORDERS
-- -----------------------------
CREATE TABLE purchase_orders (
    id BIGSERIAL PRIMARY KEY,
    po_number VARCHAR(50) NOT NULL UNIQUE,
    vendor_id BIGINT NOT NULL REFERENCES vendors(id),
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    po_date DATE NOT NULL DEFAULT CURRENT_DATE,
    expected_delivery_date DATE,
    notes TEXT,
    created_by_user_id UUID,  -- Using UUID directly
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT chk_po_status CHECK (status IN ('OPEN','PARTIALLY_RECEIVED','RECEIVED','CLOSED','CANCELLED'))
);

-- -----------------------------
-- ORDER ITEMS
-- -----------------------------
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    po_id BIGINT NOT NULL REFERENCES purchase_orders(id) ON DELETE CASCADE,
    item_id BIGINT NOT NULL REFERENCES items(id),
    ordered_quantity DECIMAL(15,3) NOT NULL,
    received_quantity DECIMAL(15,3) DEFAULT 0,
    unit_rate DECIMAL(12,2) NOT NULL,
    notes VARCHAR(500)
);

-- -----------------------------
-- INDEXES
-- -----------------------------
CREATE INDEX idx_po_vendor ON purchase_orders(vendor_id);
CREATE INDEX idx_po_status ON purchase_orders(status);
CREATE INDEX idx_po_date ON purchase_orders(po_date);
CREATE INDEX idx_order_items_po ON order_items(po_id);
