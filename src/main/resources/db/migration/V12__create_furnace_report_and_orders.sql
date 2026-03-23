-- ============================================
-- EXTENSION (UUID)
-- ============================================
CREATE EXTENSION IF NOT EXISTS "pgcrypto";


-- ============================================
-- ORDERS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS orders (
                                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number VARCHAR(50) NOT NULL UNIQUE,

    customer_id UUID NOT NULL,
    quotation_id UUID UNIQUE,

    order_type VARCHAR(50) NOT NULL,

    order_date DATE,
    delivery_date DATE,

    place_of_supply VARCHAR(255),
    po_reference VARCHAR(255),

    status VARCHAR(30) NOT NULL DEFAULT 'CREATED',

    -- AMOUNTS
    sub_total NUMERIC(19,2) DEFAULT 0,
    discount NUMERIC(19,2) DEFAULT 0,
    tax NUMERIC(19,2) DEFAULT 0,
    total_amount NUMERIC(19,2) DEFAULT 0,

    -- AUDIT FIELDS
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN DEFAULT FALSE
    );


-- ============================================
-- ORDER ITEMS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS order_items (
                                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    order_id UUID NOT NULL,

    -- PART INFO
    part_name VARCHAR(255),
    drawing_number VARCHAR(100),
    material_grade VARCHAR(100),

    -- WEIGHT
    net_weight_kg NUMERIC(10,3),
    gross_weight_kg NUMERIC(10,3),

    -- PATTERN LOGIC
    pattern_status VARCHAR(50),
    pattern_provided_by_customer BOOLEAN,

    pattern_id UUID,
    pattern_receipt_id UUID,

    -- PRICING
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(19,2),
    line_total NUMERIC(19,2),

    -- AUDIT FIELDS
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN DEFAULT FALSE
    );


-- ============================================
-- FURNACE REPORTS
-- ============================================
CREATE TABLE IF NOT EXISTS furnace_reports (
                                               id BIGSERIAL PRIMARY KEY,
                                               furnace_ref_no VARCHAR(50) NOT NULL UNIQUE,
    operator_name VARCHAR(50) NOT NULL,
    shift VARCHAR(8),
    incharge_name VARCHAR(50),
    date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
    );

ALTER TABLE furnace_reports
    ADD CONSTRAINT chk_shift
        CHECK (shift IN ('DAY','NIGHT'));


-- ============================================
-- FURNACE HEATS
-- ============================================
CREATE TABLE IF NOT EXISTS furnace_heats (
                                             id BIGSERIAL PRIMARY KEY,

                                             sipercentage DOUBLE PRECISION,
                                             cpcpercentage DOUBLE PRECISION,
                                             mgpercentage DOUBLE PRECISION,

                                             furnace_id BIGINT NOT NULL,

                                             total_weight DOUBLE PRECISION NOT NULL,

                                             start_reading DOUBLE PRECISION NOT NULL,
                                             stop_reading DOUBLE PRECISION NOT NULL,

                                             difference_reading DOUBLE PRECISION DEFAULT 0,
                                             power_to_weight DOUBLE PRECISION DEFAULT 0,

                                             pouring_temp DOUBLE PRECISION,

                                             pouring_start_time TIME,
                                             pouring_end_time TIME,

                                             order_id UUID,

                                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                             is_deleted BOOLEAN DEFAULT FALSE
);


-- ============================================
-- FOREIGN KEYS
-- ============================================

-- Orders
ALTER TABLE orders
    ADD CONSTRAINT fk_orders_customer
        FOREIGN KEY (customer_id)
            REFERENCES customer(id);

ALTER TABLE orders
    ADD CONSTRAINT fk_orders_quotation
        FOREIGN KEY (quotation_id)
            REFERENCES quotations(id);

-- Order Items
ALTER TABLE order_items
    ADD CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)
            REFERENCES orders(id)
            ON DELETE CASCADE;

ALTER TABLE order_items
    ADD CONSTRAINT fk_order_items_pattern
        FOREIGN KEY (pattern_id)
            REFERENCES patterns(id);

ALTER TABLE order_items
    ADD CONSTRAINT fk_order_items_pattern_receipt
        FOREIGN KEY (pattern_receipt_id)
            REFERENCES pattern_receipt(id);

-- Furnace
ALTER TABLE furnace_heats
    ADD CONSTRAINT fk_furnace_heat_report
        FOREIGN KEY (furnace_id)
            REFERENCES furnace_reports(id)
            ON DELETE CASCADE;

ALTER TABLE furnace_heats
    ADD CONSTRAINT fk_furnace_heat_order
        FOREIGN KEY (order_id)
            REFERENCES orders(id)
            ON DELETE SET NULL;


-- ============================================
-- INDEXES
-- ============================================

-- Orders
CREATE INDEX IF NOT EXISTS idx_orders_customer ON orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_order_type ON orders(order_type);
CREATE INDEX IF NOT EXISTS idx_orders_order_date ON orders(order_date);

-- Order Items
CREATE INDEX IF NOT EXISTS idx_order_items_order ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_pattern ON order_items(pattern_id);

-- Furnace
CREATE INDEX IF NOT EXISTS idx_furnace_heats_order ON furnace_heats(order_id);
CREATE INDEX IF NOT EXISTS idx_furnace_heats_furnace ON furnace_heats(furnace_id);