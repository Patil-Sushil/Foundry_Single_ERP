-- V6__enquiry_customer_id_uuid.sql
-- FIX enquiry.customer_id → UUID (from BIGINT)

-- This migration safely converts customer_id from BIGINT to UUID
-- It's idempotent and can be run multiple times safely

DO $$
BEGIN
    -- Check if customer_id exists and is BIGINT (needs conversion)
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_schema = CURRENT_SCHEMA()
        AND table_name = 'enquiry' 
        AND column_name = 'customer_id'
        AND data_type = 'bigint'
    ) THEN
        -- 1. Drop FK constraint if exists
ALTER TABLE enquiry DROP CONSTRAINT IF EXISTS enquiry_customer_id_fkey;

-- 2. Drop old BIGINT column
ALTER TABLE enquiry DROP COLUMN customer_id;

-- 3. Add new UUID column
ALTER TABLE enquiry ADD COLUMN customer_id UUID NOT NULL;

-- 4. Add FK constraint
ALTER TABLE enquiry
    ADD CONSTRAINT enquiry_customer_id_fkey
        FOREIGN KEY (customer_id)
            REFERENCES customer(id)
            ON DELETE RESTRICT;

RAISE NOTICE 'Successfully converted enquiry.customer_id from BIGINT to UUID';
        
    -- Check if customer_id already exists as UUID (migration already applied)
    ELSIF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_schema = CURRENT_SCHEMA()
        AND table_name = 'enquiry' 
        AND column_name = 'customer_id'
        AND data_type = 'uuid'
    ) THEN
        -- Column already correct type, just ensure FK exists
        IF NOT EXISTS (
            SELECT 1 
            FROM information_schema.table_constraints 
            WHERE constraint_schema = CURRENT_SCHEMA()
            AND table_name = 'enquiry' 
            AND constraint_name = 'enquiry_customer_id_fkey'
        ) THEN
ALTER TABLE enquiry
    ADD CONSTRAINT enquiry_customer_id_fkey
        FOREIGN KEY (customer_id)
            REFERENCES customer(id)
            ON DELETE RESTRICT;
END IF;
        
        RAISE NOTICE 'enquiry.customer_id already UUID - no changes needed';
        
    -- Column doesn't exist at all (fresh migration)
ELSE
        -- Add new UUID column
ALTER TABLE enquiry ADD COLUMN customer_id UUID NOT NULL;

-- Add FK constraint
ALTER TABLE enquiry
    ADD CONSTRAINT enquiry_customer_id_fkey
        FOREIGN KEY (customer_id)
            REFERENCES customer(id)
            ON DELETE RESTRICT;

RAISE NOTICE 'Created enquiry.customer_id as UUID';
END IF;

EXCEPTION
    WHEN OTHERS THEN
        RAISE EXCEPTION 'Error during customer_id migration: %', SQLERRM;
END $$;