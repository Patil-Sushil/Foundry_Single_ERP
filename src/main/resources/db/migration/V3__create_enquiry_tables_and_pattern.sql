-------------------------------------------------------
-- V3__create_enquiry_tables_and_pattern.sql
-------------------------------------------------------

-------------------------------------------------------
-- EXTENSION (UUID)
-------------------------------------------------------
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-------------------------------------------------------
-- PATTERN NUMBER SEQUENCE
-------------------------------------------------------
CREATE SEQUENCE IF NOT EXISTS pattern_number_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-------------------------------------------------------
-- PATTERN MASTER
-------------------------------------------------------
CREATE TABLE IF NOT EXISTS patterns (
                                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pattern_number VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    type VARCHAR(50) NOT NULL,
    material VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',
    rack_number VARCHAR(100),
    tenant_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    CONSTRAINT chk_pattern_status
    CHECK (status IN ('AVAILABLE','IN_USE','UNDER_MAINTENANCE','SCRAPPED')),
    CONSTRAINT chk_pattern_type
    CHECK (type IN ('SPLIT_PATTERN',
           'MATCH_PLATE_PATTERN',
           'COPE_AND_DRAG_PATTERN',
           'GATED_PATTERN',
           'LOOSE_PIECE_PATTERN')),
    CONSTRAINT chk_pattern_material
    CHECK (material IN ('TEAK_WOOD',
           'PINE_WOOD',
           'CAST_IRON',
           'STEEL',
           'ALUMINIUM',
           'PLASTIC',
           'RESIN'))
    );

CREATE INDEX IF NOT EXISTS idx_patterns_name ON patterns(name);
CREATE INDEX IF NOT EXISTS idx_patterns_status ON patterns(status);
CREATE INDEX IF NOT EXISTS idx_patterns_rack ON patterns(rack_number);

-------------------------------------------------------
-- PATTERN RECEIPT
-------------------------------------------------------
CREATE TABLE IF NOT EXISTS pattern_receipt (
                                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inward_date DATE,
    outward_date DATE,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    material VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    CONSTRAINT chk_pattern_receipt_type
    CHECK (type IN ('SPLIT_PATTERN',
           'MATCH_PLATE_PATTERN',
           'COPE_AND_DRAG_PATTERN',
           'GATED_PATTERN',
           'LOOSE_PIECE_PATTERN')),
    CONSTRAINT chk_pattern_receipt_material
    CHECK (material IN ('TEAK_WOOD',
           'PINE_WOOD',
           'CAST_IRON',
           'STEEL',
           'ALUMINIUM',
           'PLASTIC',
           'RESIN'))
    );

CREATE INDEX IF NOT EXISTS idx_pattern_receipt_name ON pattern_receipt(name);
CREATE INDEX IF NOT EXISTS idx_pattern_receipt_inward ON pattern_receipt(inward_date);

-------------------------------------------------------
-- ENQUIRY
-------------------------------------------------------
CREATE TABLE IF NOT EXISTS enquiry (
                                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    enquiry_no VARCHAR(50) NOT NULL UNIQUE,
    enquiry_date DATE NOT NULL,
    customer_id UUID NOT NULL,
    total_weight_kg NUMERIC(12,3) NOT NULL,
    expected_delivery_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    CONSTRAINT fk_enquiry_customer
    FOREIGN KEY (customer_id)
    REFERENCES customer(id)
    ON DELETE RESTRICT,
    CONSTRAINT chk_enquiry_status
    CHECK (status IN ('PENDING','QUOTED','CLOSED')),
    CONSTRAINT chk_total_weight_positive
    CHECK (total_weight_kg > 0)
    );

CREATE INDEX IF NOT EXISTS idx_enquiry_customer ON enquiry(customer_id);
CREATE INDEX IF NOT EXISTS idx_enquiry_status ON enquiry(status);
CREATE INDEX IF NOT EXISTS idx_enquiry_date ON enquiry(enquiry_date);

-------------------------------------------------------
-- ENQUIRY ITEM
-------------------------------------------------------
CREATE TABLE IF NOT EXISTS enquiry_item (
                                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    enquiry_id UUID NOT NULL,
    part_name VARCHAR(150) NOT NULL,
    metal_category VARCHAR(50) NOT NULL,
    material_grade VARCHAR(100),
    metal_type VARCHAR(50) NOT NULL,
    required_quantity INT NOT NULL,
    approx_piece_weight_kg NUMERIC(10,3) NOT NULL,
    total_weight_kg NUMERIC(12,3) NOT NULL,
    casting_process VARCHAR(50) NOT NULL,
    pattern_provided_by VARCHAR(20) NOT NULL,
    machine_required BOOLEAN NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    CONSTRAINT fk_enquiry_item_enquiry
    FOREIGN KEY (enquiry_id)
    REFERENCES enquiry(id)
    ON DELETE CASCADE,
    CONSTRAINT chk_pattern_source
    CHECK (pattern_provided_by IN ('CUSTOMER','COMPANY')),
    CONSTRAINT chk_metal_category
    CHECK (metal_category IN ('FERROUS','NON_FERROUS')),
    CONSTRAINT chk_required_quantity
    CHECK (required_quantity > 0),
    CONSTRAINT chk_weight_positive
    CHECK (
              approx_piece_weight_kg > 0
              AND total_weight_kg > 0
          )
    );

CREATE INDEX IF NOT EXISTS idx_enquiry_item_enquiry ON enquiry_item(enquiry_id);