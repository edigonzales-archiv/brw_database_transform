select  
        tc.constraint_name, 
        tc.constraint_schema || '.' || tc.table_name || '.' || kcu.column_name as physical_full_name,  
        tc.constraint_schema,
        tc.table_name, 
        kcu.column_name, 
        --ccu.table_name as foreign_table_name, 
        --ccu.column_name as foreign_column_name,
        tc.constraint_type
    from 
        information_schema.table_constraints as tc  
        join information_schema.key_column_usage as kcu on (tc.constraint_name = kcu.constraint_name and tc.table_name = kcu.table_name)
        --join information_schema.constraint_column_usage as ccu on ccu.constraint_name = tc.constraint_name

    where constraint_type = 'CHECK'

select *
FROM information_schema.table_constraints    
WHERE constraint_type = 'CHECK'     

-- hier findet man alle check constraints, ich denke man kann ebenfalls relativ zuverlässig prüfen, ob es sich um einen srid-check handelt.
-- dann gibts hier die tabellen oid
SELECT consrc, *
FROM pg_constraint

-- mit tabellen oid gibts hier schema
SELECT *
FROM pg_class

-- hier gibts aus schema oid den schema namen.
SELECT *
FROM pg_namespace

-- wir starten mit einem qualifizierten tabellennamen und gehen dann rückwärts. sollte machbar sein.
-- eventuell zuerst alle schemen aus namespace und nspowner = 10 sind keine user tables.

SELECT * 
FROM information_schema.tables 
WHERE table_type = 'BASE TABLE' 
    --AND table_schema = '*' 
ORDER BY table_schema, table_type, table_name