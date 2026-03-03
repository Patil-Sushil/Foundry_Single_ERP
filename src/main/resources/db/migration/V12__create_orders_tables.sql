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
                        quotation_id UUID  UNIQUE,

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
                            quantity DECIMAL NOT NULL CHECK (quantity > 0),

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

-- ============================================
-- INDEXES
-- ============================================

CREATE INDEX idx_orders_customer ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_order_date ON orders(order_date);
CREATE INDEX idx_order_item_order ON order_item(order_id);