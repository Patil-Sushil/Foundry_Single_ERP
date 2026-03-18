-- V18__create_labor_management_module.sql

CREATE TABLE IF NOT EXISTS laborers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    wage_type VARCHAR(50) NOT NULL, -- 'HOURLY' or 'PIECE_RATE'
    daily_wage DECIMAL(19, 2),
    piece_rate DECIMAL(19, 2),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS attendance (
    id BIGSERIAL PRIMARY KEY,
    laborer_id BIGINT NOT NULL REFERENCES laborers(id),
    work_date DATE NOT NULL,
    check_in_time TIME,
    check_out_time TIME,
    hours_worked DECIMAL(10, 2),
    pieces_completed INTEGER,
    earned_amount DECIMAL(19, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (laborer_id, work_date)
);

CREATE TABLE IF NOT EXISTS advance_transactions (
    id BIGSERIAL PRIMARY KEY,
    laborer_id BIGINT NOT NULL REFERENCES laborers(id),
    transaction_date DATE NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    transaction_type VARCHAR(50) NOT NULL, -- 'GIVEN' or 'DEDUCTED'
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS weekly_payouts (
    id BIGSERIAL PRIMARY KEY,
    laborer_id BIGINT NOT NULL REFERENCES laborers(id),
    week_start_date DATE NOT NULL,
    week_end_date DATE NOT NULL,
    total_hours DECIMAL(10, 2),
    gross_payout DECIMAL(19, 2) NOT NULL,
    advance_deduction DECIMAL(19, 2) NOT NULL,
    net_payout DECIMAL(19, 2) NOT NULL,
    payment_status VARCHAR(50) NOT NULL, -- 'PENDING' or 'PAID'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    pieces_completed DECIMAL(19,2),
    UNIQUE (laborer_id, week_start_date, week_end_date)
);
ALTER TABLE weekly_payouts ADD COLUMN IF NOT EXISTS payment_date DATE;
ALTER TABLE weekly_payouts ADD COLUMN IF NOT EXISTS payment_reference VARCHAR(255);


-- Add hourly_rate to laborers table
ALTER TABLE laborers ADD COLUMN hourly_rate DECIMAL(19, 2);

-- Add audit snapshot columns to attendance table
ALTER TABLE attendance ADD COLUMN wage_type_snapshot VARCHAR(20) NOT NULL DEFAULT 'DAILY';
ALTER TABLE attendance ADD COLUMN applied_rate DECIMAL(19, 2) NOT NULL DEFAULT 0.00;

-- Remove defaults after backfill
ALTER TABLE attendance ALTER COLUMN wage_type_snapshot DROP DEFAULT;
ALTER TABLE attendance ALTER COLUMN applied_rate DROP DEFAULT;
