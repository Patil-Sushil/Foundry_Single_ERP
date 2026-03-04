-------------------------------------------------------
-- V4__create_quotation_tables.sql
-------------------------------------------------------

-------------------------------------------------------
-- Create quotations table
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

    -- Lifecycle Tracking Fields
    sent_at TIMESTAMP,
    approved_at TIMESTAMP,
    rejected_at TIMESTAMP,
    rejection_reason VARCHAR(500),
    viewed_at TIMESTAMP,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    CONSTRAINT fk_quotation_customer
    FOREIGN KEY (customer_id) REFERENCES customer(id),

    CONSTRAINT fk_quotation_enquiry
    FOREIGN KEY (enquiry_id) REFERENCES enquiry(id)
    );

-------------------------------------------------------
-- Create quotation_items table
-------------------------------------------------------

CREATE TABLE IF NOT EXISTS quotation_items (

                                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    quotation_id UUID NOT NULL,

    part_name VARCHAR(255),
    drawing_number VARCHAR(100),
    material_grade VARCHAR(100),

    net_weight_kg DECIMAL(10,3),
    gross_weight_kg DECIMAL(10,3),

    pattern_status VARCHAR(20),

    quantity DECIMAL(15,3),
    unit_price DECIMAL(19,2),
    line_total DECIMAL(19,2),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    CONSTRAINT fk_quotation_item
    FOREIGN KEY (quotation_id)
    REFERENCES quotations(id)
    ON DELETE CASCADE
    );

-------------------------------------------------------
-- Indexes
-------------------------------------------------------

CREATE INDEX idx_quotations_customer
    ON quotations(customer_id);

CREATE INDEX idx_quotations_enquiry
    ON quotations(enquiry_id);

CREATE INDEX idx_quotations_status
    ON quotations(status);

CREATE INDEX idx_quotations_date
    ON quotations(quotation_date);

CREATE INDEX idx_quotations_number
    ON quotations(quotation_number);

CREATE INDEX idx_quotation_items_quotation
    ON quotation_items(quotation_id);