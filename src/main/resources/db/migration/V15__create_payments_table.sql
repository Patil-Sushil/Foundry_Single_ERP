---------------------------------
-- V15__PAYMENTS TABLE (Updated)
---------------------------------

-- Drop existing table if needed (CAREFUL in production!)
-- DROP TABLE IF EXISTS payments CASCADE;

CREATE TABLE payments
(
    -- ══════════════════════════════════════════════
    -- PRIMARY KEY
    -- ══════════════════════════════════════════════
    id                  UUID            PRIMARY KEY,

    payment_number      VARCHAR(50)     NOT NULL UNIQUE,

    -- ══════════════════════════════════════════════
    -- FOREIGN KEYS
    -- ══════════════════════════════════════════════
    invoice_id          UUID            NOT NULL,
    customer_id         UUID            NOT NULL,

    -- ══════════════════════════════════════════════
    -- CORE FIELDS
    -- ══════════════════════════════════════════════
    payment_date        DATE            NOT NULL,
    payment_method      VARCHAR(50)     NOT NULL,
    amount_paid         DECIMAL(14, 2)  NOT NULL,
    status              VARCHAR(50)     NOT NULL DEFAULT 'SUCCESS',

    -- ══════════════════════════════════════════════
    -- UPI / CARD / NEFT / RTGS / IMPS / BANK_TRANSFER
    -- ══════════════════════════════════════════════
    transaction_id      VARCHAR(100),

    -- ══════════════════════════════════════════════
    -- CHEQUE / DEMAND DRAFT
    -- ══════════════════════════════════════════════
    instrument_number   VARCHAR(20),
    instrument_date     DATE,
    bank_name           VARCHAR(100),
    branch_name         VARCHAR(100),

    -- ══════════════════════════════════════════════
    -- GENERAL
    -- ══════════════════════════════════════════════
    reference_number    VARCHAR(100),
    remarks             VARCHAR(500),
    receipt_url         VARCHAR(500),
    received_by         VARCHAR(100),
    cancellation_reason VARCHAR(500),

    -- ══════════════════════════════════════════════
    -- AUDIT
    -- ══════════════════════════════════════════════
    created_at          TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),

    -- ══════════════════════════════════════════════
    -- CONSTRAINTS
    -- ══════════════════════════════════════════════
    CONSTRAINT fk_payment_invoice
        FOREIGN KEY (invoice_id)
            REFERENCES invoices (id),

    CONSTRAINT fk_payment_customer
        FOREIGN KEY (customer_id)
            REFERENCES customer (id),

    CONSTRAINT chk_amount_positive
        CHECK (amount_paid > 0),

    CONSTRAINT chk_payment_method
        CHECK (payment_method IN (
                                  'CASH', 'UPI', 'BANK_TRANSFER', 'CHEQUE',
                                  'CARD', 'DEMAND_DRAFT', 'NEFT', 'RTGS', 'IMPS'
            )),

    CONSTRAINT chk_payment_status
        CHECK (status IN (
                          'PENDING', 'SUCCESS', 'FAILED','PARTIAL',
                          'CANCELLED', 'REFUNDED', 'BOUNCED'
            ))
);

-- ══════════════════════════════════════════════════
-- INDEXES
-- ══════════════════════════════════════════════════

CREATE UNIQUE INDEX idx_payment_number
    ON payments (payment_number);

CREATE INDEX idx_payment_invoice
    ON payments (invoice_id);

CREATE INDEX idx_payment_customer
    ON payments (customer_id);

CREATE INDEX idx_payment_date
    ON payments (payment_date);

CREATE INDEX idx_payment_status
    ON payments (status);

CREATE INDEX idx_payment_method
    ON payments (payment_method);

CREATE INDEX idx_payment_date_status
    ON payments (payment_date, status);

CREATE INDEX idx_payment_transaction_id
    ON payments (transaction_id)
    WHERE transaction_id IS NOT NULL;

CREATE INDEX idx_payment_instrument_number
    ON payments (instrument_number)
    WHERE instrument_number IS NOT NULL;

