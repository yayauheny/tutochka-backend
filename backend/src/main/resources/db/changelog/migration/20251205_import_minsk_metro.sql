-- liquibase formatted sql

-- changeset yayauheny:import-minsk-metro
-- Импорт данных Минского метро: страна, город, линии и станции

-- Подстраховка: добавляем недостающие геоколонки, если схемы были применены частично
ALTER TABLE cities ADD COLUMN IF NOT EXISTS coordinates GEOMETRY(POINT, 4326);
ALTER TABLE cities ADD COLUMN IF NOT EXISTS city_bounds GEOMETRY(Polygon, 4326);

-- 0. Страна Беларусь
INSERT INTO countries (code, name_ru, name_en)
VALUES ('BY', 'Беларусь', 'Belarus')
ON CONFLICT (code) DO NOTHING;

-- 1. Город Минск (примерные координаты центра)
INSERT INTO cities (country_id, name_ru, name_en, region, coordinates)
SELECT c.id,
       'Минск',
       'Minsk',
       NULL,
       ST_SetSRID(ST_MakePoint(27.5619, 53.9023), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO NOTHING;

-- 2. Линии и станции Минского метро
WITH minsk AS (
    SELECT ci.id AS city_id
    FROM cities ci
    JOIN countries co ON co.id = ci.country_id
    WHERE co.code = 'BY' AND ci.name_ru = 'Минск'
),
lines AS (
    INSERT INTO subway_lines (city_id, name_ru, name_en, hex_color, short_code)
    SELECT city_id, 'Московская линия', 'Moskovskaya line', '#0066CC', '1' FROM minsk
    UNION ALL
    SELECT city_id, 'Автозаводская линия', 'Avtozavodskaya line', '#CC0000', '2' FROM minsk
    UNION ALL
    SELECT city_id, 'Зеленолужская линия', 'Zelenoluzhskaya line', '#00AA00', '3' FROM minsk
    RETURNING id, name_ru
),
l1 AS (SELECT id AS line_id FROM lines WHERE name_ru = 'Московская линия'),
l2 AS (SELECT id AS line_id FROM lines WHERE name_ru = 'Автозаводская линия'),
l3 AS (SELECT id AS line_id FROM lines WHERE name_ru = 'Зеленолужская линия')
INSERT INTO subway_stations (
    subway_line_id,
    name_ru,
    name_en,
    coordinates,
    is_transfer,
    external_ids
)
VALUES
  -- Линия 1: Московская
  ((SELECT line_id FROM l1), 'Малиновка', 'Malinovka',
   ST_SetSRID(ST_MakePoint(27.4747, 53.8497), 4326), false, '{}'::jsonb),
  ((SELECT line_id FROM l1), 'Петровщина', 'Petrovshchina',
   ST_SetSRID(ST_MakePoint(27.4568, 53.8629), 4326), false, '{}'::jsonb),
  ((SELECT line_id FROM l1), 'Михалово', 'Mikhalovo',
   ST_SetSRID(ST_MakePoint(27.4421, 53.8747), 4326), false, '{}'::jsonb),
  ((SELECT line_id FROM l1), 'Грушевка', 'Grushevka',
   ST_SetSRID(ST_MakePoint(27.4319, 53.8861), 4326), false, '{}'::jsonb),
  ((SELECT line_id FROM l1), 'Институт культуры', 'Institut kultury',
   ST_SetSRID(ST_MakePoint(27.4221, 53.8980), 4326), false, '{}'::jsonb),
  ((SELECT line_id FROM l1), 'Площадь Ленина', 'Ploshchad Lenina',
   ST_SetSRID(ST_MakePoint(27.5478, 53.8917), 4326), true, '{}'::jsonb), -- пересадка на Вокзальную
  ((SELECT line_id FROM l1), 'Октябрьская', 'Oktyabrskaya',
   ST_SetSRID(ST_MakePoint(27.5586, 53.8981), 4326), true, '{}'::jsonb), -- пересадка на Купаловскую
  ((SELECT line_id FROM l1), 'Площадь Победы', 'Ploshchad Pobedy',
   ST_SetSRID(ST_MakePoint(27.5720, 53.9081), 4326), false, '{}'::jsonb),
  ((SELECT line_id FROM l1), 'Площадь Якуба Коласа', 'Ploshchad Yakuba Kolasa',
   ST_SetSRID(ST_MakePoint(27.5897, 53.9186), 4326), false, '{}'::jsonb),
  ((SELECT line_id FROM l1), 'Академия наук', 'Akademiya nauk',
   ST_SetSRID(ST_MakePoint(27.6028, 53.9272), 4326), false, '{}'::jsonb),
  ((SELECT line_id FROM l1), 'Парк Челюскинцев', 'Park Chelyuskintsev',
   ST_SetSRID(ST_MakePoint(27.6136, 53.9242), 4326), false, '{}'::jsonb),
  ((SELECT line_id FROM l1), 'Московская', 'Moskovskaya',
   ST_SetSRID(ST_MakePoint(27.6278, 53.9279), 4326), false, '{}'::jsonb),
  ((SELECT line_id FROM l1), 'Восток', 'Vostok',
   ST_SetSRID(ST_MakePoint(27.6408, 53.9319), 4326), false, '{}'::jsonb),
  ((SELECT line_id FROM l1), 'Борисовский тракт', 'Borisovskiy trakt',
   ST_SetSRID(ST_MakePoint(27.6668, 53.9368), 4326), false, '{}'::jsonb),
  ((SELECT line_id FROM l1), 'Уручье', 'Uruchye',
   ST_SetSRID(ST_MakePoint(27.6891, 53.9408), 4326), false, '{}'::jsonb),

  -- Линия 2: Автозаводская
  ((SELECT line_id FROM l2), 'Каменная горка', 'Kamennaya gorka',
   ST_SetSRID(ST_MakePoint(27.4378, 53.9068), 4326), false, '{}'::jsonb),
  ((SELECT line_id FROM l2), 'Кунцевщина', 'Kuntsevshchina',
   ST_SetSRID(ST_MakePoint(27.4128, 53.8961), 4326), false, '{}'::jsonb),
  ((SELECT line_id FROM l2), 'Спортивная', 'Sportivnaya',
   ST_SetSRID(ST_MakePoint(27.3894, 53.8847), 4326), false, '{}'::jsonb),
  ((SELECT line_id FROM l2), 'Пушкинская', 'Pushkinskaya',
   ST_SetSRID(ST_MakePoint(27.3656, 53.8747), 4326), false, '{}'::jsonb),
  ((SELECT line_id FROM l2), 'Молодежная', 'Molodezhnaya',
   ST_SetSRID(ST_MakePoint(27.3431, 53.8639), 4326), false, '{}'::jsonb),
  ((SELECT line_id FROM l2), 'Фрунзенская', 'Frunzenskaya',
   ST_SetSRID(ST_MakePoint(27.3189, 53.8514), 4326), true, '{}'::jsonb), -- пересадка на Юбилейную площадь
  ((SELECT line_id FROM l2), 'Немига', 'Nemiga',
   ST_SetSRID(ST_MakePoint(27.3011, 53.8390), 4326), false, '{}'::jsonb),
  ((SELECT line_id FROM l2), 'Купаловская', 'Kupalovskaya',
   ST_SetSRID(ST_MakePoint(27.3300, 53.8401), 4326), true, '{}'::jsonb), -- пересадка на Октябрьскую
  ((SELECT line_id FROM l2), 'Первомайская', 'Pervomayskaya',
   ST_SetSRID(ST_MakePoint(27.3469, 53.8501), 4326), false, '{}'::jsonb),
  ((SELECT line_id FROM l2), 'Пролетарская', 'Proletarskaya',
   ST_SetSRID(ST_MakePoint(27.3647, 53.8597), 4326), false, '{}'::jsonb),
  ((SELECT line_id FROM l2), 'Тракторный завод', 'Traktornyy zavod',
   ST_SetSRID(ST_MakePoint(27.3822, 53.8697), 4326), false, '{}'::jsonb),
  ((SELECT line_id FROM l2), 'Партизанская', 'Partizanskaya',
   ST_SetSRID(ST_MakePoint(27.4000, 53.8797), 4326), false, '{}'::jsonb),
  ((SELECT line_id FROM l2), 'Автозаводская', 'Avtozavodskaya',
   ST_SetSRID(ST_MakePoint(27.4178, 53.8897), 4326), false, '{}'::jsonb),
  ((SELECT line_id FROM l2), 'Могилевская', 'Mogilevskaya',
   ST_SetSRID(ST_MakePoint(27.4356, 53.8997), 4326), false, '{}'::jsonb),

  -- Линия 3: Зеленолужская
  ((SELECT line_id FROM l3), 'Ковальская слобода', 'Kovalskaya sloboda',
   ST_SetSRID(ST_MakePoint(27.5131, 53.8931), 4326), false, '{}'::jsonb),
  ((SELECT line_id FROM l3), 'Вокзальная', 'Vokzalnaya',
   ST_SetSRID(ST_MakePoint(27.5219, 53.8878), 4326), true, '{}'::jsonb), -- пересадка на Площадь Ленина
  ((SELECT line_id FROM l3), 'Площадь Франтишка Богушевича', 'Ploshchad Frantishka Bogushevicha',
   ST_SetSRID(ST_MakePoint(27.5308, 53.8824), 4326), false, '{}'::jsonb),
  ((SELECT line_id FROM l3), 'Юбилейная площадь', 'Yubileynaya ploshchad',
   ST_SetSRID(ST_MakePoint(27.5397, 53.8769), 4326), true, '{}'::jsonb), -- пересадка на Фрунзенскую
  ((SELECT line_id FROM l3), 'Аэродромная', 'Aerodromnaya',
   ST_SetSRID(ST_MakePoint(27.5494, 53.8669), 4326), false, '{}'::jsonb),
  ((SELECT line_id FROM l3), 'Неморшанский сад', 'Nemorshanskiy sad',
   ST_SetSRID(ST_MakePoint(27.5369, 53.8500), 4326), false, '{}'::jsonb),
  ((SELECT line_id FROM l3), 'Слуцкий гостинец', 'Slutskiy gostinets',
   ST_SetSRID(ST_MakePoint(27.5244, 53.8336), 4326), false, '{}'::jsonb);
-- rollback DELETE FROM subway_stations WHERE subway_line_id IN (SELECT id FROM subway_lines WHERE city_id IN (SELECT id FROM cities WHERE name_ru = 'Минск'));
-- rollback DELETE FROM subway_lines WHERE city_id IN (SELECT id FROM cities WHERE name_ru = 'Минск');
-- rollback DELETE FROM cities WHERE name_ru = 'Минск' AND country_id IN (SELECT id FROM countries WHERE code = 'BY');
-- rollback DELETE FROM countries WHERE code = 'BY';
