DO $$
    DECLARE
r RECORD;
        v_max BIGINT;
        v_min BIGINT;
BEGIN
FOR r IN
SELECT
    ns.nspname  AS schema_name,
    seq.relname AS seq_name,
    tab.relname AS table_name,
    col.attname AS column_name
FROM pg_class seq
         JOIN pg_namespace ns ON ns.oid = seq.relnamespace
         JOIN pg_depend dep   ON dep.objid = seq.oid AND dep.deptype = 'a'
         JOIN pg_class tab    ON tab.oid = dep.refobjid
         JOIN pg_attribute col ON col.attrelid = tab.oid AND col.attnum = dep.refobjsubid
WHERE seq.relkind = 'S'
  AND ns.nspname NOT IN ('pg_catalog', 'information_schema')
    LOOP
                -- max(id) của bảng
                EXECUTE format('SELECT MAX(%I) FROM %I.%I', r.column_name, r.schema_name, r.table_name)
INTO v_max;

-- minvalue của sequence
EXECUTE format(
        'SELECT s.seqmin FROM pg_sequence s
         JOIN pg_class c ON c.oid = s.seqrelid
         JOIN pg_namespace n ON n.oid = c.relnamespace
         WHERE n.nspname = %L AND c.relname = %L',
        r.schema_name, r.seq_name
        ) INTO v_min;

IF v_min IS NULL THEN
                    v_min := 1;
END IF;

                IF v_max IS NULL THEN
                    -- bảng rỗng: set về minvalue và is_called=false để nextval ra đúng minvalue
                    EXECUTE format(
                            'SELECT setval(%L::regclass, %s, false)',
                            r.schema_name || '.' || r.seq_name,
                            v_min
                            );
ELSE
                    -- bảng có dữ liệu: set về max(id)
                    EXECUTE format(
                            'SELECT setval(%L::regclass, %s, true)',
                            r.schema_name || '.' || r.seq_name,
                            v_max
                            );
END IF;
END LOOP;
END $$;
