-- Asegura unicidad de email en user_accounts
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE schemaname='public'
          AND indexname='ux_user_accounts_email'
    ) THEN
CREATE UNIQUE INDEX ux_user_accounts_email ON public.user_accounts (email);
END IF;
END;
$$;
