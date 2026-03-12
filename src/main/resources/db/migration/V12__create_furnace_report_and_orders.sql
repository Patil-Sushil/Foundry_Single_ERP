-- ============================================
-- EXTENSION (UUID)
-- ============================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";


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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
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

                                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE furnace_heats
    ADD CONSTRAINT fk_furnace_heat_report
        FOREIGN KEY (furnace_id)
            REFERENCES furnace_reports(id)
            ON DELETE CASCADE;


-- ============================================
-- ORDERS TABLE
-- ============================================

CREATE TABLE IF NOT EXISTS orders (

  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    order_number VARCHAR(50) NOT NULL UNIQUE,

    customer_id UUID NOT NULL,
    quotation_id UUID UNIQUE,

    order_date DATE,
    delivery_date DATE,

    place_of_supply VARCHAR(100),
    po_reference VARCHAR(100),

    status VARCHAR(30),

    total_amount NUMERIC(19,2),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
    );


-- ============================================
-- ORDER ITEMS TABLE
-- ============================================

CREATE TABLE IF NOT EXISTS order_item (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    order_id UUID NOT NULL,

    product_name VARCHAR(255) NOT NULL,
    metal_type VARCHAR(100) NOT NULL,

    pattern_id UUID,

    quantity INTEGER NOT NULL CHECK (quantity > 0),

    unit_price NUMERIC(19,2),
    total_price NUMERIC(19,2),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
    );


-- ============================================
-- FOREIGN KEYS
-- ============================================

ALTER TABLE orders
    ADD CONSTRAINT fk_orders_customer
        FOREIGN KEY (customer_id)
            REFERENCES customer(id);

ALTER TABLE orders
    ADD CONSTRAINT fk_orders_quotation
        FOREIGN KEY (quotation_id)
            REFERENCES quotations(id);

ALTER TABLE order_item
    ADD CONSTRAINT fk_order_item_order
        FOREIGN KEY (order_id)
            REFERENCES orders(id)
            ON DELETE CASCADE;

ALTER TABLE order_item
    ADD CONSTRAINT fk_order_item_pattern
        FOREIGN KEY (pattern_id)
            REFERENCES patterns(id);

ALTER TABLE furnace_heats
    ADD CONSTRAINT fk_furnace_heat_order
        FOREIGN KEY (order_id)
            REFERENCES orders(id)
            ON DELETE SET NULL;


-- ============================================
-- INDEXES (IMPORTANT FOR ERP PERFORMANCE)
-- ============================================

CREATE INDEX IF NOT EXISTS idx_orders_customer
    ON orders(customer_id);

CREATE INDEX IF NOT EXISTS idx_orders_status
    ON orders(status);

CREATE INDEX IF NOT EXISTS idx_orders_order_date
    ON orders(order_date);

CREATE INDEX IF NOT EXISTS idx_order_item_order
    ON order_item(order_id);

CREATE INDEX IF NOT EXISTS idx_order_item_pattern
    ON order_item(pattern_id);

CREATE INDEX IF NOT EXISTS idx_furnace_heats_furnace
    ON furnace_heats(furnace_id);

CREATE INDEX IF NOT EXISTS idx_furnace_heats_order
    ON furnace_heats(order_id);

ALTER TABLE furnace_heats
    ADD CONSTRAINT fk_furnace_heats_order
        FOREIGN KEY (order_id)
            REFERENCES orders(id)
            ON DELETE SET NULL;

CREATE INDEX idx_furnace_heats_order_id ON furnace_heats(order_id);