INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '00000000-0000-0000-0000-0000000000c1'::uuid,
       c.id,
       'Минск',
       'Minsk',
       NULL,
       ST_SetSRID(ST_MakePoint(27.5667, 53.9), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;
