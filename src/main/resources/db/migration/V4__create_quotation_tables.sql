-------------------------------------------------------
-- V4__create_quotation_tables.sql
-------------------------------------------------------

-------------------------------------------------------
-- EXTENSION (UUID)
-------------------------------------------------------
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-------------------------------------------------------
-- QUOTATIONS TABLE
-------------------------------------------------------
CREATE TABLE IF NOT EXISTS quotations (

                                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    quotation_number VARCHAR(50) UNIQUE NOT NULL,
    quotation_date DATE,
    valid_until DATE,
    revision_no INTEGER DEFAULT 0,

    customer_id UUID NOT NULL,
    enquiry_id UUID,

    status VARCHAR(20) DEFAULT 'DRAFT',

    sub_total DECIMAL(19,2) DEFAULT 0,
    discount DECIMAL(19,2) DEFAULT 0,
    tax DECIMAL(19,2) DEFAULT 0,
    total_amount DECIMAL(19,2) DEFAULT 0,

    payment_terms VARCHAR(500),
    delivery_terms VARCHAR(500),
    delivery_location VARCHAR(255),

    currency VARCHAR(10) DEFAULT 'INR',
    notes VARCHAR(1000),

    -- Lifecycle Tracking
    sent_at TIMESTAMP,
    approved_at TIMESTAMP,
    rejected_at TIMESTAMP,
    rejection_reason VARCHAR(500),
    viewed_at TIMESTAMP,

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    -- Foreign Keys
    CONSTRAINT fk_quotation_customer
    FOREIGN KEY (customer_id)
    REFERENCES customer(id)
    ON DELETE RESTRICT,

    CONSTRAINT fk_quotation_enquiry
    FOREIGN KEY (enquiry_id)
    REFERENCES enquiry(id)
    ON DELETE SET NULL,

    -- Status Validation
    CONSTRAINT chk_quotation_status
    CHECK (status IN ('DRAFT', 'SENT', 'REVISED', 'APPROVED', 'CANCELLED', 'EXPIRED'))
    );

-------------------------------------------------------
-- QUOTATION ITEMS TABLE
-------------------------------------------------------
CREATE TABLE IF NOT EXISTS quotation_items (

                                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    quotation_id UUID NOT NULL,

    -- Part Info
    part_name VARCHAR(255),
    drawing_number VARCHAR(100),
    material_grade VARCHAR(100),

    -- Metal & Casting (NEW)
    metal_type VARCHAR(50),
    casting_process VARCHAR(50),

    -- Weight
    net_weight_kg DECIMAL(10,3),
    gross_weight_kg DECIMAL(10,3),

    -- Pattern Info
    pattern_status VARCHAR(20),
    pattern_provided_by_customer BOOLEAN,
    pattern_id UUID,
    pattern_receipt_id UUID,

    -- Pricing
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(19,2),
    line_total DECIMAL(19,2),

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    -- Foreign Keys
    CONSTRAINT fk_quotation_item_quotation
    FOREIGN KEY (quotation_id)
    REFERENCES quotations(id)
    ON DELETE CASCADE,

    CONSTRAINT fk_quotation_item_pattern
    FOREIGN KEY (pattern_id)
    REFERENCES patterns(id)
    ON DELETE SET NULL,

    CONSTRAINT fk_quotation_item_pattern_receipt
    FOREIGN KEY (pattern_receipt_id)
    REFERENCES pattern_receipt(id)
    ON DELETE SET NULL,

    -- Pattern Status Validation
    CONSTRAINT chk_pattern_status
    CHECK (pattern_status IS NULL OR pattern_status IN ('AVAILABLE', 'TO_BE_MADE', 'CUSTOMER_SUPPLY'))
    );

-------------------------------------------------------
-- INDEXES FOR QUOTATIONS
-------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_quotations_customer
    ON quotations(customer_id);

CREATE INDEX IF NOT EXISTS idx_quotations_enquiry
    ON quotations(enquiry_id);

CREATE INDEX IF NOT EXISTS idx_quotations_status
    ON quotations(status);

CREATE INDEX IF NOT EXISTS idx_quotations_date
    ON quotations(quotation_date);

CREATE INDEX IF NOT EXISTS idx_quotations_number
    ON quotations(quotation_number);

CREATE INDEX IF NOT EXISTS idx_quotations_valid_until
    ON quotations(valid_until);

-------------------------------------------------------
-- INDEXES FOR QUOTATION ITEMS
-------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_quotation_items_quotation
    ON quotation_items(quotation_id);

CREATE INDEX IF NOT EXISTS idx_quotation_items_pattern
    ON quotation_items(pattern_id);

CREATE INDEX IF NOT EXISTS idx_quotation_items_pattern_receipt
    ON quotation_items(pattern_receipt_id);

CREATE INDEX IF NOT EXISTS idx_quotation_items_part_name
    ON quotation_items(part_name);

CREATE INDEX IF NOT EXISTS idx_quotation_items_metal_type
    ON quotation_items(metal_type);