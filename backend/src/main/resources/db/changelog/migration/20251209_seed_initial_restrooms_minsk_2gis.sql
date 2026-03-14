-- liquibase formatted sql


-- 1. Ensure country BY (Беларусь) exists
INSERT INTO countries (id, code, name_ru, name_en)
VALUES ('00000000-0000-0000-0000-0000000000b1'::uuid, 'BY', 'Беларусь', 'Belarus')
ON CONFLICT (code) DO UPDATE
SET name_ru = EXCLUDED.name_ru,
    name_en = EXCLUDED.name_en;

-- 2. Ensure city Минск exists (coordinates ≈ центр города)
INSERT INTO cities (id, country_id, name_ru, name_en, region, coordinates)
VALUES (
    '00000000-0000-0000-0000-0000000000c1'::uuid,
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
    id,
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
    '00000000-0000-0000-0000-0000000000d1'::uuid,
    '00000000-0000-0000-0000-0000000000c1'::uuid,
    'Минский железнодорожный вокзал',
    'Минск, Привокзальная площадь, 5',
    'railway_station',
    NULL,
    ST_SetSRID(ST_MakePoint(27.55127, 53.890864), 4326),
    '{"2gis_building_id": "70030076195149995"}'::jsonb,
    'COMPLETED'
)
ON CONFLICT (id) DO NOTHING;

-- 4. Restroom #1: Мужской платный туалет (predefined id)
INSERT INTO restrooms (
    id,
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
    '00000000-0000-0000-0000-0000000000e1'::uuid,
    '00000000-0000-0000-0000-0000000000c1'::uuid,
    '00000000-0000-0000-0000-0000000000d1'::uuid,
    'f3f404f3-1dfe-451e-8193-727e60685b80'::uuid,

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
)
ON CONFLICT (id) DO NOTHING;

-- 5. Restroom #2: Женский платный туалет (predefined id)
INSERT INTO restrooms (
    id,
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
    '00000000-0000-0000-0000-0000000000e2'::uuid,
    '00000000-0000-0000-0000-0000000000c1'::uuid,
    '00000000-0000-0000-0000-0000000000d1'::uuid,
    'f3f404f3-1dfe-451e-8193-727e60685b80'::uuid,

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
)
ON CONFLICT (id) DO NOTHING;

-- rollback DELETE FROM restrooms WHERE data_source = 'IMPORT' AND address = 'Минск, Привокзальная площадь, 5';
-- rollback DELETE FROM buildings WHERE address = 'Минск, Привокзальная площадь, 5';
