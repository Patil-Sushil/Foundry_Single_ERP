-- EXTENSION (required for UUID generation)

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- PATTERN MASTER

CREATE TABLE patterns (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                          name VARCHAR(150) NOT NULL,
                          type VARCHAR(50) NOT NULL,
                          material VARCHAR(50) NOT NULL,

                          tenant_id UUID,

                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP,
                          created_by VARCHAR(255),
                          updated_by VARCHAR(255)
);

CREATE INDEX idx_patterns_name ON patterns(name);

-- PATTERN RECEIPT (Customer provided patterns)

CREATE TABLE pattern_receipt (
                                 id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                                 inward_date DATE,
                                 outward_date DATE,
                                 name VARCHAR(255) NOT NULL,
                                 type VARCHAR(50) NOT NULL,
                                 material VARCHAR(50) NOT NULL,
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP,
                                 created_by VARCHAR(255),
                                 updated_by VARCHAR(255)
);

-- ENQUIRY--

CREATE TABLE enquiry (
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

CREATE INDEX idx_enquiry_customer ON enquiry(customer_id);
CREATE INDEX idx_enquiry_status ON enquiry(status);
CREATE INDEX idx_enquiry_date ON enquiry(enquiry_date);

-- ENQUIRY ITEM (ENUM BASED)

CREATE TABLE enquiry_item (
                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                              enquiry_id UUID NOT NULL,

                              part_name VARCHAR(150) NOT NULL,

                             -- ENUM stored as STRING
                              metal_category VARCHAR(50) NOT NULL,
                              metal_type VARCHAR(50) NOT NULL,

                              required_quantity INT NOT NULL,
                              approx_piece_weight_kg NUMERIC(10,3) NOT NULL,
                              total_weight_kg NUMERIC(12,3) NOT NULL,

                              casting_process VARCHAR(50) NOT NULL,

                              pattern_provided_by_customer BOOLEAN NOT NULL,
                              pattern_id UUID,
                              pattern_receipt_id UUID,

                              machine_required BOOLEAN NOT NULL,

                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP,
                              created_by VARCHAR(255),
                              updated_by VARCHAR(255),

                              CONSTRAINT fk_enquiry_item_enquiry
                                  FOREIGN KEY (enquiry_id)
                                      REFERENCES enquiry(id)
                                      ON DELETE CASCADE,

                              CONSTRAINT fk_enquiry_item_pattern
                                  FOREIGN KEY (pattern_id)
                                      REFERENCES patterns(id),

                              CONSTRAINT fk_enquiry_item_pattern_receipt
                                  FOREIGN KEY (pattern_receipt_id)
                                      REFERENCES pattern_receipt(id),

                              CONSTRAINT chk_pattern_logic
                                  CHECK (
                                      (pattern_provided_by_customer = TRUE AND pattern_receipt_id IS NOT NULL)
                                          OR
                                      (pattern_provided_by_customer = FALSE AND pattern_id IS NOT NULL)
                                      ),

                              -- Optional: Enforce valid categories
                              CONSTRAINT chk_metal_category
                                  CHECK (metal_category IN ('FERROUS','NON_FERROUS'))
);

CREATE INDEX idx_enquiry_item_enquiry ON enquiry_item(enquiry_id);
CREATE INDEX idx_enquiry_item_pattern ON enquiry_item(pattern_id);
CREATE INDEX idx_enquiry_item_pattern_receipt ON enquiry_item(pattern_receipt_id);