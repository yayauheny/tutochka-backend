-- liquibase formatted sql

-- Seed countries and localities observed in the 2GIS/Yandex scrape datasets.

-- The import files use a mix of city and state fields, so this migration keeps only

-- confirmed locality names and ignores street/region fragments.

INSERT INTO countries (id, code, name_ru, name_en)
VALUES ('00000000-0000-0000-0000-0000000000a1'::uuid, 'AB', 'Абхазия', 'Abkhazia')
ON CONFLICT (code) DO UPDATE
SET id = EXCLUDED.id,
    name_ru = EXCLUDED.name_ru,
    name_en = EXCLUDED.name_en;

INSERT INTO countries (id, code, name_ru, name_en)
VALUES ('00000000-0000-0000-0000-0000000000a2'::uuid, 'AM', 'Армения', 'Armenia')
ON CONFLICT (code) DO UPDATE
SET id = EXCLUDED.id,
    name_ru = EXCLUDED.name_ru,
    name_en = EXCLUDED.name_en;

INSERT INTO countries (id, code, name_ru, name_en)
VALUES ('00000000-0000-0000-0000-0000000000b1'::uuid, 'BY', 'Беларусь', 'Belarus')
ON CONFLICT (code) DO UPDATE
SET id = EXCLUDED.id,
    name_ru = EXCLUDED.name_ru,
    name_en = EXCLUDED.name_en;

INSERT INTO countries (id, code, name_ru, name_en)
VALUES ('00000000-0000-0000-0000-0000000000c1'::uuid, 'GE', 'Грузия', 'Georgia')
ON CONFLICT (code) DO UPDATE
SET id = EXCLUDED.id,
    name_ru = EXCLUDED.name_ru,
    name_en = EXCLUDED.name_en;

INSERT INTO countries (id, code, name_ru, name_en)
VALUES ('00000000-0000-0000-0000-0000000000d1'::uuid, 'KZ', 'Казахстан', 'Kazakhstan')
ON CONFLICT (code) DO UPDATE
SET id = EXCLUDED.id,
    name_ru = EXCLUDED.name_ru,
    name_en = EXCLUDED.name_en;

INSERT INTO countries (id, code, name_ru, name_en)
VALUES ('00000000-0000-0000-0000-0000000000e1'::uuid, 'PL', 'Польша', 'Poland')
ON CONFLICT (code) DO UPDATE
SET id = EXCLUDED.id,
    name_ru = EXCLUDED.name_ru,
    name_en = EXCLUDED.name_en;

INSERT INTO countries (id, code, name_ru, name_en)
VALUES ('00000000-0000-0000-0000-0000000000f1'::uuid, 'RS', 'Сербия', 'Serbia')
ON CONFLICT (code) DO UPDATE
SET id = EXCLUDED.id,
    name_ru = EXCLUDED.name_ru,
    name_en = EXCLUDED.name_en;

INSERT INTO countries (id, code, name_ru, name_en)
VALUES ('00000000-0000-0000-0000-0000000001a1'::uuid, 'RU', 'Россия', 'Russia')
ON CONFLICT (code) DO UPDATE
SET id = EXCLUDED.id,
    name_ru = EXCLUDED.name_ru,
    name_en = EXCLUDED.name_en;

INSERT INTO countries (id, code, name_ru, name_en)
VALUES ('00000000-0000-0000-0000-0000000001a2'::uuid, 'TR', 'Турция', 'Turkey')
ON CONFLICT (code) DO UPDATE
SET id = EXCLUDED.id,
    name_ru = EXCLUDED.name_ru,
    name_en = EXCLUDED.name_en;

INSERT INTO countries (id, code, name_ru, name_en)
VALUES ('00000000-0000-0000-0000-0000000001a3'::uuid, 'UA', 'Украина', 'Ukraine')
ON CONFLICT (code) DO UPDATE
SET id = EXCLUDED.id,
    name_ru = EXCLUDED.name_ru,
    name_en = EXCLUDED.name_en;

INSERT INTO countries (id, code, name_ru, name_en)
VALUES ('00000000-0000-0000-0000-0000000001a4'::uuid, 'UZ', 'Узбекистан', 'Uzbekistan')
ON CONFLICT (code) DO UPDATE
SET id = EXCLUDED.id,
    name_ru = EXCLUDED.name_ru,
    name_en = EXCLUDED.name_en;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '1b13d972-bdee-560d-8e43-807dc49a7872'::uuid, c.id, 'Гагра', 'Gagra', NULL, ST_SetSRID(ST_MakePoint(40.262264, 43.283252), 4326)
