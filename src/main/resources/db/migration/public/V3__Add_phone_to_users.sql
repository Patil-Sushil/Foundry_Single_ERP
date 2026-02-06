ALTER TABLE public.users
    ADD COLUMN IF NOT EXISTS phone VARCHAR(10);

CREATE UNIQUE INDEX IF NOT EXISTS ux_users_phone
    ON public.users(phone);
