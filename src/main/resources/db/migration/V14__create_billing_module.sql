-- ============================================
-- V14__create_delivery_challan_and_invoice.sql
-- ============================================

-- ============================================
-- EXTENSION (UUID generation)
-- ============================================
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================
-- DELIVERY CHALLANS
-- ============================================
CREATE TABLE IF NOT EXISTS delivery_challans (

                                                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    dc_number VARCHAR(50) UNIQUE NOT NULL,

    order_id UUID NOT NULL,
    customer_id UUID NOT NULL,

    dispatch_date DATE,

    vehicle_number VARCHAR(50),
    transport_name VARCHAR(100),
    lr_number VARCHAR(100),

    total_quantity INT,
    total_weight DECIMAL(12,2),

    -- GST FIELDS (NEW)
    gst_type VARCHAR(20),
    gst_percentage DECIMAL(5,2) DEFAULT 18,
    subtotal DECIMAL(19,2) DEFAULT 0,
    cgst DECIMAL(19,2) DEFAULT 0,
    sgst DECIMAL(19,2) DEFAULT 0,
    igst DECIMAL(19,2) DEFAULT 0,
    total_gst DECIMAL(19,2) DEFAULT 0,

    total_amount DECIMAL(19,2) DEFAULT 0,

    status VARCHAR(20) DEFAULT 'CREATED',

    -- AUDIT FIELDS
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    -- CONSTRAINTS
    CONSTRAINT chk_dc_status
    CHECK (status IN ('CREATED', 'DISPATCHED', 'DELIVERED', 'INVOICED', 'CANCELLED')),

    CONSTRAINT chk_dc_gst_type
    CHECK (gst_type IS NULL OR gst_type IN ('CGST_SGST', 'IGST'))
    );

-- ============================================
-- DELIVERY CHALLAN ITEMS
-- ============================================
CREATE TABLE IF NOT EXISTS delivery_challan_items (

                                                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    dc_id UUID NOT NULL,
    order_item_id UUID NOT NULL,

    quantity INT,
    weight DECIMAL(12,2),
    rate DECIMAL(12,2),
    amount DECIMAL(19,2),

    -- GST PER ITEM (NEW)
    gst_percentage DECIMAL(5,2) DEFAULT 18,
    gst_amount DECIMAL(19,2) DEFAULT 0,
    total_with_gst DECIMAL(19,2) DEFAULT 0,

    -- AUDIT FIELDS
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
    );

-- ============================================
-- INVOICES (1 ORDER → 1 INVOICE)
-- ============================================
CREATE TABLE IF NOT EXISTS invoices (

                                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    invoice_number VARCHAR(50) UNIQUE NOT NULL,

    order_id UUID UNIQUE NOT NULL,

    vehicle_number VARCHAR(50),

    subtotal DECIMAL(19,2) DEFAULT 0,

    -- GST FIELDS (UPDATED)
    gst_type VARCHAR(20),
    gst_percentage DECIMAL(5,2) DEFAULT 18,
    cgst DECIMAL(19,2) DEFAULT 0,
    sgst DECIMAL(19,2) DEFAULT 0,
    igst DECIMAL(19,2) DEFAULT 0,
    total_gst DECIMAL(19,2) DEFAULT 0,

    total_amount DECIMAL(19,2) DEFAULT 0,

    invoice_date DATE,
    due_date DATE,

    bill_status VARCHAR(20) DEFAULT 'UNPAID',

    -- AUDIT FIELDS
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    -- CONSTRAINTS
    CONSTRAINT chk_invoice_status
    CHECK (bill_status IN ('PAID', 'UNPAID', 'PARTIALLY_PAID', 'CANCELLED')),

    CONSTRAINT chk_invoice_gst_type
    CHECK (gst_type IS NULL OR gst_type IN ('CGST_SGST', 'IGST'))
    );

-- ============================================
-- INVOICE ITEMS
-- ============================================
CREATE TABLE IF NOT EXISTS invoice_items (

                                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    invoice_id UUID NOT NULL,
    order_item_id UUID NOT NULL,

    quantity INT,
    weight DECIMAL(12,2),
    rate DECIMAL(12,2),
    amount DECIMAL(19,2),

    -- GST PER ITEM (NEW)
    gst_percentage DECIMAL(5,2) DEFAULT 18,
    gst_amount DECIMAL(19,2) DEFAULT 0,
    total_with_gst DECIMAL(19,2) DEFAULT 0,

    -- AUDIT FIELDS
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
    );

