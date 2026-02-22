-- liquibase formatted sql

-- changeset yayauheny:seed-initial-restrooms-minsk-2gis

-- 1. Ensure country BY (Беларусь) exists
INSERT INTO countries (code, name_ru, name_en)
VALUES ('BY', 'Беларусь', 'Belarus')
ON CONFLICT (code) DO UPDATE
SET name_ru = EXCLUDED.name_ru,
    name_en = EXCLUDED.name_en;

-- 2. Ensure city Минск exists (coordinates ≈ центр города)
INSERT INTO cities (country_id, name_ru, name_en, region, coordinates)
VALUES (
    (SELECT id FROM countries WHERE code = 'BY'),
    'Минск',
    'Minsk',
    NULL,
    ST_SetSRID(ST_MakePoint(27.5619, 53.9023), 4326)
)
ON CONFLICT (country_id, name_ru) DO UPDATE
SET name_en = EXCLUDED.name_en;

-- 3. Building: Минск, Привокзальная площадь, 5 (ЖД вокзал)
INSERT INTO buildings (
    city_id,
    name,
    address,
    building_type,
    work_time,
    coordinates,
    external_ids,
    import_status
)
VALUES (
    (SELECT id FROM cities WHERE country_id = (SELECT id FROM countries WHERE code = 'BY') AND name_ru = 'Минск'),
    'Минский железнодорожный вокзал',
    'Минск, Привокзальная площадь, 5',
    'railway_station',
    NULL,
    ST_SetSRID(ST_MakePoint(27.55127, 53.890864), 4326),
    '{"2gis_building_id": "70030076195149995"}'::jsonb,
    'COMPLETED'
);

-- 4. Restroom #1: Мужской платный туалет
INSERT INTO restrooms (
    city_id,
    building_id,
    subway_station_id,

    name,
    place_type,
    address,

    direction_guide,
    access_note,

    fee_type,
    gender_type,
    accessibility_type,
    status,

    phones,
    work_time,
    inherit_building_schedule,

    amenities,
    has_photos,

    coordinates,
    external_maps,
    data_source
)
VALUES (
    (SELECT id FROM cities WHERE country_id = (SELECT id FROM countries WHERE code = 'BY') AND name_ru = 'Минск'),
    (SELECT id
     FROM buildings
     WHERE city_id = (SELECT id FROM cities WHERE country_id = (SELECT id FROM countries WHERE code = 'BY') AND name_ru = 'Минск')
       AND address = 'Минск, Привокзальная площадь, 5'
     ORDER BY created_at ASC
     LIMIT 1),
    NULL,

    'Мужской платный туалет',
    'public_toilet',
    'Минск, Привокзальная площадь, 5',

    '-1 этаж',
    'Туалет для маломобильных людей; доступный вход для людей с инвалидностью',

    'PAID',
    'MEN',
    'DISABLED',
    'ACTIVE',

    NULL,
    '{"is_24x7": true}'::jsonb,
    false,

    '{
       "payment_methods": ["card","cash"],
       "accessible_toilet": true,
       "accessible_entrance": true,
       "source_attributes": [
         "additionalgarbage_availabilitytoilet_paid_toilet",
         "accessible_entrance_inclusive_environment_wc_inclusive",
         "accessible_entrance_accessible_entrance"
       ]
     }'::jsonb,
    true,

    ST_SetSRID(ST_MakePoint(27.55127, 53.890864), 4326),
    '{"2gis": {"branch_id": "70000001081591971"}}'::jsonb,
    'IMPORT'
);

-- 5. Restroom #2: Женский платный туалет (тот же адрес, те же условия)
INSERT INTO restrooms (
    city_id,
    building_id,
    subway_station_id,

    name,
    place_type,
    address,

    direction_guide,
    access_note,

    fee_type,
    gender_type,
    accessibility_type,
    status,

    phones,
    work_time,
    inherit_building_schedule,

    amenities,
    has_photos,

    coordinates,
    external_maps,
    data_source
)
VALUES (
    (SELECT id FROM cities WHERE country_id = (SELECT id FROM countries WHERE code = 'BY') AND name_ru = 'Минск'),
    (SELECT id
     FROM buildings
     WHERE city_id = (SELECT id FROM cities WHERE country_id = (SELECT id FROM countries WHERE code = 'BY') AND name_ru = 'Минск')
       AND address = 'Минск, Привокзальная площадь, 5'
     ORDER BY created_at ASC
     LIMIT 1),
    NULL,

    'Женский платный туалет',
    'public_toilet',
    'Минск, Привокзальная площадь, 5',

    '-1 этаж',
    'Туалет для маломобильных людей; доступный вход для людей с инвалидностью',

    'PAID',
    'WOMEN',
    'DISABLED',
    'ACTIVE',

    NULL,
    '{"is_24x7": true}'::jsonb,
    false,

    '{
       "payment_methods": ["card","cash"],
       "accessible_toilet": true,
       "accessible_entrance": true,
       "source_attributes": [
         "additionalgarbage_availabilitytoilet_paid_toilet",
         "accessible_entrance_inclusive_environment_wc_inclusive",
         "accessible_entrance_accessible_entrance"
       ]
     }'::jsonb,
    true,

    ST_SetSRID(ST_MakePoint(27.55127, 53.890864), 4326),
    '{"2gis": {"branch_id": "70000001081591972"}}'::jsonb,
    'IMPORT'
);

-- rollback DELETE FROM restrooms WHERE data_source = 'IMPORT' AND address = 'Минск, Привокзальная площадь, 5';
-- rollback DELETE FROM buildings WHERE address = 'Минск, Привокзальная площадь, 5';
