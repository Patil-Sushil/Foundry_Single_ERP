
---------------------------------
-- PAYMENTS TABLE
---------------------------------

CREATE TABLE payments
(
    id UUID PRIMARY KEY,

    payment_number VARCHAR(50) NOT NULL UNIQUE,

    invoice_id UUID NOT NULL,
    customer_id UUID NOT NULL,

    payment_date DATE,

    payment_method VARCHAR(50),

    amount_paid DECIMAL(12,2) NOT NULL,

    reference_number VARCHAR(100),

    remarks TEXT,

    status VARCHAR(50),

    created_at TIMESTAMP,
    updated_at TIMESTAMP,

    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    CONSTRAINT fk_payment_invoice
        FOREIGN KEY (invoice_id)
            REFERENCES invoices(id),

    CONSTRAINT fk_payment_customer
        FOREIGN KEY (customer_id)
            REFERENCES customer(id)
);

CREATE INDEX idx_payment_date_status
    ON payments(payment_date, status);

CREATE INDEX idx_payment_date
    ON payments(payment_date);

