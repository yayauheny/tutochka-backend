-- liquibase formatted sql

-- Импорт данных Минского метро: страна, город, линии и станции

-- Подстраховка: добавляем недостающие геоколонки, если схемы были применены частично
ALTER TABLE cities ADD COLUMN IF NOT EXISTS coordinates GEOMETRY(POINT, 4326);
ALTER TABLE cities ADD COLUMN IF NOT EXISTS city_bounds GEOMETRY(Polygon, 4326);

-- Используем фиксированный UUID для страны BY
INSERT INTO countries (id, code, name_ru, name_en)
VALUES ('00000000-0000-0000-0000-0000000000b1'::uuid, 'BY', 'Беларусь', 'Belarus')
ON CONFLICT (code) DO UPDATE
SET name_ru = EXCLUDED.name_ru,
    name_en = EXCLUDED.name_en;

-- Используем фиксированный UUID для города Минск
INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '00000000-0000-0000-0000-0000000000c1'::uuid,
       c.id,
       'Минск',
       'Minsk',
       NULL,
       ST_SetSRID(ST_MakePoint(27.5619, 53.9023), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET name_en = EXCLUDED.name_en;

WITH minsk AS (
    SELECT ci.id AS city_id
    FROM cities ci
    JOIN countries co ON co.id = ci.country_id
    WHERE co.code = 'BY' AND ci.name_ru = 'Минск'
),
inserted_lines AS (
    INSERT INTO subway_lines (id, city_id, name_ru, name_en, hex_color, short_code)
    SELECT '55665eb9-1efa-469f-bd14-058e35c4bf75'::uuid, city_id, 'Московская линия', 'Moskovskaya line', '#0066CC', '1' FROM minsk
    UNION ALL
    SELECT '270518e5-df16-4c42-bf9f-21f2f3a8dcbb'::uuid, city_id, 'Автозаводская линия', 'Avtozavodskaya line', '#CC0000', '2' FROM minsk
    UNION ALL
    SELECT 'b250dde1-d7cf-486c-8f21-1a3246d4e5a0'::uuid, city_id, 'Зеленолужская линия', 'Zelenoluzhskaya line', '#00AA00', '3' FROM minsk
    ON CONFLICT (id) DO NOTHING
),
l1 AS (SELECT '55665eb9-1efa-469f-bd14-058e35c4bf75'::uuid AS line_id),
l2 AS (SELECT '270518e5-df16-4c42-bf9f-21f2f3a8dcbb'::uuid AS line_id),
l3 AS (SELECT 'b250dde1-d7cf-486c-8f21-1a3246d4e5a0'::uuid AS line_id)
INSERT INTO subway_stations (
    id,
    subway_line_id,
    name_ru,
    name_en,
    coordinates,
    is_transfer,
    external_ids
)
VALUES
  -- Линия 1: Московская
  ('ea88119c-73d2-4ac2-8341-774295b3e617'::uuid, (SELECT line_id FROM l1), 'Малиновка', 'Malinovka',
   ST_SetSRID(ST_MakePoint(27.4747, 53.8497), 4326), false, '{}'::jsonb),
  ('c4a8b989-f59a-4319-82e3-23061330dcab'::uuid, (SELECT line_id FROM l1), 'Петровщина', 'Petrovshchina',
   ST_SetSRID(ST_MakePoint(27.4568, 53.8629), 4326), false, '{}'::jsonb),
  ('642381d1-7748-4768-b081-8168e33965c5'::uuid, (SELECT line_id FROM l1), 'Михалово', 'Mikhalovo',
   ST_SetSRID(ST_MakePoint(27.4421, 53.8747), 4326), false, '{}'::jsonb),
  ('1f26f874-0cdd-4a9b-bd24-8d6089860d00'::uuid, (SELECT line_id FROM l1), 'Грушевка', 'Grushevka',
   ST_SetSRID(ST_MakePoint(27.4319, 53.8861), 4326), false, '{}'::jsonb),
  ('232915cf-4280-486c-a0f8-6e3c0459af31'::uuid, (SELECT line_id FROM l1), 'Институт культуры', 'Institut kultury',
   ST_SetSRID(ST_MakePoint(27.4221, 53.8980), 4326), false, '{}'::jsonb),
  ('3b3b81bd-bbb9-4040-ac75-3816e975a576'::uuid, (SELECT line_id FROM l1), 'Площадь Ленина', 'Ploshchad Lenina',
   ST_SetSRID(ST_MakePoint(27.5478, 53.8917), 4326), true, '{}'::jsonb),
  ('8964216a-a08a-4c1c-ac5c-ebbf24466bbe'::uuid, (SELECT line_id FROM l1), 'Октябрьская', 'Oktyabrskaya',
   ST_SetSRID(ST_MakePoint(27.5586, 53.8981), 4326), true, '{}'::jsonb),
  ('5e69997b-d6d6-4fb6-b5d7-4acaf422b910'::uuid, (SELECT line_id FROM l1), 'Площадь Победы', 'Ploshchad Pobedy',
   ST_SetSRID(ST_MakePoint(27.5720, 53.9081), 4326), false, '{}'::jsonb),
  ('eac4ead4-83d1-4cba-8a54-b860f4ed0ff8'::uuid, (SELECT line_id FROM l1), 'Площадь Якуба Коласа', 'Ploshchad Yakuba Kolasa',
   ST_SetSRID(ST_MakePoint(27.5897, 53.9186), 4326), false, '{}'::jsonb),
  ('4b4c046a-c7d2-46ba-8971-9c1491ec4adb'::uuid, (SELECT line_id FROM l1), 'Академия наук', 'Akademiya nauk',
   ST_SetSRID(ST_MakePoint(27.6028, 53.9272), 4326), false, '{}'::jsonb),
  ('f3806018-1e41-470d-97af-4e0ae1145bb1'::uuid, (SELECT line_id FROM l1), 'Парк Челюскинцев', 'Park Chelyuskintsev',
   ST_SetSRID(ST_MakePoint(27.6136, 53.9242), 4326), false, '{}'::jsonb),
  ('00fddd00-11fc-4723-b284-077bc3d339b7'::uuid, (SELECT line_id FROM l1), 'Московская', 'Moskovskaya',
   ST_SetSRID(ST_MakePoint(27.6278, 53.9279), 4326), false, '{}'::jsonb),
  ('8ee907e3-1e87-4390-a365-a67c5b31103c'::uuid, (SELECT line_id FROM l1), 'Восток', 'Vostok',
   ST_SetSRID(ST_MakePoint(27.6408, 53.9319), 4326), false, '{}'::jsonb),
  ('8f8bd6cc-7c29-45da-a171-d83babb03473'::uuid, (SELECT line_id FROM l1), 'Борисовский тракт', 'Borisovskiy trakt',
   ST_SetSRID(ST_MakePoint(27.6668, 53.9368), 4326), false, '{}'::jsonb),
  ('ce27977b-964d-4996-a0e8-298c1776691b'::uuid, (SELECT line_id FROM l1), 'Уручье', 'Uruchye',
   ST_SetSRID(ST_MakePoint(27.6891, 53.9408), 4326), false, '{}'::jsonb),

  -- Линия 2: Автозаводская
  ('05cd10b6-3179-4924-99db-3931f2fb82d4'::uuid, (SELECT line_id FROM l2), 'Каменная горка', 'Kamennaya gorka',
   ST_SetSRID(ST_MakePoint(27.4378, 53.9068), 4326), false, '{}'::jsonb),
  ('f932094b-15ae-419e-9e03-4d3dbaca3ad8'::uuid, (SELECT line_id FROM l2), 'Кунцевщина', 'Kuntsevshchina',
   ST_SetSRID(ST_MakePoint(27.4128, 53.8961), 4326), false, '{}'::jsonb),
  ('ae3cdf73-48ff-4b5c-b5b5-d8706a02b081'::uuid, (SELECT line_id FROM l2), 'Спортивная', 'Sportivnaya',
   ST_SetSRID(ST_MakePoint(27.3894, 53.8847), 4326), false, '{}'::jsonb),
  ('ab4f8dcd-bf45-4e4a-bea2-bc1ec2c17015'::uuid, (SELECT line_id FROM l2), 'Пушкинская', 'Pushkinskaya',
   ST_SetSRID(ST_MakePoint(27.3656, 53.8747), 4326), false, '{}'::jsonb),
  ('c81fcae4-0248-4a9c-897e-470ce2ed163d'::uuid, (SELECT line_id FROM l2), 'Молодежная', 'Molodezhnaya',
   ST_SetSRID(ST_MakePoint(27.3431, 53.8639), 4326), false, '{}'::jsonb),
  ('ec2f32b2-26b9-470d-ba02-d1ca314d9639'::uuid, (SELECT line_id FROM l2), 'Фрунзенская', 'Frunzenskaya',
   ST_SetSRID(ST_MakePoint(27.3189, 53.8514), 4326), true, '{}'::jsonb),
  ('06ea7137-eb28-4060-ad60-8eb03f1d4a0d'::uuid, (SELECT line_id FROM l2), 'Немига', 'Nemiga',
   ST_SetSRID(ST_MakePoint(27.3011, 53.8390), 4326), false, '{}'::jsonb),
  ('5d402db6-a05f-411d-9888-137162dd6847'::uuid, (SELECT line_id FROM l2), 'Купаловская', 'Kupalovskaya',
   ST_SetSRID(ST_MakePoint(27.3300, 53.8401), 4326), true, '{}'::jsonb),
  ('4af6a446-c3d1-4363-b5b3-978312ca50b2'::uuid, (SELECT line_id FROM l2), 'Первомайская', 'Pervomayskaya',
   ST_SetSRID(ST_MakePoint(27.3469, 53.8501), 4326), false, '{}'::jsonb),
  ('6a017c22-7595-4a29-978b-e016e1634cf3'::uuid, (SELECT line_id FROM l2), 'Пролетарская', 'Proletarskaya',
   ST_SetSRID(ST_MakePoint(27.3647, 53.8597), 4326), false, '{}'::jsonb),
  ('c52d621c-77c4-4f23-aa2a-2cf045e39481'::uuid, (SELECT line_id FROM l2), 'Тракторный завод', 'Traktornyy zavod',
   ST_SetSRID(ST_MakePoint(27.3822, 53.8697), 4326), false, '{}'::jsonb),
  ('a69804ad-867f-4783-b753-9e00c96651d4'::uuid, (SELECT line_id FROM l2), 'Партизанская', 'Partizanskaya',
   ST_SetSRID(ST_MakePoint(27.4000, 53.8797), 4326), false, '{}'::jsonb),
  ('1d33b1ce-f25d-4e7b-bad7-0eb146bef1da'::uuid, (SELECT line_id FROM l2), 'Автозаводская', 'Avtozavodskaya',
   ST_SetSRID(ST_MakePoint(27.4178, 53.8897), 4326), false, '{}'::jsonb),
  ('b42aaa6a-cb5d-4542-b3dc-3d5641efa851'::uuid, (SELECT line_id FROM l2), 'Могилевская', 'Mogilevskaya',
   ST_SetSRID(ST_MakePoint(27.4356, 53.8997), 4326), false, '{}'::jsonb),

  -- Линия 3: Зеленолужская
  ('87bb0369-a4b5-4d97-9e54-441952919642'::uuid, (SELECT line_id FROM l3), 'Ковальская слобода', 'Kovalskaya sloboda',
   ST_SetSRID(ST_MakePoint(27.5131, 53.8931), 4326), false, '{}'::jsonb),
  ('f3f404f3-1dfe-451e-8193-727e60685b80'::uuid, (SELECT line_id FROM l3), 'Вокзальная', 'Vokzalnaya',
   ST_SetSRID(ST_MakePoint(27.5219, 53.8878), 4326), true, '{}'::jsonb),
  ('dd997017-c1a1-496e-addd-8c3bfabd86e1'::uuid, (SELECT line_id FROM l3), 'Площадь Франтишка Богушевича', 'Ploshchad Frantishka Bogushevicha',
   ST_SetSRID(ST_MakePoint(27.5308, 53.8824), 4326), false, '{}'::jsonb),
  ('f35b29e1-2303-42cf-a88e-380b33eadfc7'::uuid, (SELECT line_id FROM l3), 'Юбилейная площадь', 'Yubileynaya ploshchad',
   ST_SetSRID(ST_MakePoint(27.5397, 53.8769), 4326), true, '{}'::jsonb),
  ('3da81292-d4c7-4a9d-b28e-d46e2949d065'::uuid, (SELECT line_id FROM l3), 'Аэродромная', 'Aerodromnaya',
   ST_SetSRID(ST_MakePoint(27.5494, 53.8669), 4326), false, '{}'::jsonb),
  ('ef0d2d07-3122-4207-b531-3fab5e7721b9'::uuid, (SELECT line_id FROM l3), 'Неморшанский сад', 'Nemorshanskiy sad',
   ST_SetSRID(ST_MakePoint(27.5369, 53.8500), 4326), false, '{}'::jsonb),
  ('17ae2b9d-0566-441e-8bc2-359bb9795f94'::uuid, (SELECT line_id FROM l3), 'Слуцкий гостинец', 'Slutskiy gostinets',
   ST_SetSRID(ST_MakePoint(27.5244, 53.8336), 4326), false, '{}'::jsonb)
ON CONFLICT (id) DO NOTHING;
-- rollback DELETE FROM subway_stations WHERE subway_line_id IN (SELECT id FROM subway_lines WHERE city_id IN (SELECT id FROM cities WHERE name_ru = 'Минск'));
-- rollback DELETE FROM subway_lines WHERE city_id IN (SELECT id FROM cities WHERE name_ru = 'Минск');
-- rollback DELETE FROM cities WHERE name_ru = 'Минск' AND country_id IN (SELECT id FROM countries WHERE code = 'BY');
-- rollback DELETE FROM countries WHERE code = 'BY';
