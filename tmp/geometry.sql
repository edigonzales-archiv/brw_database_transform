
SELECT * 
FROM information_schema.tables 
WHERE table_type = 'BASE TABLE' 
    --AND table_schema = '*' 
ORDER BY table_schema, table_type, table_name



 select column_name, data_type from information_schema.columns
 where table_name = 'bezirksgrenzen_bezirksgrenzabschnitt';


 SELECT attrelid::regclass, attnum, attname
FROM   pg_attribute
WHERE  attrelid = 'av_avdpool_ch.bezirksgrenzen_bezirksgrenzabschnitt'::regclass
AND    attnum > 0
AND    NOT attisdropped
ORDER  BY attnum;


SELECT c.nspname, a.attname as column_name, format_type(a.atttypid, a.atttypmod) AS data_type
FROM pg_attribute a
JOIN pg_class b ON (a.attrelid = b.relfilenode)
JOIN pg_namespace c ON (c.oid = b.relnamespace)
WHERE b.relname = 'bezirksgrenzen_bezirksgrenzabschnitt' and a.attstattarget = -1;

-- SOGIS (postgis 1.5) liefert nur "geometry". Problem? Man könnte erste Geometry lesen. Falls -1 ists wahrscheilnich lv03. ah und noch range tests. weil ja wgs84 wollen wir nicht transformieren.
