-- ==============================
-- METAL MASTER TABLES
-- ==============================

CREATE TABLE IF NOT EXISTS metal_categories (
                                                id BIGSERIAL PRIMARY KEY,
                                                name VARCHAR(100) UNIQUE NOT NULL,
    active BOOLEAN DEFAULT TRUE
    );

CREATE TABLE IF NOT EXISTS metal_types (
                                           id BIGSERIAL PRIMARY KEY,
                                           category_id BIGINT NOT NULL,
                                           name VARCHAR(100) NOT NULL,
    active BOOLEAN DEFAULT TRUE,

    CONSTRAINT fk_metal_type_category
    FOREIGN KEY (category_id)
    REFERENCES metal_categories(id)
    );

-- ==============================
-- ENQUIRY (HEADER)
-- ==============================

CREATE TABLE IF NOT EXISTS enquiry (
                                       id UUID PRIMARY KEY,
                                       tenant_id BIGINT NOT NULL,
                                       enquiry_no VARCHAR(50) NOT NULL,
    enquiry_date DATE NOT NULL,
    customer_id UUID NOT NULL,
    total_weight_kg NUMERIC(12,3) NOT NULL,
    expected_delivery_date DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'NEW',

    CONSTRAINT uq_enquiry UNIQUE (tenant_id, enquiry_no)
    );

-- ==============================
-- ENQUIRY ITEMS (LINES)
-- ==============================

CREATE TABLE IF NOT EXISTS enquiry_item (
                                            id BIGSERIAL PRIMARY KEY,
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

    CONSTRAINT fk_enquiry_item_enquiry
    FOREIGN KEY (enquiry_id)
    REFERENCES enquiry(id)
    ON DELETE CASCADE,

    CONSTRAINT fk_enquiry_item_metal_category
    FOREIGN KEY (metal_category_id)
    REFERENCES metal_categories(id),

    CONSTRAINT fk_enquiry_item_metal_type
    FOREIGN KEY (metal_type_id)
    REFERENCES metal_types(id)
    );
