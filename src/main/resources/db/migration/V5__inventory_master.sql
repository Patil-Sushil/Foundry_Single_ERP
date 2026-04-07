-- V6: Inventory Master Tables
-- =============================================

-- -----------------------------
-- VENDORS
-- -----------------------------
CREATE TABLE vendors (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    gst_number VARCHAR(20),
    address TEXT,
    email TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- -----------------------------
-- DEPARTMENTS
-- -----------------------------
CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(20) NOT NULL UNIQUE
);

-- -----------------------------
-- ITEMS
-- -----------------------------
CREATE TABLE items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    category VARCHAR(30) NOT NULL,
    sub_category VARCHAR(30),
    department_id BIGINT REFERENCES departments(id),
    unit VARCHAR(20) NOT NULL,
    current_stock DECIMAL(15,3) DEFAULT 0,
    reorder_level DECIMAL(15,3) DEFAULT 0,
    min_stock_level DECIMAL(15,3) DEFAULT 0,
    location VARCHAR(100),
    last_purchase_rate DECIMAL(12,2) DEFAULT 0,
    avg_rate DECIMAL(12,2) DEFAULT 0,
    hsn_code VARCHAR(10) UNIQUE ,
    gst_rate DECIMAL(5,2) DEFAULT 18.00,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- -----------------------------
-- ITEM VENDOR RATES
-- -----------------------------
CREATE TABLE item_vendor_rates (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL REFERENCES items(id),
    vendor_id BIGINT NOT NULL REFERENCES vendors(id),
    last_rate DECIMAL(12,2) NOT NULL,
    last_purchased_on DATE NOT NULL,
    UNIQUE(item_id, vendor_id)
);

-- -----------------------------
-- SEED DEPARTMENTS
-- -----------------------------
INSERT INTO departments (name, code) VALUES
  ('Melting', 'MELT'),
  ('Moulding', 'MOULD'),
  ('Fettling', 'FETT'),
  ('Dispatch', 'DISP'),
  ('Maintenance', 'MAINT'),
  ('Store', 'STORE');

-- -----------------------------
-- INDEXES
-- -----------------------------
CREATE INDEX idx_items_category ON items(category);
CREATE INDEX idx_items_code ON items(code);
CREATE INDEX idx_items_active ON items(is_active);
CREATE INDEX idx_item_vendor_rates ON item_vendor_rates(item_id, vendor_id);
