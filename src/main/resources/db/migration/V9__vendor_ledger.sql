-- V10: Vendor Ledger
-- =============================================

-- -----------------------------
-- VENDOR LEDGER
-- -----------------------------
CREATE TABLE vendor_ledger (
    id BIGSERIAL PRIMARY KEY,
    vendor_id BIGINT NOT NULL REFERENCES vendors(id),
    material_inward_id BIGINT REFERENCES material_inwards(id),
    entry_type VARCHAR(10) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    description VARCHAR(500),
    entry_date DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMP DEFAULT NOW(),
    created_by VARCHAR(255),
    CONSTRAINT chk_entry_type CHECK (entry_type IN ('CREDIT','DEBIT'))
);

-- -----------------------------
-- INDEXES
-- -----------------------------
CREATE INDEX idx_ledger_vendor ON vendor_ledger(vendor_id);
CREATE INDEX idx_ledger_date ON vendor_ledger(entry_date);
CREATE INDEX idx_ledger_type ON vendor_ledger(entry_type);
