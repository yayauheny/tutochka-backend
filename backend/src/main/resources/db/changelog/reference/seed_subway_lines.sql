INSERT INTO subway_lines (id, city_id, name_ru, name_en, hex_color, short_code)
SELECT '55665eb9-1efa-469f-bd14-058e35c4bf75'::uuid,
       c.id,
       'Московская линия',
       'Moskovskaya line',
       '#0066CC',
       '1'
FROM cities c
WHERE c.id = '00000000-0000-0000-0000-0000000000c1'::uuid
ON CONFLICT (id) DO NOTHING;

INSERT INTO subway_lines (id, city_id, name_ru, name_en, hex_color, short_code)
SELECT '270518e5-df16-4c42-bf9f-21f2f3a8dcbb'::uuid,
       c.id,
       'Автозаводская линия',
       'Avtozavodskaya line',
       '#CC0000',
       '2'
FROM cities c
WHERE c.id = '00000000-0000-0000-0000-0000000000c1'::uuid
ON CONFLICT (id) DO NOTHING;

INSERT INTO subway_lines (id, city_id, name_ru, name_en, hex_color, short_code)
SELECT 'b250dde1-d7cf-486c-8f21-1a3246d4e5a0'::uuid,
       c.id,
       'Зеленолужская линия',
       'Zelenoluzhskaya line',
       '#00AA00',
       '3'
FROM cities c
WHERE c.id = '00000000-0000-0000-0000-0000000000c1'::uuid
ON CONFLICT (id) DO NOTHING;
