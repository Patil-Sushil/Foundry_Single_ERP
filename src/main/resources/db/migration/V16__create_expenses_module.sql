------------------------------------
-- Expense Heads
------------------------------------
CREATE TABLE expense_heads
(
    id UUID PRIMARY KEY,

    name VARCHAR(255) NOT NULL,

    category VARCHAR(50) NOT NULL,

    description TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    CONSTRAINT uk_expense_head_name_category
        UNIQUE (name, category)
);

------------------------------------
-- Expenses
------------------------------------
CREATE TABLE expenses
(
    id UUID PRIMARY KEY,

    expense_number VARCHAR(50) NOT NULL UNIQUE,

    expense_head_id UUID NOT NULL,

    amount DECIMAL(12,2) NOT NULL CHECK (amount > 0),

    expense_date DATE NOT NULL,

    payment_mode VARCHAR(50),

    reference_number VARCHAR(100),

    description TEXT,

    remarks TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    CONSTRAINT fk_expense_head
        FOREIGN KEY (expense_head_id)
            REFERENCES expense_heads(id)
            ON DELETE RESTRICT
);

------------------------------------
-- Index for fast reporting
------------------------------------
CREATE INDEX idx_expenses_date
    ON expenses(expense_date);

CREATE INDEX idx_expenses_head
    ON expenses(expense_head_id);