FROM countries c
WHERE c.code = 'AB'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'b826f888-8cf6-5fc8-a4c0-d3c377a2bb05'::uuid, c.id, 'Новый Афон', 'Novyy Afon', NULL, ST_SetSRID(ST_MakePoint(40.810546, 43.09017), 4326)
FROM countries c
WHERE c.code = 'AB'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '76c82fd1-1a77-5d41-8ae4-73425bfd845e'::uuid, c.id, 'Пицунда', 'Pitsunda', NULL, ST_SetSRID(ST_MakePoint(40.349641, 43.156427), 4326)
FROM countries c
WHERE c.code = 'AB'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'e9987d51-5dc4-5093-85fc-05ada241bdd5'::uuid, c.id, 'Ереван', 'Yerevan', NULL, ST_SetSRID(ST_MakePoint(44.479468, 40.192603), 4326)
FROM countries c
WHERE c.code = 'AM'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '00000000-0000-0000-0000-0000000000c1'::uuid, c.id, 'Минск', 'Minsk', NULL, ST_SetSRID(ST_MakePoint(27.5667, 53.9), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '4a1b2e8f-3fd3-5ee7-9bc5-f403d63db63e'::uuid, c.id, 'Гомель', 'Homyel’', NULL, ST_SetSRID(ST_MakePoint(30.9842, 52.4453), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'cc1ba551-ddd8-5b6b-aa4e-c1e6fe60b1e5'::uuid, c.id, 'Витебск', 'Vitsyebsk', NULL, ST_SetSRID(ST_MakePoint(30.2056, 55.1917), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '668d983e-56d0-565f-ade4-0bd0a8101e6b'::uuid, c.id, 'Гродно', 'Hrodna', NULL, ST_SetSRID(ST_MakePoint(23.8333, 53.6667), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '4e34a02c-7d49-5b7f-b960-85b3f12a7ae9'::uuid, c.id, 'Могилёв', 'Mahilyow', NULL, ST_SetSRID(ST_MakePoint(30.35, 53.9167), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '87776e95-0102-5fe2-97fc-9e9ec637a502'::uuid, c.id, 'Брест', 'Brest', NULL, ST_SetSRID(ST_MakePoint(23.6569, 52.1347), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'f99f8d34-2365-5180-994c-377bbf67becb'::uuid, c.id, 'Бобруйск', 'Babruysk', NULL, ST_SetSRID(ST_MakePoint(29.2333, 53.15), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '1416541b-0820-5c04-b183-1334455e6b76'::uuid, c.id, 'Барановичи', 'Baranavichy', NULL, ST_SetSRID(ST_MakePoint(26.0167, 53.1333), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '4eb6e4ab-e48c-5863-a588-8033ab48d7a2'::uuid, c.id, 'Город Борисов', 'Horad Barysaw', NULL, ST_SetSRID(ST_MakePoint(28.505, 54.2279), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '7681f521-e6fd-5545-bf3d-d746f3feef44'::uuid, c.id, 'Пинск', 'Pinsk', NULL, ST_SetSRID(ST_MakePoint(26.1031, 52.1153), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'b2a628c8-7bbd-5f5b-abbc-6c0bc6026fa8'::uuid, c.id, 'Мозырь', 'Mazyr', NULL, ST_SetSRID(ST_MakePoint(29.25, 52.05), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '3862d793-6c2b-5715-a611-637527d98c07'::uuid, c.id, 'Лида', 'Lida', NULL, ST_SetSRID(ST_MakePoint(25.2958, 53.8956), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '54e1b6cc-6e90-555f-abc9-b0cdb14bd64e'::uuid, c.id, 'Орша', 'Orsha', NULL, ST_SetSRID(ST_MakePoint(30.4258, 54.5092), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'e6d9e616-be74-5c70-9466-cc9acf3647c5'::uuid, c.id, 'Солигорск', 'Salihorsk', NULL, ST_SetSRID(ST_MakePoint(27.5333, 52.8), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'b2bf913c-dd57-51c1-a196-5ec5174896d4'::uuid, c.id, 'Новополоцк', 'Navapolatsk', NULL, ST_SetSRID(ST_MakePoint(28.65, 55.5333), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '48bc1e56-d262-5d49-ae71-02f308a62b83'::uuid, c.id, 'Молодечно', 'Maladzyechna', NULL, ST_SetSRID(ST_MakePoint(26.8572, 54.3208), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'ff681f13-12f1-55d6-9e50-74e4c838a557'::uuid, c.id, 'Полоцк', 'Polatsk', NULL, ST_SetSRID(ST_MakePoint(28.8, 55.4833), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'f4da9198-c64f-5edc-a2a4-110ffa6b2408'::uuid, c.id, 'Жлобин', 'Zhlobin', NULL, ST_SetSRID(ST_MakePoint(30.0333, 52.9), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '46f5deef-f4e4-5467-b519-7ceca2b78476'::uuid, c.id, 'Речица', 'Rechytsa', NULL, ST_SetSRID(ST_MakePoint(30.3947, 52.3639), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'd71d6548-4f26-5bf4-b184-42e48c10a2d3'::uuid, c.id, 'Город Жодино', 'Horad Zhodzina', NULL, ST_SetSRID(ST_MakePoint(28.35, 54.1), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '110807ab-24ce-5433-b56b-73ed9d1f36f4'::uuid, c.id, 'Светлогорск', 'Svyetlahorsk', NULL, ST_SetSRID(ST_MakePoint(29.7333, 52.6333), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '95cba281-11ad-56c2-9c25-d6428a91a424'::uuid, c.id, 'Слуцк', 'Slutsk', NULL, ST_SetSRID(ST_MakePoint(27.5667, 53.0333), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'ab4d1847-4be1-5c9f-a6fb-6c3ec69577a0'::uuid, c.id, 'Кобрин', 'Kobryn', NULL, ST_SetSRID(ST_MakePoint(24.3667, 52.2167), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '3f87c061-4093-54b3-b0a9-d164b76a4cee'::uuid, c.id, 'Слоним', 'Slonim', NULL, ST_SetSRID(ST_MakePoint(25.3167, 53.0833), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '72651ea9-4334-538b-b1a9-168a1459009c'::uuid, c.id, 'Волковыск', 'Vawkavysk', NULL, ST_SetSRID(ST_MakePoint(24.4667, 53.1667), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '67f6af1e-a62b-5a9f-8674-a56e2033df36'::uuid, c.id, 'Калинковичи', 'Kalinkavichy', NULL, ST_SetSRID(ST_MakePoint(29.3333, 52.125), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'a5246107-5385-5fc5-83e4-90b197e6f1e0'::uuid, c.id, 'Сморгонь', 'Smarhon', NULL, ST_SetSRID(ST_MakePoint(26.4, 54.4836), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '3d9dfe8d-5d48-5cdf-86c2-e7ab72ca28ea'::uuid, c.id, 'Рогачёв', 'Rahachow', NULL, ST_SetSRID(ST_MakePoint(30.05, 53.1), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'd56e1a9d-12e2-5940-927c-363b5baf1c14'::uuid, c.id, 'Дзержинск', 'Dzyarzhynsk', NULL, ST_SetSRID(ST_MakePoint(27.1333, 53.6833), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'a3606a31-5d68-5b77-add1-35dc648e386a'::uuid, c.id, 'Осиповичи', 'Asipovichy', NULL, ST_SetSRID(ST_MakePoint(28.4756, 53.2933), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '2eb3aff6-4dd1-556c-b9d6-114c99835900'::uuid, c.id, 'Горки', 'Horki', NULL, ST_SetSRID(ST_MakePoint(30.9833, 54.2667), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '1c43120e-c8c6-5ef9-97e4-9fed15238e32'::uuid, c.id, 'Берёза', 'Byaroza', NULL, ST_SetSRID(ST_MakePoint(24.9667, 52.55), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '6991effe-b22b-512b-9bbc-46f7644702b3'::uuid, c.id, 'Новогрудок', 'Navahrudak', NULL, ST_SetSRID(ST_MakePoint(25.8167, 53.5833), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '049998fa-1a77-5693-9d65-a6f2f9043ebe'::uuid, c.id, 'Вилейка', 'Vilyeyka', NULL, ST_SetSRID(ST_MakePoint(26.926, 54.498), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '6ed1bb23-601b-5c55-af3d-8f6dfe088c10'::uuid, c.id, 'Лунинец', 'Luninyets', NULL, ST_SetSRID(ST_MakePoint(26.8, 52.25), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'be155bdb-6ed8-58ec-bc52-dd9cc8875116'::uuid, c.id, 'Кричев', 'Krychaw', NULL, ST_SetSRID(ST_MakePoint(31.7139, 53.7194), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'ae0dbfbd-72b6-5d4a-b784-02c008f20346'::uuid, c.id, 'Ивацевичи', 'Ivatsevichy', NULL, ST_SetSRID(ST_MakePoint(25.3333, 52.7167), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'f46b72d5-1c3f-51aa-8ecb-6281c09f7b4a'::uuid, c.id, 'Город Смолевичи', 'Horad Smalyavichy', NULL, ST_SetSRID(ST_MakePoint(28.0667, 54.1), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '5c7549ec-f20d-5fb5-b698-d5e82fb8a014'::uuid, c.id, 'Марьина Горка', 'Mar’’ina Horka', NULL, ST_SetSRID(ST_MakePoint(28.1522, 53.5072), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '6dd1005e-0140-5e35-afe8-148c9e016c72'::uuid, c.id, 'Пружаны', 'Pruzhany', NULL, ST_SetSRID(ST_MakePoint(24.4644, 52.5567), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '35f1ac3e-dbec-5a00-8869-232353a536da'::uuid, c.id, 'Поставы', 'Pastavy', NULL, ST_SetSRID(ST_MakePoint(26.8333, 55.1167), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '1661f215-b20c-5094-afc4-92fd5776fc52'::uuid, c.id, 'Добруш', 'Dobrush', NULL, ST_SetSRID(ST_MakePoint(31.3167, 52.4167), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'a4b005d6-0eb9-59d0-bc23-f1fafda8d2a9'::uuid, c.id, 'Фаниполь', 'Fanipal’', NULL, ST_SetSRID(ST_MakePoint(27.3333, 53.75), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'ec766d5a-29c7-5a0a-b225-3d24021191da'::uuid, c.id, 'Глубокое', 'Hlybokaye', NULL, ST_SetSRID(ST_MakePoint(27.6833, 55.1333), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'aaedd088-dcf1-5656-8e50-1c42c21d483b'::uuid, c.id, 'Столбцы', 'Stowbtsy', NULL, ST_SetSRID(ST_MakePoint(26.7333, 53.4833), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '12c869d4-b290-5d79-a08e-70b2a79b7a91'::uuid, c.id, 'Заславль', 'Zaslawye', NULL, ST_SetSRID(ST_MakePoint(27.2847, 54.0083), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '76abb5a9-f187-51e5-84f3-6aef772f1312'::uuid, c.id, 'Лепель', 'Lyepyel', NULL, ST_SetSRID(ST_MakePoint(28.6944, 54.875), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'cc40a55c-c06c-5e47-bbe9-517454d7da3c'::uuid, c.id, 'Ошмяны', 'Ashmyany', NULL, ST_SetSRID(ST_MakePoint(25.9375, 54.425), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '618f923c-e5f6-5a70-9ac7-fdef2bb61680'::uuid, c.id, 'Быхов', 'Bykhaw', NULL, ST_SetSRID(ST_MakePoint(30.25, 53.5167), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'c4d0f4cd-8bac-5e4f-9da1-4d43b8589f23'::uuid, c.id, 'Иваново', 'Ivanava', NULL, ST_SetSRID(ST_MakePoint(25.55, 52.1333), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '7d36c07a-6fb8-5c0d-8a79-b8e13e363c9f'::uuid, c.id, 'Житковичи', 'Zhytkavichy', NULL, ST_SetSRID(ST_MakePoint(27.8667, 52.2333), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '72adb732-888c-58a4-a624-471664cf95c2'::uuid, c.id, 'Несвиж', 'Nyasvizh', NULL, ST_SetSRID(ST_MakePoint(26.6667, 53.2167), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '5e9cd170-9d12-5f0b-9805-10f357eae3dd'::uuid, c.id, 'Щучин', 'Shchuchyn', NULL, ST_SetSRID(ST_MakePoint(24.7333, 53.6167), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'bd85e9b8-df10-5d87-aa61-d5fecef473c8'::uuid, c.id, 'Логойск', 'Lahoysk', NULL, ST_SetSRID(ST_MakePoint(27.85, 54.2), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '7cbfd928-ae42-5e92-a966-02b1468187ee'::uuid, c.id, 'Климовичи', 'Klimavichy', NULL, ST_SetSRID(ST_MakePoint(31.95, 53.6167), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '41b3fa35-48a7-50e3-9c53-33f5935930b9'::uuid, c.id, 'Костюковичи', 'Kastsyukovichy', NULL, ST_SetSRID(ST_MakePoint(32.05, 53.3333), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '7c51251f-2993-5c03-922b-45eb3a233bcb'::uuid, c.id, 'Шклов', 'Shklow', NULL, ST_SetSRID(ST_MakePoint(30.2864, 54.2236), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '9a9df6a5-04c8-5b13-b4f1-88a00ec71c4c'::uuid, c.id, 'Дрогичин', 'Drahichyn', NULL, ST_SetSRID(ST_MakePoint(25.15, 52.1833), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '0f1f2b0d-a561-579d-b228-8b8e10fd13ea'::uuid, c.id, 'Островец', 'Astravyets', NULL, ST_SetSRID(ST_MakePoint(25.9553, 54.6136), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '1b1d8cd1-e530-5442-afcc-f0bbe00acbdb'::uuid, c.id, 'Мосты', 'Masty', NULL, ST_SetSRID(ST_MakePoint(24.55, 53.417), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'a7c7936b-0348-5585-a0bc-146ae1797307'::uuid, c.id, 'Жабинка', 'Zhabinka', NULL, ST_SetSRID(ST_MakePoint(24.0233, 52.2006), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '96154d14-d109-546d-ba94-9a72e2de140b'::uuid, c.id, 'Столин', 'Stolin', NULL, ST_SetSRID(ST_MakePoint(26.85, 51.8833), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'a0e49755-12b1-5c27-a632-de6b1c090c32'::uuid, c.id, 'Ганцевичи', 'Hantsavichy', NULL, ST_SetSRID(ST_MakePoint(26.4333, 52.75), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '18658358-21c4-5e16-97b2-69f464da360f'::uuid, c.id, 'Хойники', 'Khoyniki', NULL, ST_SetSRID(ST_MakePoint(29.9644, 51.8892), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '5dd38174-f24f-5697-b5f1-00d80e1771f5'::uuid, c.id, 'Малорита', 'Malaryta', NULL, ST_SetSRID(ST_MakePoint(24.0833, 51.7833), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'a5eef218-06b0-5660-8976-a044ad8e5df6'::uuid, c.id, 'Микашевичи', 'Mikashevichy', NULL, ST_SetSRID(ST_MakePoint(27.4736, 52.2203), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'dd79c8e2-1c79-5939-ad97-6149802e1c65'::uuid, c.id, 'Лельчицы', 'Lyelchytsy', NULL, ST_SetSRID(ST_MakePoint(28.3214, 51.7894), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '5d611c0e-888b-5f93-9a32-7f6c1b086ab9'::uuid, c.id, 'Новолукомль', 'Novalukoml’', NULL, ST_SetSRID(ST_MakePoint(29.15, 54.6569), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '572c5be0-37da-533e-aa21-17b27cf64946'::uuid, c.id, 'Городок', 'Haradok', NULL, ST_SetSRID(ST_MakePoint(30, 55.4667), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '53cdd465-d6a0-54a2-bdae-9abb98824a33'::uuid, c.id, 'Березино', 'Byerazino', NULL, ST_SetSRID(ST_MakePoint(28.9833, 53.8333), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'e8e10a14-c8cf-52a7-b97c-0c8002a6f194'::uuid, c.id, 'Любань', 'Lyuban', NULL, ST_SetSRID(ST_MakePoint(28.0525, 52.7819), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '4b6da8f7-54cf-5c63-b42d-08e33700320a'::uuid, c.id, 'Клецк', 'Klyetsk', NULL, ST_SetSRID(ST_MakePoint(26.6372, 53.0636), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'b1c97e92-7d7e-5641-b06e-8768343033b9'::uuid, c.id, 'Белоозёрск', 'Byelaazyorsk', NULL, ST_SetSRID(ST_MakePoint(25.1667, 52.45), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'ec7a24cc-72f6-5ea3-ada6-1e1de0e3a670'::uuid, c.id, 'Старые Дороги', 'Staryya Darohi', NULL, ST_SetSRID(ST_MakePoint(28.265, 53.0394), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '9b39cfe5-68ac-53b0-95dd-ce1b2d6439de'::uuid, c.id, 'Узда', 'Uzda', NULL, ST_SetSRID(ST_MakePoint(27.2244, 53.4661), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'bfab1b70-a6e3-5645-ad32-b6fe68293cd2'::uuid, c.id, 'Ляховичи', 'Lyakhavichy', NULL, ST_SetSRID(ST_MakePoint(26.2667, 53.0333), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '0f080dba-8aad-59e8-92f8-0b9b89a93a1c'::uuid, c.id, 'Червень', 'Chervyen', NULL, ST_SetSRID(ST_MakePoint(28.4322, 53.7078), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '660237b4-52a6-5388-9cd1-d9a7cdb5b97c'::uuid, c.id, 'Мачулищи', 'Machulishchy', NULL, ST_SetSRID(ST_MakePoint(27.5958, 53.7814), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '248414af-a69a-5cd2-9b1a-77408782f6ac'::uuid, c.id, 'Петриков', 'Pyetrykaw', NULL, ST_SetSRID(ST_MakePoint(28.5, 52.1333), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '8d25a201-9fa0-550c-964b-6fc33091084f'::uuid, c.id, 'Барань', 'Baran', NULL, ST_SetSRID(ST_MakePoint(30.3333, 54.4833), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '35454700-a632-5df8-b8b1-7d835738f847'::uuid, c.id, 'Копыль', 'Kapyl', NULL, ST_SetSRID(ST_MakePoint(27.0917, 53.15), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'd23df7ae-9f0d-52a8-9027-1617a9da03db'::uuid, c.id, 'Воложин', 'Valozhyn', NULL, ST_SetSRID(ST_MakePoint(26.5167, 54.0833), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'cc570753-9371-5037-8008-d1082a093f70'::uuid, c.id, 'Мстиславль', 'Mstsislaw', NULL, ST_SetSRID(ST_MakePoint(31.7167, 54.0167), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '5767b688-2968-5423-9e22-900c6fcc5345'::uuid, c.id, 'Чаусы', 'Chavusy', NULL, ST_SetSRID(ST_MakePoint(30.9714, 53.8075), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '06be63e2-36c0-50bb-a9ce-dfbbe6352fee'::uuid, c.id, 'Белыничи', 'Byalynichy', NULL, ST_SetSRID(ST_MakePoint(29.7094, 53.9956), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '9c850be9-d1e1-56e4-83e5-06f36344eab4'::uuid, c.id, 'Скидель', 'Skidal’', NULL, ST_SetSRID(ST_MakePoint(24.2519, 53.5861), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '6055d1b0-75e3-5bb7-8687-8ab0e97ae3a1'::uuid, c.id, 'Толочин', 'Talachyn', NULL, ST_SetSRID(ST_MakePoint(29.7, 54.4167), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '58763148-eeb0-5e1d-9fa5-81f93f59240c'::uuid, c.id, 'Берёзовка', 'Byarozawka', NULL, ST_SetSRID(ST_MakePoint(25.5, 53.7167), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '34386dcf-a98a-5223-85a8-8500b29291a3'::uuid, c.id, 'Браслав', 'Braslaw', NULL, ST_SetSRID(ST_MakePoint(27.0318, 55.6391), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'd57a0aad-7b22-58e2-948c-fac3ee5e2839'::uuid, c.id, 'Чечерск', 'Chachersk', NULL, ST_SetSRID(ST_MakePoint(30.9161, 52.9161), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '0de6570d-3d8f-5d8a-b8f1-5d502c593263'::uuid, c.id, 'Ельск', 'Yelsk', NULL, ST_SetSRID(ST_MakePoint(29.15, 51.8167), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '007afe04-a692-53e9-a931-cc7a85deea25'::uuid, c.id, 'Ветка', 'Vyetka', NULL, ST_SetSRID(ST_MakePoint(31.1833, 52.5667), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '27544880-d3e2-5d72-89a3-38e931bafc09'::uuid, c.id, 'Буда-Кошелёво', 'Buda-Kashalyova', NULL, ST_SetSRID(ST_MakePoint(30.5667, 52.7167), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '5ff9e0be-f4ed-5c3f-8b77-3f06576611b6'::uuid, c.id, 'Крупки', 'Krupki', NULL, ST_SetSRID(ST_MakePoint(29.1333, 54.3167), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '2ddae909-f404-5aab-b211-e95da9d27f63'::uuid, c.id, 'Наровля', 'Narowlya', NULL, ST_SetSRID(ST_MakePoint(29.9644, 51.8892), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'cc563609-eb19-5ab7-bae0-fec232a89258'::uuid, c.id, 'Каменец', 'Kamyanyets', NULL, ST_SetSRID(ST_MakePoint(23.8167, 52.4), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'a8c66393-e9d7-52ec-8355-6909c6bbfcfd'::uuid, c.id, 'Кировск', 'Kirawsk', NULL, ST_SetSRID(ST_MakePoint(29.473, 53.2692), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '0dc99339-1747-505e-a70f-c94378fa8008'::uuid, c.id, 'Корма', 'Karma', NULL, ST_SetSRID(ST_MakePoint(30.8106, 53.1292), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '79690b12-ec44-5101-83de-972b6860e7d3'::uuid, c.id, 'Дятлово', 'Dzyatlava', NULL, ST_SetSRID(ST_MakePoint(25.4056, 53.4653), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'dd6e9aee-2dab-53c6-80c2-0a221fb03fe9'::uuid, c.id, 'Чашники', 'Chashniki', NULL, ST_SetSRID(ST_MakePoint(29.1647, 54.8533), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'f4ec49f0-69de-5005-a7d9-cf8b0bf35d4d'::uuid, c.id, 'Славгород', 'Slawharad', NULL, ST_SetSRID(ST_MakePoint(31, 53.4333), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '04ccc46b-64a6-5c68-918d-10791c8b491f'::uuid, c.id, 'Миоры', 'Myory', NULL, ST_SetSRID(ST_MakePoint(27.6167, 55.6167), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '07ca654b-63eb-5576-8f37-19130f0f8eaa'::uuid, c.id, 'Чериков', 'Cherykaw', NULL, ST_SetSRID(ST_MakePoint(31.3667, 53.5667), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '54746c7e-88a1-54e8-a9f1-1a8e24f60e82'::uuid, c.id, 'Кличев', 'Klichaw', NULL, ST_SetSRID(ST_MakePoint(29.3333, 53.4833), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '1bafd13a-5eba-56cc-abe1-98b9527a2547'::uuid, c.id, 'Круглое', 'Kruhlaye', NULL, ST_SetSRID(ST_MakePoint(29.7964, 54.2478), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'e734cc02-6c57-5cfa-824e-9459ac0c5d6e'::uuid, c.id, 'Октябрьский', 'Aktsyabrski', NULL, ST_SetSRID(ST_MakePoint(28.8833, 52.6472), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '81eee604-f2fe-5a10-b4cc-4351a6d509f9'::uuid, c.id, 'Ивье', 'Iwye', NULL, ST_SetSRID(ST_MakePoint(25.7667, 53.9167), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '0139ad8e-4103-51ab-bfe5-2cdc0520f581'::uuid, c.id, 'Сенно', 'Syanno', NULL, ST_SetSRID(ST_MakePoint(29.7, 54.8), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '07aa3e1f-8cfe-5b49-98df-ed6f597b84fa'::uuid, c.id, 'Глусск', 'Hlusk', NULL, ST_SetSRID(ST_MakePoint(28.6922, 52.8895), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '807ba1b5-518e-51d1-810e-159a17d7b4f2'::uuid, c.id, 'Мядель', 'Myadzyel', NULL, ST_SetSRID(ST_MakePoint(26.9333, 54.8667), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'aaf4bba5-86f7-5ce0-8b2c-8cba0cf07f59'::uuid, c.id, 'Дубровно', 'Dubrowna', NULL, ST_SetSRID(ST_MakePoint(30.6833, 54.5667), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '291b7f6b-c1fc-583c-8a0b-4262b4c5f06e'::uuid, c.id, 'Верхнедвинск', 'Vyerkhnyadzvinsk', NULL, ST_SetSRID(ST_MakePoint(27.95, 55.7833), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'c2c38e2c-1814-5153-a99f-7133c92a3905'::uuid, c.id, 'Докшицы', 'Dokshytsy', NULL, ST_SetSRID(ST_MakePoint(27.7667, 54.9), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '3ae9fd46-15a8-515d-8f1c-d54bfc663b15'::uuid, c.id, 'Лиозно', 'Lyozna', NULL, ST_SetSRID(ST_MakePoint(30.8, 55.0167), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '3d279169-65cc-55ca-bbfb-9be8652b0860'::uuid, c.id, 'Радошковичи', 'Radashkovichy', NULL, ST_SetSRID(ST_MakePoint(27.2333, 54.15), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'e7ac3689-94d4-5202-97e9-2494617d85ae'::uuid, c.id, 'Хотимск', 'Khotsimsk', NULL, ST_SetSRID(ST_MakePoint(32.5722, 53.4083), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '4441aa5b-ce33-5db2-8604-e988f39f764e'::uuid, c.id, 'Шарковщина', 'Sharkawshchyna', NULL, ST_SetSRID(ST_MakePoint(27.4667, 55.3667), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '6f48bd6c-4923-58bc-ba8b-ed0e615864a7'::uuid, c.id, 'Лоев', 'Loyew', NULL, ST_SetSRID(ST_MakePoint(30.8, 51.9333), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'bbbb04a0-bdf2-5211-adc7-f99da63dc5c5'::uuid, c.id, 'Свислочь', 'Svislach', NULL, ST_SetSRID(ST_MakePoint(24.1, 53.0333), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '3f27c538-f08f-50ea-9fd1-d377313774da'::uuid, c.id, 'Давид-Городок', 'Davyd-Haradok', NULL, ST_SetSRID(ST_MakePoint(27.2139, 52.0556), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '32b01b48-dce4-5f07-82b8-11bebdaa1eb0'::uuid, c.id, 'Краснополье', 'Krasnapollye', NULL, ST_SetSRID(ST_MakePoint(31.4022, 53.3333), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '123241ee-cddc-5cf0-9c7e-e6f5ba97e5d6'::uuid, c.id, 'Вороново', 'Voranava', NULL, ST_SetSRID(ST_MakePoint(25.3167, 54.15), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '1005c65a-ba32-5cc1-8c9e-10195f25b874'::uuid, c.id, 'Великая Берестовица', 'Vyalikaya Byerastavitsa', NULL, ST_SetSRID(ST_MakePoint(24.0208, 53.1956), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'd236ef10-e728-52d6-93ad-5a85c4795f18'::uuid, c.id, 'Чисть', 'Chysts', NULL, ST_SetSRID(ST_MakePoint(27.1089, 54.2683), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'a33c6e96-24ff-5972-80ce-ea7f1c5ef06a'::uuid, c.id, 'Высокое', 'Vysokaye', NULL, ST_SetSRID(ST_MakePoint(23.3806, 52.3686), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'e1434982-edb7-52e8-bd19-a719b0202666'::uuid, c.id, 'Россоны', 'Rasony', NULL, ST_SetSRID(ST_MakePoint(28.8092, 55.9042), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '3acb0185-be2a-5754-9923-1c24531dcd75'::uuid, c.id, 'Брагин', 'Brahin', NULL, ST_SetSRID(ST_MakePoint(30.2667, 51.7833), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '0d4b4eec-8b0c-53d1-8e6f-24862a585bfe'::uuid, c.id, 'Василевичи', 'Vasilyevichy', NULL, ST_SetSRID(ST_MakePoint(29.8, 52.2667), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '3b6cf13c-e7b4-5f4c-9eb2-825d269548ab'::uuid, c.id, 'Дрибин', 'Drybin', NULL, ST_SetSRID(ST_MakePoint(31.0931, 54.1194), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '4a106b37-f70a-5e59-b8e8-31e1d72ec1d8'::uuid, c.id, 'Туров', 'Turaw', NULL, ST_SetSRID(ST_MakePoint(27.74, 52.07), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '1353f634-49f9-5281-ab49-bed70b3cf911'::uuid, c.id, 'Коссово', 'Kosava', NULL, ST_SetSRID(ST_MakePoint(25.15, 52.75), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '16ef8a75-bef7-5036-b8c7-b2950b32cfc5'::uuid, c.id, 'Дисна', 'Dzisna', NULL, ST_SetSRID(ST_MakePoint(28.2167, 55.5667), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '9ca2c6c7-79d2-5f0f-b35b-0d553616c2ea'::uuid, c.id, 'Носилово', 'Nasilava', NULL, ST_SetSRID(ST_MakePoint(26.7789, 54.3094), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'b3068a18-27e1-5e97-881f-5e625ffd794d'::uuid, c.id, 'Андреевщина', 'Andreyewshchyna', NULL, ST_SetSRID(ST_MakePoint(30.4521, 54.5689), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '0e4c40f1-dd41-5dc4-8a96-586e3295c5b2'::uuid, c.id, 'Город Орша', 'Horad Orsha', NULL, ST_SetSRID(ST_MakePoint(30.4053, 54.5153), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'ff05c9fb-f4ae-57f6-8da3-7a4ebe89990f'::uuid, c.id, 'Город Речица', 'Horad Rechytsa', NULL, ST_SetSRID(ST_MakePoint(30.3947, 52.3639), 4326)
FROM countries c
WHERE c.code = 'BY'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '5cb1fe26-9025-504b-8eb7-aab9604c9d8e'::uuid, c.id, 'Сухум', 'Sukhumi', NULL, ST_SetSRID(ST_MakePoint(41.02661, 43.000258), 4326)
FROM countries c
WHERE c.code = 'GE'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'ee11cc00-fc56-51d9-b33c-f80875093758'::uuid, c.id, 'Астана', 'Astana', NULL, ST_SetSRID(ST_MakePoint(71.424027, 51.161404), 4326)
FROM countries c
WHERE c.code = 'KZ'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'bead8bc6-d37c-5bab-b27c-97b914d9c659'::uuid, c.id, 'Костанай', 'Kostanay', NULL, ST_SetSRID(ST_MakePoint(63.63248, 53.212318), 4326)
FROM countries c
WHERE c.code = 'KZ'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'd452efe8-f812-5f19-9468-e617cd637ce0'::uuid, c.id, 'Шымкент', 'Shymkent', NULL, ST_SetSRID(ST_MakePoint(69.58421, 42.318756), 4326)
FROM countries c
WHERE c.code = 'KZ'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '0ed10b5a-73c6-55cb-a1a5-78479e68c63b'::uuid, c.id, 'Тересполь', 'Terespol', NULL, ST_SetSRID(ST_MakePoint(23.640801, 52.068554), 4326)
FROM countries c
WHERE c.code = 'PL'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'fc69fecb-4431-52ef-8d76-dba6ea859a71'::uuid, c.id, 'Белград', 'Belgrade', NULL, ST_SetSRID(ST_MakePoint(20.460684, 44.816663), 4326)
FROM countries c
WHERE c.code = 'RS'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '22f5068d-2c59-5497-a368-c31d6fbd164d'::uuid, c.id, 'Абакан', 'Abakan', NULL, ST_SetSRID(ST_MakePoint(91.431597, 53.741383), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '6ce5c217-82f7-5294-954a-e9a3efe73b57'::uuid, c.id, 'Альметьевск', 'Almetevsk', NULL, ST_SetSRID(ST_MakePoint(52.302541, 54.899231), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'f3fc4785-d253-5562-98ab-e274e1d1fd6c'::uuid, c.id, 'Анапа', 'Anapa', NULL, ST_SetSRID(ST_MakePoint(37.317605, 44.894975), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'dacf34d5-dd51-54a5-b9ee-db0fba1fe34e'::uuid, c.id, 'Арамиль', 'Aramil', NULL, ST_SetSRID(ST_MakePoint(60.833706, 56.697398), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'e8904bab-502b-5f60-8d2d-a4bffb431a7f'::uuid, c.id, 'Армавир', 'Armavir', NULL, ST_SetSRID(ST_MakePoint(41.129629, 44.999995), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '33148e84-f9b7-50a0-9ffb-7102d3e57b3c'::uuid, c.id, 'Архангельск', 'Arkhangelsk', NULL, ST_SetSRID(ST_MakePoint(40.574504, 64.550552), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'e4158586-24f9-56f7-8d5b-e6af91ac8a04'::uuid, c.id, 'Балашиха', 'Balashikha', NULL, ST_SetSRID(ST_MakePoint(37.936944, 55.800094), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'eebefda5-4eb0-56f3-b474-7bb3cfb1aacc'::uuid, c.id, 'Белогорск', 'Belogorsk', NULL, ST_SetSRID(ST_MakePoint(128.472332, 50.922151), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'fd42bbcd-67ec-5b68-8ec6-9721bebf105b'::uuid, c.id, 'Белореченск', 'Belorechensk', NULL, ST_SetSRID(ST_MakePoint(39.872353, 44.761839), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '3d790e90-7f80-5f53-bce9-ce9e9b93c0b0'::uuid, c.id, 'Бийск', 'Biysk', NULL, ST_SetSRID(ST_MakePoint(85.230808, 52.544108), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '6f0a720a-16f6-5973-acfd-514cb8cdc914'::uuid, c.id, 'Бобров', 'Bobrov', NULL, ST_SetSRID(ST_MakePoint(40.036052, 51.096515), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '31eb380c-d837-515a-8e16-3976ed51293e'::uuid, c.id, 'Бор', 'Bor', NULL, ST_SetSRID(ST_MakePoint(44.073932, 56.355667), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'e1a70a03-74a7-5e69-aa6d-eb1714272c77'::uuid, c.id, 'Борисоглебск', 'Borisoglebsk', NULL, ST_SetSRID(ST_MakePoint(42.092404, 51.35758), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'b8fd4e09-60f9-5273-bcea-b14c9f2f1d30'::uuid, c.id, 'Боровичи', 'Borovichi', NULL, ST_SetSRID(ST_MakePoint(33.910483, 58.389568), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '3144c92d-39f7-508d-b940-e1404b23a3de'::uuid, c.id, 'Брянск', 'Bryansk', NULL, ST_SetSRID(ST_MakePoint(34.347944, 53.237129), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'b92a7367-fc85-537d-84ba-38247d7e54d3'::uuid, c.id, 'Волгоград', 'Volgograd', NULL, ST_SetSRID(ST_MakePoint(44.443663, 48.665895), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '7b793692-9e67-5a0b-b53e-12a9e7064a9b'::uuid, c.id, 'Вольск', 'Volsk', NULL, ST_SetSRID(ST_MakePoint(47.386034, 52.049803), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '1f1420b9-c565-5551-8488-cb169aedf2ee'::uuid, c.id, 'Выкса', 'Vyksa', NULL, ST_SetSRID(ST_MakePoint(42.185257, 55.32105), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'd23bdb9a-a4f1-5bbf-a100-1765e09c9ccf'::uuid, c.id, 'Вязьма', 'Vyazma', NULL, ST_SetSRID(ST_MakePoint(34.288444, 55.210449), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '17f78dcd-406c-559f-a84b-2e30012c5831'::uuid, c.id, 'Гатчина', 'Gatchina', NULL, ST_SetSRID(ST_MakePoint(30.104332, 59.559237), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '7e02e6d5-bc44-570f-b2ef-fbb6780e9a4f'::uuid, c.id, 'Гороховец', 'Gorokhovets', NULL, ST_SetSRID(ST_MakePoint(42.680804, 56.207565), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '00a05085-5ac1-59ce-83e4-4f5b47c1a060'::uuid, c.id, 'Гусев', 'Gusev', NULL, ST_SetSRID(ST_MakePoint(22.201119, 54.59281), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'dac0c06e-ccff-559c-8711-be3b2037036e'::uuid, c.id, 'Данилов', 'Danilov', NULL, ST_SetSRID(ST_MakePoint(40.178439, 58.185812), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '39c08db4-57cf-5477-8a77-7053296c8f34'::uuid, c.id, 'Десногорск', 'Desnogorsk', NULL, ST_SetSRID(ST_MakePoint(33.284573, 54.15275), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'db941f0f-9ead-5d71-b3b1-45d1af183bc3'::uuid, c.id, 'Дзержинск', 'Dzerzhinsk', NULL, ST_SetSRID(ST_MakePoint(43.456994, 56.297349), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'c312843d-3526-5cf3-b545-df66406fc349'::uuid, c.id, 'Дубна', 'Dubna', NULL, ST_SetSRID(ST_MakePoint(37.136929, 56.727406), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '03df331e-287b-5e58-9cf8-f9f48adafe05'::uuid, c.id, 'Дюртюли', 'Dyurtyuli', NULL, ST_SetSRID(ST_MakePoint(54.854506, 55.489184), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'b1daab5f-8bee-59a8-9cb8-a4e3ce58e896'::uuid, c.id, 'Екатеринбург', 'Ekaterinburg', NULL, ST_SetSRID(ST_MakePoint(60.545635, 56.864822), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'b652f64b-9b00-58b0-a086-d6253854c0d8'::uuid, c.id, 'Жигулёвск', 'Zhigulyovsk', NULL, ST_SetSRID(ST_MakePoint(49.528583, 53.431524), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'dfd8d7e3-7c46-5f30-abac-6d86763e15b9'::uuid, c.id, 'Жуковский', 'Zhukovskiy', NULL, ST_SetSRID(ST_MakePoint(38.122529, 55.588738), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '591da311-a2ca-5a90-a9e6-e3cab2555808'::uuid, c.id, 'Заволжье', 'Zavolzhe', NULL, ST_SetSRID(ST_MakePoint(43.392366, 56.644711), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'a2f1f7eb-e4de-5523-9342-2aede8ee5adc'::uuid, c.id, 'Зеленоград', 'Zelenograd', NULL, ST_SetSRID(ST_MakePoint(37.175135, 55.982699), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '53f97207-209e-5e06-8a28-bfa44589ec86'::uuid, c.id, 'Ижевск', 'Izhevsk', NULL, ST_SetSRID(ST_MakePoint(53.219694, 56.854032), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '21909867-8ba8-5015-908c-1d74986e6185'::uuid, c.id, 'Искитим', 'Iskitim', NULL, ST_SetSRID(ST_MakePoint(83.281291, 54.611487), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'cd628379-9822-5c2c-ab3d-22bba8f470fd'::uuid, c.id, 'Истра', 'Istra', NULL, ST_SetSRID(ST_MakePoint(36.857964, 55.903799), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '1cbf1f4a-6bcf-588d-b427-2ae70daaef74'::uuid, c.id, 'Ишим', 'Ishim', NULL, ST_SetSRID(ST_MakePoint(69.467335, 56.107724), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'd4bd5412-527f-5f5a-b3c2-658304893ac1'::uuid, c.id, 'Йошкар-Ола', 'Yoshkar-Ola', NULL, ST_SetSRID(ST_MakePoint(47.884289, 56.63317), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '900966c4-a84d-5adc-b3f1-2e264a866dbb'::uuid, c.id, 'Казань', 'Kazan', NULL, ST_SetSRID(ST_MakePoint(49.15267, 55.798425), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'fd4a6b0b-837c-51c7-b2b3-3ea91f71efd3'::uuid, c.id, 'Калининград', 'Kaliningrad', NULL, ST_SetSRID(ST_MakePoint(20.52522, 54.730152), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '5da90341-d312-59f0-9f5f-28f543e43822'::uuid, c.id, 'Калязин', 'Kalyazin', NULL, ST_SetSRID(ST_MakePoint(37.839263, 57.23786), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'f3b1269b-ac11-58c9-9691-fe81baa46a8d'::uuid, c.id, 'Каменск-Уральский', 'Kamensk-Uralskiy', NULL, ST_SetSRID(ST_MakePoint(61.92954, 56.390576), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '94b5fb2c-ea71-5b58-88f4-8aed15c94822'::uuid, c.id, 'Кемерово', 'Kemerovo', NULL, ST_SetSRID(ST_MakePoint(86.088249, 55.3555), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '7cb5d84c-a6b9-5090-8adf-1d805dbe5611'::uuid, c.id, 'Кинешма', 'Kineshma', NULL, ST_SetSRID(ST_MakePoint(42.147572, 57.441012), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '1bea48bd-cc69-5346-97eb-588cff4a4f7c'::uuid, c.id, 'Киреевск', 'Kireevsk', NULL, ST_SetSRID(ST_MakePoint(37.928913, 53.926484), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '4317deda-e7f6-5dd0-b5e9-7783b4a3016f'::uuid, c.id, 'Кисловодск', 'Kislovodsk', NULL, ST_SetSRID(ST_MakePoint(42.710526, 43.909986), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'cb7ec5ea-a6de-5e49-978b-d6e25f2c0727'::uuid, c.id, 'Колпино', 'Kolpino', NULL, ST_SetSRID(ST_MakePoint(30.591323, 59.749047), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'adaef62e-1554-586b-a1a6-eb793606d60b'::uuid, c.id, 'Конаково', 'Konakovo', NULL, ST_SetSRID(ST_MakePoint(36.77721, 56.711549), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'ea04a544-699f-550f-8e49-86eb6dbe1a75'::uuid, c.id, 'Кондрово', 'Kondrovo', NULL, ST_SetSRID(ST_MakePoint(35.932103, 54.80815), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'e960d01d-c6a0-57b7-b6cd-584064242253'::uuid, c.id, 'Красногорск', 'Krasnogorsk', NULL, ST_SetSRID(ST_MakePoint(37.322767, 55.829025), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '899426a9-bc71-5b10-ae46-86d890eadfd5'::uuid, c.id, 'Красноуфимск', 'Krasnoufimsk', NULL, ST_SetSRID(ST_MakePoint(57.770937, 56.615268), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '5ec262e3-8cc5-52f0-aa46-91113065df1d'::uuid, c.id, 'Крымск', 'Krymsk', NULL, ST_SetSRID(ST_MakePoint(37.986668, 44.934511), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '166cb00c-0e3a-53af-b4c5-ce4b0d0d9513'::uuid, c.id, 'Куйбышев', 'Kuybyshev', NULL, ST_SetSRID(ST_MakePoint(78.326281, 55.447371), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '33f461f2-16c5-5601-bced-96d83798f8da'::uuid, c.id, 'Кунгур', 'Kungur', NULL, ST_SetSRID(ST_MakePoint(56.944259, 57.428119), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '1057a89f-9005-5439-9dc8-a7d82cc696c9'::uuid, c.id, 'Курск', 'Kursk', NULL, ST_SetSRID(ST_MakePoint(36.187679, 51.746076), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'c4742443-b04e-5c92-8596-e791fbb41017'::uuid, c.id, 'Лабытнанги', 'Labytnangi', NULL, ST_SetSRID(ST_MakePoint(66.375955, 66.66213), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '8919fc79-daeb-556f-814e-dfa42d7791ca'::uuid, c.id, 'Лакинск', 'Lakinsk', NULL, ST_SetSRID(ST_MakePoint(39.975549, 56.020901), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '74615681-f481-5c21-8763-2a96aae88ffa'::uuid, c.id, 'Лысково', 'Lyskovo', NULL, ST_SetSRID(ST_MakePoint(45.055629, 56.017133), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '572266bc-3606-55bc-97a8-d354e6884e98'::uuid, c.id, 'Майкоп', 'Maykop', NULL, ST_SetSRID(ST_MakePoint(40.098517, 44.589473), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'cd30f9e9-e4d0-56f5-be4d-53c20ac980f3'::uuid, c.id, 'Махачкала', 'Makhachkala', NULL, ST_SetSRID(ST_MakePoint(47.489923, 42.987026), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '6ace1272-e5f2-53b5-b609-233814ee8c48'::uuid, c.id, 'Междуреченск', 'Mezhdurechensk', NULL, ST_SetSRID(ST_MakePoint(88.082576, 53.684849), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'eacf78ba-b0a9-555c-a532-4f4a80416def'::uuid, c.id, 'Миасс', 'Miass', NULL, ST_SetSRID(ST_MakePoint(60.114698, 55.065832), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'f26a9ee1-449a-5be1-afeb-3a6c80b4f23a'::uuid, c.id, 'Москва', 'Moscow', NULL, ST_SetSRID(ST_MakePoint(37.287234, 55.605509), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'effce173-874f-5695-b397-f116cf912b64'::uuid, c.id, 'Московский', 'Moskovsky', NULL, ST_SetSRID(ST_MakePoint(37.349355, 55.592617), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '0261af3a-158c-511c-bcbc-ce5bf535170a'::uuid, c.id, 'Мурино', 'Murino', NULL, ST_SetSRID(ST_MakePoint(30.474928, 60.046925), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'e2c5a302-7d0d-5b40-ae9b-98827fcba3cf'::uuid, c.id, 'Мценск', 'Mtsensk', NULL, ST_SetSRID(ST_MakePoint(36.565919, 53.292956), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '8e0a4fe8-83e1-5811-9d7f-94204f19db4f'::uuid, c.id, 'Нальчик', 'Nalchik', NULL, ST_SetSRID(ST_MakePoint(43.587652, 43.45927), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '1995a6f4-28c5-5570-b2da-74bc7e742748'::uuid, c.id, 'Невинномысск', 'Nevinnomyssk', NULL, ST_SetSRID(ST_MakePoint(41.937069, 44.644796), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'ffd55b04-0587-5aa1-a4e8-fc32a6ca1a7c'::uuid, c.id, 'Нижнекамск', 'Nizhnekamsk', NULL, ST_SetSRID(ST_MakePoint(51.820059, 55.642512), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'be2bc773-6db4-5f63-bf0b-bfeca08ddfeb'::uuid, c.id, 'Новоалтайск', 'Novoaltaysk', NULL, ST_SetSRID(ST_MakePoint(83.933502, 53.419879), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'a227ba47-0205-5ffc-adcd-861490c01558'::uuid, c.id, 'Новокузнецк', 'Novokuznetsk', NULL, ST_SetSRID(ST_MakePoint(87.15063, 53.758549), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'e9873a9a-4938-51a8-a1d5-1c147246c533'::uuid, c.id, 'Новосибирск', 'Novosibirsk', NULL, ST_SetSRID(ST_MakePoint(82.918026, 55.041808), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '767b91c7-dae2-5124-adb4-a6b7bc4aeffc'::uuid, c.id, 'Ногинск', 'Noginsk', NULL, ST_SetSRID(ST_MakePoint(38.442098, 55.852201), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'f7017758-c728-5e38-bd35-9321f6d7b5ba'::uuid, c.id, 'Озёры', 'Ozyory', NULL, ST_SetSRID(ST_MakePoint(38.557567, 54.853537), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'cb3c1275-b170-54e9-8ef5-af8b699ce4f5'::uuid, c.id, 'Октябрьский', 'Oktyabrskiy', NULL, ST_SetSRID(ST_MakePoint(53.469961, 54.48854), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'd736c98c-7377-5300-8766-b3bafea47bfd'::uuid, c.id, 'Орёл', 'Oryol', NULL, ST_SetSRID(ST_MakePoint(36.068584, 52.972649), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '1680a3a4-9cc3-5727-8dff-14c216fc8184'::uuid, c.id, 'Оренбург', 'Orenburg', NULL, ST_SetSRID(ST_MakePoint(55.073989, 51.773652), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '4f81022a-8a16-5c04-a817-0b91df62bb01'::uuid, c.id, 'Петушки', 'Petushki', NULL, ST_SetSRID(ST_MakePoint(39.463267, 55.924903), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '80855589-d3a3-50eb-b373-07647d94823e'::uuid, c.id, 'Пионерский', 'Pionerskiy', NULL, ST_SetSRID(ST_MakePoint(20.225029, 54.949329), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '8a55a117-8e57-5997-912a-f9666041173b'::uuid, c.id, 'Протвино', 'Protvino', NULL, ST_SetSRID(ST_MakePoint(37.222657, 54.877201), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '53635091-6abe-506e-b48d-d6414ca5edae'::uuid, c.id, 'Псков', 'Pskov', NULL, ST_SetSRID(ST_MakePoint(28.358519, 57.802347), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'f9c931c6-c473-5df8-bc67-457e02f73525'::uuid, c.id, 'Пугачев', 'Pugachev', NULL, ST_SetSRID(ST_MakePoint(48.813944, 52.010122), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '35668fd1-4506-5793-9dde-ace82565f8b2'::uuid, c.id, 'Пушкин', 'Pushkin', NULL, ST_SetSRID(ST_MakePoint(30.418715, 59.713549), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'daab68f8-e41d-5c07-ae7c-414485ae14ac'::uuid, c.id, 'Пущино', 'Pushchino', NULL, ST_SetSRID(ST_MakePoint(37.632521, 54.832818), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '4127885a-4cf9-563b-8d6f-766244cdec72'::uuid, c.id, 'Россошь', 'Rossosh', NULL, ST_SetSRID(ST_MakePoint(39.577637, 50.19513), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '947e2d8c-780a-55a7-a5ce-1047da7c6723'::uuid, c.id, 'Ростов', 'Rostov', NULL, ST_SetSRID(ST_MakePoint(39.415949, 57.184266), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'c5ed0c2c-41b6-5a03-9be9-e3e8ad84bfc2'::uuid, c.id, 'Рязань', 'Ryazan', NULL, ST_SetSRID(ST_MakePoint(39.764649, 54.634636), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '2da1aa01-e83b-55be-bd70-de227d8025e6'::uuid, c.id, 'Самара', 'Samara', NULL, ST_SetSRID(ST_MakePoint(50.245422, 53.278459), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '028972b2-e72f-5d96-bcd2-3c368276c04b'::uuid, c.id, 'Санкт-Петербург', 'Saint Petersburg', NULL, ST_SetSRID(ST_MakePoint(29.277595, 60.474196), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '551ffde9-176d-5f5a-a2f5-d5a0ab11b41d'::uuid, c.id, 'Саранск', 'Saransk', NULL, ST_SetSRID(ST_MakePoint(45.1814, 54.174869), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'd9ef94a4-c40f-52c0-9cca-dd92c4b77c86'::uuid, c.id, 'Саров', 'Sarov', NULL, ST_SetSRID(ST_MakePoint(43.33069, 54.935979), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '9a5a6725-a320-5ecf-b547-ccd4ca6d0568'::uuid, c.id, 'Светлогорск', 'Svetlogorsk', NULL, ST_SetSRID(ST_MakePoint(20.159589, 54.933407), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '9168752c-1783-5a8d-a6f5-5ab6b00633c6'::uuid, c.id, 'Севастополь', 'Sevastopol', NULL, ST_SetSRID(ST_MakePoint(33.529135, 44.594531), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '3832b4e3-e5d9-5852-9072-62eb720924aa'::uuid, c.id, 'Сергач', 'Sergach', NULL, ST_SetSRID(ST_MakePoint(45.49437, 55.519712), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'a91e3ff6-f183-560c-ad4f-c85d666d6947'::uuid, c.id, 'Серпухов', 'Serpukhov', NULL, ST_SetSRID(ST_MakePoint(37.411421, 54.913684), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '3f754b06-174b-5a10-9c7d-774caee678cc'::uuid, c.id, 'Симферополь', 'Simferopol', NULL, ST_SetSRID(ST_MakePoint(34.091595, 44.944987), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'ef76723f-80a9-59a0-bf3d-57238d311a8a'::uuid, c.id, 'Сланцы', 'Slantsy', NULL, ST_SetSRID(ST_MakePoint(28.086461, 59.118508), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '2774f19b-fab9-51d5-8859-f9dbe8d885f9'::uuid, c.id, 'Слободской', 'Slobodskoy', NULL, ST_SetSRID(ST_MakePoint(50.181551, 58.721338), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '1f151ba1-847b-5d19-a7a8-007c445bf3e0'::uuid, c.id, 'Сортавала', 'Sortavala', NULL, ST_SetSRID(ST_MakePoint(30.944184, 61.388597), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '47601697-41c1-505d-a34e-d8276562ccb6'::uuid, c.id, 'Сосновый Бор', 'Sosnovyy Bor', NULL, ST_SetSRID(ST_MakePoint(29.086244, 59.910784), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'b65461d3-871f-5a9b-8bd2-e2045a55f794'::uuid, c.id, 'Сочи', 'Sochi', NULL, ST_SetSRID(ST_MakePoint(40.310183, 43.625163), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'ad4d6fd8-0a9e-5a9a-bf4b-cd0f42d30951'::uuid, c.id, 'Старая Русса', 'Staraya Russa', NULL, ST_SetSRID(ST_MakePoint(31.36438, 57.993591), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '438fb590-a77f-510d-8ded-5503cc1710b3'::uuid, c.id, 'Суздаль', 'Suzdal', NULL, ST_SetSRID(ST_MakePoint(40.441331, 56.431975), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '7d8f8242-1971-551b-a2e6-da459e750873'::uuid, c.id, 'Сызрань', 'Syzran', NULL, ST_SetSRID(ST_MakePoint(48.480291, 53.169933), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'aa1ba7a8-5d1d-5b80-87b1-be4d9e5ce947'::uuid, c.id, 'Сыктывкар', 'Syktyvkar', NULL, ST_SetSRID(ST_MakePoint(50.834177, 61.667135), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'ae6aaaf5-716e-529c-8d64-a2657692ff8d'::uuid, c.id, 'Таганрог', 'Taganrog', NULL, ST_SetSRID(ST_MakePoint(38.945288, 47.203606), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'd0895816-2912-5cd9-a17b-4a38e9ffd0b2'::uuid, c.id, 'Тейково', 'Teykovo', NULL, ST_SetSRID(ST_MakePoint(40.535029, 56.851831), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'b028467d-92d1-5259-9db8-08ece64f6195'::uuid, c.id, 'Тимашевск', 'Timashevsk', NULL, ST_SetSRID(ST_MakePoint(38.939568, 45.628175), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '07c0da1d-2562-5837-92b2-55130aaad02a'::uuid, c.id, 'Тихвин', 'Tikhvin', NULL, ST_SetSRID(ST_MakePoint(33.534969, 59.644604), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '96c80b8e-e428-53e3-92e7-8f5b8defa5c8'::uuid, c.id, 'Троицк', 'Troitsk', NULL, ST_SetSRID(ST_MakePoint(37.288669, 55.454378), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '877ebcd4-af84-5dfd-ba08-b0e2459d0f00'::uuid, c.id, 'Тула', 'Tula', NULL, ST_SetSRID(ST_MakePoint(37.499633, 54.413427), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '25192ece-6343-5450-962a-b99698919d00'::uuid, c.id, 'Тутаев', 'Tutaev', NULL, ST_SetSRID(ST_MakePoint(39.541683, 57.881559), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '2dfde700-3454-5b2a-86dc-0cc0adf024ef'::uuid, c.id, 'Тюмень', 'Tyumen', NULL, ST_SetSRID(ST_MakePoint(65.535311, 57.151626), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'e585d6ea-3c82-5f71-ad13-1efb3d92b105'::uuid, c.id, 'Узловая', 'Uzlovaya', NULL, ST_SetSRID(ST_MakePoint(38.158047, 53.976043), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '411d3520-6dd6-5d0f-ab32-9ff27705d3c7'::uuid, c.id, 'Усть-Лабинск', 'Ust-Labinsk', NULL, ST_SetSRID(ST_MakePoint(39.689431, 45.215164), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '20409d7f-e341-56eb-976c-a7d3332de2e6'::uuid, c.id, 'Уфа', 'Ufa', NULL, ST_SetSRID(ST_MakePoint(55.985556, 54.750968), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '4d198006-6f34-5ee6-94f2-c79c02bb79bd'::uuid, c.id, 'Чапаевск', 'Chapaevsk', NULL, ST_SetSRID(ST_MakePoint(49.746677, 52.989281), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '301266cc-22a4-5eb7-9fa4-735cc1153ae2'::uuid, c.id, 'Челябинск', 'Chelyabinsk', NULL, ST_SetSRID(ST_MakePoint(61.399827, 55.163731), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'f4598f17-e722-51f6-934d-226992009461'::uuid, c.id, 'Череповец', 'Cherepovets', NULL, ST_SetSRID(ST_MakePoint(37.928552, 59.123938), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'eddd9387-20e5-573e-b2c3-672d85dc71b8'::uuid, c.id, 'Черкесск', 'Cherkessk', NULL, ST_SetSRID(ST_MakePoint(42.047311, 44.227244), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'ea6a374c-c071-57ce-a5c5-eccdebe6c56d'::uuid, c.id, 'Чита', 'Chita', NULL, ST_SetSRID(ST_MakePoint(113.499581, 52.033012), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'f852a17c-3877-5bd4-bb42-5eac586a30bd'::uuid, c.id, 'Шатура', 'Shatura', NULL, ST_SetSRID(ST_MakePoint(39.521316, 55.57369), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '27c0f8fd-e13e-5089-9844-ca8774129ec1'::uuid, c.id, 'Шелехов', 'Shelekhov', NULL, ST_SetSRID(ST_MakePoint(104.083688, 52.205373), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '8d3daf54-5801-56ff-831a-63972a7f34de'::uuid, c.id, 'Шуя', 'Shuya', NULL, ST_SetSRID(ST_MakePoint(41.39126, 56.857337), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'e7928f7e-0ff8-55e8-b91a-eb97046fc256'::uuid, c.id, 'Щёлкино', 'Shchyolkino', NULL, ST_SetSRID(ST_MakePoint(35.822069, 45.429443), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '5e25bf9d-fa46-5658-8769-7cf20f5586fc'::uuid, c.id, 'Щербинка', 'Shcherbinka', NULL, ST_SetSRID(ST_MakePoint(37.568088, 55.499285), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'faf08385-bd70-5bb8-906f-0f79aa1ea519'::uuid, c.id, 'Элиста', 'Elista', NULL, ST_SetSRID(ST_MakePoint(44.264148, 46.308318), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '3d60b8ca-e383-539a-8b4e-3170347f76cd'::uuid, c.id, 'Южно-Сахалинск', 'Yuzhno-Sakhalinsk', NULL, ST_SetSRID(ST_MakePoint(142.752994, 46.965357), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '554db5bf-64ed-5c0b-b5c4-e1ca930344c6'::uuid, c.id, 'Юрга', 'Yurga', NULL, ST_SetSRID(ST_MakePoint(84.928494, 55.713093), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'c2b1316f-a8fe-550b-a42f-cdc6220e16c0'::uuid, c.id, 'Юрьев-Польский', 'Yuryev-Polsky', NULL, ST_SetSRID(ST_MakePoint(39.668526, 56.484024), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '2aa0b893-74f3-5ba8-a006-f26cc39db8d9'::uuid, c.id, 'Якутск', 'Yakutsk', NULL, ST_SetSRID(ST_MakePoint(129.728985, 62.029183), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'f468b2fd-6a2e-57c0-ac7d-35acfbe82168'::uuid, c.id, 'Яровое', 'Yarovoye', NULL, ST_SetSRID(ST_MakePoint(78.563671, 52.922509), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '70c9d0b9-0699-5861-a990-3f0bb71d7e9b'::uuid, c.id, 'Ярцево', 'Yartsevo', NULL, ST_SetSRID(ST_MakePoint(32.771675, 55.081878), 4326)
FROM countries c
WHERE c.code = 'RU'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '8d769692-3728-5885-8222-c045653a59d7'::uuid, c.id, 'Бурса', 'Bursa', NULL, ST_SetSRID(ST_MakePoint(29.139934, 40.120027), 4326)
FROM countries c
WHERE c.code = 'TR'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'b55117a5-1102-5928-973c-80668440d2cd'::uuid, c.id, 'Гебзе', 'Gebze', NULL, ST_SetSRID(ST_MakePoint(29.4298, 40.7988), 4326)
FROM countries c
WHERE c.code = 'TR'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '97776fb1-9ce3-5263-905d-744d54f0ad87'::uuid, c.id, 'Дарыджа', 'Darica', NULL, ST_SetSRID(ST_MakePoint(29.3793, 40.7565), 4326)
FROM countries c
WHERE c.code = 'TR'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '5d5bb14a-63c4-5e33-b8dd-0b26283a5429'::uuid, c.id, 'Орхангази', 'Orhangazi', NULL, ST_SetSRID(ST_MakePoint(29.309461, 40.490713), 4326)
FROM countries c
WHERE c.code = 'TR'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '99dd14f7-3ba2-5d69-93b8-e4a3bd418d49'::uuid, c.id, 'Стамбул', 'Istanbul', NULL, ST_SetSRID(ST_MakePoint(29.208879, 40.918202), 4326)
FROM countries c
WHERE c.code = 'TR'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '3df89423-b586-5a4a-9711-a39e0203d5a1'::uuid, c.id, 'Харманджик', 'Harmancik', NULL, ST_SetSRID(ST_MakePoint(29.1477, 39.6787), 4326)
FROM countries c
WHERE c.code = 'TR'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'd89bfd1c-a754-5617-856e-f587749222bd'::uuid, c.id, 'Чайырова', 'Cayirova', NULL, ST_SetSRID(ST_MakePoint(29.4204, 40.8353), 4326)
FROM countries c
WHERE c.code = 'TR'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '0484c4c8-e547-5cdf-a597-af56837cfa3b'::uuid, c.id, 'Шиле', 'Shile', NULL, ST_SetSRID(ST_MakePoint(29.412808, 41.202708), 4326)
FROM countries c
WHERE c.code = 'TR'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '27f6daa2-6308-5067-8750-5e02091aaac5'::uuid, c.id, 'Килия', 'Kiliya', NULL, ST_SetSRID(ST_MakePoint(29.269513, 45.43594), 4326)
FROM countries c
WHERE c.code = 'UA'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '6d78fa3b-daa6-522a-a154-81e32190676f'::uuid, c.id, 'Малин', 'Malyn', NULL, ST_SetSRID(ST_MakePoint(29.236416, 50.774176), 4326)
FROM countries c
WHERE c.code = 'UA'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT 'db5b70a5-5607-5fcc-ad22-042b6986c7d2'::uuid, c.id, 'Мариуполь', 'Mariupol', NULL, ST_SetSRID(ST_MakePoint(37.543672, 47.096903), 4326)
FROM countries c
WHERE c.code = 'UA'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '0f8ecde0-3e4e-53ad-988f-73954fc8ca09'::uuid, c.id, 'Погребище', 'Pohrebyshche', NULL, ST_SetSRID(ST_MakePoint(29.258574, 49.48453), 4326)
FROM countries c
WHERE c.code = 'UA'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '2545d5fb-659d-5c37-b968-b40c90b9b611'::uuid, c.id, 'Ташкент', 'Tashkent', NULL, ST_SetSRID(ST_MakePoint(69.250743, 41.285858), 4326)
FROM countries c
WHERE c.code = 'UZ'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
SELECT '2d545533-a6a8-5ba3-9717-86bd8d6bc384'::uuid, c.id, 'Фергана', 'Fergana', NULL, ST_SetSRID(ST_MakePoint(71.785431, 40.389803), 4326)
FROM countries c
WHERE c.code = 'UZ'
ON CONFLICT (country_id, name_ru) DO UPDATE
SET id = EXCLUDED.id,
    name_en = EXCLUDED.name_en,
    region = EXCLUDED.region,
    coordinates = EXCLUDED.coordinates;

-- rollback statements are intentionally omitted because this seed is shared by multiple imports.