-- ============================================
-- FOREIGN KEYS - DELIVERY CHALLANS
-- ============================================
ALTER TABLE delivery_challans
    ADD CONSTRAINT fk_dc_order
        FOREIGN KEY (order_id)
            REFERENCES orders(id)
            ON DELETE RESTRICT;

ALTER TABLE delivery_challans
    ADD CONSTRAINT fk_dc_customer
        FOREIGN KEY (customer_id)
            REFERENCES customer(id)
            ON DELETE RESTRICT;

-- ============================================
-- FOREIGN KEYS - DELIVERY CHALLAN ITEMS
-- ============================================
ALTER TABLE delivery_challan_items
    ADD CONSTRAINT fk_dc_items_dc
        FOREIGN KEY (dc_id)
            REFERENCES delivery_challans(id)
            ON DELETE CASCADE;

ALTER TABLE delivery_challan_items
    ADD CONSTRAINT fk_dc_items_order_item
        FOREIGN KEY (order_item_id)
            REFERENCES order_items(id)
            ON DELETE RESTRICT;

-- ============================================
-- FOREIGN KEYS - INVOICES
-- ============================================
ALTER TABLE invoices
    ADD CONSTRAINT fk_invoice_order
        FOREIGN KEY (order_id)
            REFERENCES orders(id)
            ON DELETE RESTRICT;

-- ============================================
-- FOREIGN KEYS - INVOICE ITEMS
-- ============================================
ALTER TABLE invoice_items
    ADD CONSTRAINT fk_invoice_items_invoice
        FOREIGN KEY (invoice_id)
            REFERENCES invoices(id)
            ON DELETE CASCADE;

ALTER TABLE invoice_items
    ADD CONSTRAINT fk_invoice_items_order_item
        FOREIGN KEY (order_item_id)
            REFERENCES order_items(id)
            ON DELETE RESTRICT;

-- ============================================
-- INDEXES - DELIVERY CHALLANS
-- ============================================
CREATE INDEX IF NOT EXISTS idx_dc_order_id ON delivery_challans(order_id);
CREATE INDEX IF NOT EXISTS idx_dc_customer_id ON delivery_challans(customer_id);
CREATE INDEX IF NOT EXISTS idx_dc_status ON delivery_challans(status);
CREATE INDEX IF NOT EXISTS idx_dc_dispatch_date ON delivery_challans(dispatch_date);
CREATE INDEX IF NOT EXISTS idx_dc_number ON delivery_challans(dc_number);
CREATE INDEX IF NOT EXISTS idx_dc_gst_type ON delivery_challans(gst_type);

-- ============================================
-- INDEXES - DELIVERY CHALLAN ITEMS
-- ============================================
CREATE INDEX IF NOT EXISTS idx_dc_items_dc_id ON delivery_challan_items(dc_id);
CREATE INDEX IF NOT EXISTS idx_dc_items_order_item_id ON delivery_challan_items(order_item_id);

-- ============================================
-- INDEXES - INVOICES
-- ============================================
CREATE INDEX IF NOT EXISTS idx_invoice_order_id ON invoices(order_id);
CREATE INDEX IF NOT EXISTS idx_invoice_status ON invoices(bill_status);
CREATE INDEX IF NOT EXISTS idx_invoice_due_date ON invoices(due_date);
CREATE INDEX IF NOT EXISTS idx_invoice_date ON invoices(invoice_date);
CREATE INDEX IF NOT EXISTS idx_invoice_number ON invoices(invoice_number);
CREATE INDEX IF NOT EXISTS idx_invoice_status_due ON invoices(bill_status, due_date);
CREATE INDEX IF NOT EXISTS idx_invoice_gst_type ON invoices(gst_type);

-- ============================================
-- INDEXES - INVOICE ITEMS
-- ============================================
CREATE INDEX IF NOT EXISTS idx_invoice_items_invoice_id ON invoice_items(invoice_id);
CREATE INDEX IF NOT EXISTS idx_invoice_items_order_item_id ON invoice_items(order_item_id);