-- Create purchase_invoices table for vendor invoice tracking

CREATE TABLE purchase_invoices (
    id                      BIGSERIAL PRIMARY KEY,
    vendor_invoice_number   VARCHAR(50)     NOT NULL,
    vendor_invoice_date     DATE            NOT NULL,
    invoice_amount          DECIMAL(12, 2),
    vendor_id               BIGINT          NOT NULL,
    purchase_order_id       BIGINT,
    material_inward_id      BIGINT,
    remarks                 VARCHAR(500),
    source                  VARCHAR(10)     NOT NULL DEFAULT 'AUTO',
    is_verified             BOOLEAN         NOT NULL DEFAULT FALSE,
    verified_by_user_id     BIGINT,
    verified_at             TIMESTAMP,
    created_at              TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    created_by_user_id      BIGINT,
    updated_at              TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_vendor_invoice UNIQUE (vendor_id, vendor_invoice_number),
    CONSTRAINT fk_pi_vendor FOREIGN KEY (vendor_id) REFERENCES vendors(id),
    CONSTRAINT fk_pi_purchase_order FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id),
    CONSTRAINT fk_pi_material_inward FOREIGN KEY (material_inward_id) REFERENCES material_inwards(id),
    CONSTRAINT chk_pi_source CHECK (source IN ('AUTO', 'MANUAL'))
);

CREATE INDEX idx_pi_vendor ON purchase_invoices(vendor_id);
CREATE INDEX idx_pi_purchase_order ON purchase_invoices(purchase_order_id);
CREATE INDEX idx_pi_material_inward ON purchase_invoices(material_inward_id);
CREATE INDEX idx_pi_invoice_date ON purchase_invoices(vendor_invoice_date);
CREATE INDEX idx_pi_verified ON purchase_invoices(is_verified);
CREATE INDEX idx_pi_vendor_date ON purchase_invoices(vendor_id, vendor_invoice_date);

-- Add invoice fields to material_inwards for draft stage storage
ALTER TABLE material_inwards ADD COLUMN vendor_invoice_number VARCHAR(50) DEFAULT NULL;
ALTER TABLE material_inwards ADD COLUMN vendor_invoice_date DATE DEFAULT NULL;
