-- ============================================
-- V36__create_credit_note_module.sql
-- ============================================

CREATE TABLE IF NOT EXISTS credit_notes (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    credit_note_number  VARCHAR(50) UNIQUE NOT NULL,
    customer_id         UUID NOT NULL,
    order_id            UUID,
    customer_return_id  BIGINT,
    
    issue_date          DATE NOT NULL,
    reason              TEXT,
    
    subtotal            DECIMAL(19,2) DEFAULT 0,
    gst_type            VARCHAR(20),
    gst_percentage      DECIMAL(5,2) DEFAULT 18,
    cgst                DECIMAL(19,2) DEFAULT 0,
    sgst                DECIMAL(19,2) DEFAULT 0,
    igst                DECIMAL(19,2) DEFAULT 0,
    total_gst           DECIMAL(19,2) DEFAULT 0,
    total_amount        DECIMAL(19,2) DEFAULT 0,
    
    status              VARCHAR(20) DEFAULT 'ISSUED',
    
    -- AUDIT FIELDS
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    
    CONSTRAINT fk_cn_customer FOREIGN KEY (customer_id) REFERENCES customer(id),
    CONSTRAINT fk_cn_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_cn_return FOREIGN KEY (customer_return_id) REFERENCES qa_customer_returns(id),
    CONSTRAINT chk_cn_status CHECK (status IN ('DRAFT', 'ISSUED', 'APPLIED', 'CANCELLED')),
    CONSTRAINT chk_cn_gst_type CHECK (gst_type IS NULL OR gst_type IN ('CGST_SGST', 'IGST'))
);

CREATE INDEX idx_cn_customer_id ON credit_notes(customer_id);
CREATE INDEX idx_cn_order_id ON credit_notes(order_id);
CREATE INDEX idx_cn_return_id ON credit_notes(customer_return_id);
CREATE INDEX idx_cn_status ON credit_notes(status);
CREATE INDEX idx_cn_number ON credit_notes(credit_note_number);
