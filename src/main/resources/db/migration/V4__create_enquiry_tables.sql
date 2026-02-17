-- =============================================
-- V4: Enquiry Tables
-- =============================================

CREATE TABLE IF NOT EXISTS enquiry (
                                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    enquiry_no VARCHAR(50) NOT NULL UNIQUE,
    enquiry_date DATE NOT NULL,

    customer_id UUID NOT NULL,

    total_weight_kg NUMERIC(12,3) NOT NULL,
    expected_delivery_date DATE,
    status VARCHAR(30) DEFAULT 'NEW',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    CONSTRAINT fk_enquiry_customer
    FOREIGN KEY (customer_id)
    REFERENCES customer(id)
    ON DELETE RESTRICT
    );

CREATE TABLE IF NOT EXISTS enquiry_item (
                                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    enquiry_id UUID NOT NULL,

    part_name VARCHAR(150) NOT NULL,
    metal_category_id BIGINT NOT NULL,
    metal_type_id BIGINT NOT NULL,
    required_quantity INT NOT NULL,
    approx_piece_weight_kg NUMERIC(10,3) NOT NULL,
    total_weight_kg NUMERIC(12,3) NOT NULL,
    casting_process VARCHAR(50) NOT NULL,
    pattern_available BOOLEAN NOT NULL,
    machine_required BOOLEAN NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    FOREIGN KEY (enquiry_id) REFERENCES enquiry(id) ON DELETE CASCADE,
    FOREIGN KEY (metal_category_id) REFERENCES metal_categories(id),
    FOREIGN KEY (metal_type_id) REFERENCES metal_types(id)
    );

CREATE INDEX idx_enquiry_customer ON enquiry(customer_id);
CREATE INDEX idx_enquiry_status ON enquiry(status);
CREATE INDEX idx_enquiry_date ON enquiry(enquiry_date);
CREATE INDEX idx_enquiry_item_enquiry ON enquiry_item(enquiry_id);
