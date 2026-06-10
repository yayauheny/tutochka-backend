INSERT INTO countries (id, code, name_ru, name_en)
VALUES ('00000000-0000-0000-0000-0000000000b1'::uuid, 'BY', 'Беларусь', 'Belarus')
ON CONFLICT (code) DO UPDATE
SET id = EXCLUDED.id,
    name_ru = EXCLUDED.name_ru,
    name_en = EXCLUDED.name_en;
