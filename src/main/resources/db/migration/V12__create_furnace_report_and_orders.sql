CREATE TABLE furnace_reports (
    id BIGSERIAL PRIMARY KEY,
    furnace_ref_no VARCHAR(50) NOT NULL UNIQUE,
    operator_name VARCHAR(50) NOT NULL,
    shift VARCHAR(8),
    incharge_name VARCHAR(50),
    date DATE
);

CREATE TABLE furnace_heats (
    id BIGSERIAL PRIMARY KEY,
    sipercentage double precision,
    cpcpercentage double precision,
    mgpercentage double precision,
    furnace_id BIGINT NOT NULL REFERENCES furnace_reports(id) ON DELETE CASCADE,
    total_weight double precision NOT NULL ,
    start_reading double precision NOT NULL ,
    stop_reading double precision NOT NULL ,
    difference_reading double precision DEFAULT 0,
    power_to_weight double precision DEFAULT 0,
    pouring_temp double precision
);

-- ============================================
-- EXTENSION (UUID)
-- ============================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================
-- ORDERS TABLE
-- ============================================

CREATE TABLE orders (

                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                        order_number VARCHAR(50) NOT NULL UNIQUE,

                        customer_id UUID NOT NULL,
                        quotation_id UUID UNIQUE,

                        order_date DATE,
                        delivery_date DATE,

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

CREATE TABLE order_item (

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


-- ============================================
-- INDEXES
-- ============================================

CREATE INDEX idx_orders_customer
    ON orders(customer_id);

CREATE INDEX idx_orders_status
    ON orders(status);

CREATE INDEX idx_orders_order_date
    ON orders(order_date);

CREATE INDEX idx_order_item_order
    ON order_item(order_id);

CREATE INDEX idx_order_item_pattern
    ON order_item(pattern_id);