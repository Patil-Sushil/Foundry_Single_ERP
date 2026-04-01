-- V11: Fix User ID types in Inventory
-- =============================================

ALTER TABLE purchase_orders 
    ALTER COLUMN created_by_user_id TYPE UUID USING (gen_random_uuid()); -- Use dummy or null for now, or just change type

-- Since there are no real foreign keys to users in BIGINT, we can safely change the type.
-- We'll set them to NULL and then to NOT NULL again if needed.

ALTER TABLE purchase_orders ALTER COLUMN created_by_user_id DROP NOT NULL;
ALTER TABLE purchase_orders ALTER COLUMN created_by_user_id TYPE UUID USING NULL;

ALTER TABLE material_inwards ALTER COLUMN created_by_user_id DROP NOT NULL;
ALTER TABLE material_inwards ALTER COLUMN created_by_user_id TYPE UUID USING NULL;
ALTER TABLE material_inwards ALTER COLUMN confirmed_by_user_id TYPE UUID USING NULL;

ALTER TABLE material_issues ALTER COLUMN issued_by_user_id DROP NOT NULL;
ALTER TABLE material_issues ALTER COLUMN issued_by_user_id TYPE UUID USING NULL;
