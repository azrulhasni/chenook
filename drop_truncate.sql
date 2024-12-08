-- FUNCTION: public.drop_tables()

-- DROP FUNCTION IF EXISTS public.drop_tables();

CREATE OR REPLACE FUNCTION public.drop_tables(
	)
    RETURNS void
    LANGUAGE 'sql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
DO $$ DECLARE
    table_name text;
BEGIN
    FOR table_name IN (SELECT tablename FROM pg_tables WHERE schemaname='public') LOOP
        IF table_name <> 'location' THEN
			EXECUTE 'DROP TABLE finapp."' || table_name || '" CASCADE;';
		END IF;
    END LOOP;
END $$;
$BODY$;

ALTER FUNCTION public.drop_tables()
    OWNER TO postgres;




-- =================================================
-- FUNCTION: public.truncate_tables()

-- DROP FUNCTION IF EXISTS public.truncate_tables();

CREATE OR REPLACE FUNCTION public.truncate_tables(
	)
    RETURNS void
    LANGUAGE 'sql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
DO $$ DECLARE
    table_name text;
BEGIN
    FOR table_name IN (SELECT tablename FROM pg_tables WHERE schemaname='public') LOOP
        IF table_name <> 'location' THEN
			EXECUTE 'TRUNCATE TABLE public."' || table_name || '" CASCADE;';
		END IF;
    END LOOP;
END $$;
$BODY$;

ALTER FUNCTION public.truncate_tables()
    OWNER TO finapp;

