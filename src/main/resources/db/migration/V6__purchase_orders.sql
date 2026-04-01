-- =============================================
-- V8__purchase_orders.sql
-- =============================================

-- =============================================
-- PURCHASE ORDERS TABLE
-- =============================================

CREATE TABLE purchase_orders (
                                 id BIGSERIAL PRIMARY KEY,

                                 po_number VARCHAR(50) NOT NULL UNIQUE,

                                 vendor_id BIGINT NOT NULL,
                                 status VARCHAR(30) NOT NULL DEFAULT 'OPEN',

                                 po_date DATE NOT NULL DEFAULT CURRENT_DATE,
                                 expected_delivery_date DATE,

                                 notes TEXT,

                                 created_by_user_id UUID,

                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 created_by VARCHAR(255),
                                 updated_by VARCHAR(255),

                                 CONSTRAINT fk_po_vendor
                                     FOREIGN KEY (vendor_id)
                                         REFERENCES vendors(id),

                                 CONSTRAINT chk_po_status
                                     CHECK (status IN (
                                                       'OPEN',
                                                       'PARTIALLY_RECEIVED',
                                                       'RECEIVED',
                                                       'CLOSED',
                                                       'CANCELLED'
                                         ))
);

-- =============================================
-- PURCHASE ORDER ITEMS TABLE
-- =============================================

CREATE TABLE purchase_order_items (
                                      id BIGSERIAL PRIMARY KEY,

                                      po_id BIGINT NOT NULL,
                                      item_id BIGINT NOT NULL,

                                      ordered_quantity NUMERIC(15,3) NOT NULL CHECK (ordered_quantity > 0),
                                      received_quantity NUMERIC(15,3) DEFAULT 0 CHECK (received_quantity >= 0),

                                      unit_rate NUMERIC(12,2) NOT NULL CHECK (unit_rate >= 0),

                                      notes VARCHAR(500),

                                      CONSTRAINT fk_poi_po
                                          FOREIGN KEY (po_id)
                                              REFERENCES purchase_orders(id)
                                              ON DELETE CASCADE,

                                      CONSTRAINT fk_poi_item
                                          FOREIGN KEY (item_id)
                                              REFERENCES items(id)
);

-- =============================================
-- INDEXES (Performance Ready)
-- =============================================

CREATE INDEX idx_po_vendor ON purchase_orders(vendor_id);
CREATE INDEX idx_po_status ON purchase_orders(status);
CREATE INDEX idx_po_date ON purchase_orders(po_date);
CREATE INDEX idx_purchase_order_items_po
    ON purchase_order_items(po_id);

CREATE INDEX idx_purchase_order_items_item
    ON purchase_order_items(item_id);