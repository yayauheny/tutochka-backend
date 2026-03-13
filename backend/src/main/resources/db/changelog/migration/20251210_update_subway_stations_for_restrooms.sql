-- liquibase formatted sql

-- changeset yayauheny:update-subway-stations-for-restrooms
-- Проставляет ближайшие станции метро для всех туалетов в Минске

-- Обновляем subway_station_id для всех туалетов в Минске, у которых он не проставлен
-- Используем подзапрос для поиска ближайшей станции метро в том же городе
UPDATE restrooms r
SET subway_station_id = (
    SELECT s.id
    FROM subway_stations s
    JOIN subway_lines l ON s.subway_line_id = l.id
    WHERE l.city_id = r.city_id
      AND s.is_deleted = false
      AND l.is_deleted = false
    ORDER BY s.coordinates <-> r.coordinates
    LIMIT 1
),
updated_at = NOW()
WHERE r.city_id = (SELECT id FROM cities WHERE country_id = (SELECT id FROM countries WHERE code = 'BY') AND name_ru = 'Минск')
  AND r.subway_station_id IS NULL
  AND r.is_deleted = false;

-- rollback UPDATE restrooms SET subway_station_id = NULL, updated_at = NOW() WHERE city_id = (SELECT id FROM cities WHERE country_id = (SELECT id FROM countries WHERE code = 'BY') AND name_ru = 'Минск') AND subway_station_id IS NOT NULL;



