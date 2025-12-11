# Анализ производительности запросов к базе данных

## ✅ Исправленные проблемы

### 1. Лишние проверки `isNull` для `IS_DELETED`
**Проблема:** Во всех репозиториях использовалась проверка `.eq(false).or(.isNull)` для поля `is_deleted`, хотя поле имеет `DEFAULT false` и никогда не будет `NULL`.

**Исправлено в:**
- `RestroomRepositoryImpl` (5 мест)
- `BuildingRepositoryImpl` (1 место - метод `notDeletedCondition()`)
- `CityRepositoryImpl` (5 мест)
- `CountryRepositoryImpl` (3 места)
- `SubwayRepositoryImpl` (2 места - методы `notDeletedLineCondition()` и `notDeletedStationCondition()`)

**Результат:** Упрощены условия запросов, улучшена читаемость, небольшой прирост производительности за счет упрощения плана запроса.

---

### 2. Неэффективный поиск в JSONB через оператор `->>`
**Проблема:** `BuildingRepositoryImpl.findByExternalId()` использовал оператор `->>` для поиска в JSONB, который не использует GIN индекс эффективно.

**Было:**
```kotlin
DSL.condition("{0} ->> {1} = {2}", BUILDINGS.EXTERNAL_IDS, provider, externalId)
```

**Стало:**
```kotlin
DSL.condition("{0} @> jsonb_build_object({1}, {2})", BUILDINGS.EXTERNAL_IDS, provider, externalId)
```

**Результат:** Запрос теперь использует GIN индекс `idx_buildings_external_ids`, что дает значительный прирост производительности на больших объемах данных (10k+ записей).

**Аналогичная проблема была исправлена ранее в:**
- `RestroomRepositoryImpl.findByExternalMap()` - уже использует оператор `@>`

---

## ⚠️ Потенциальные проблемы производительности

### 1. `SubwayRepositoryImpl.setNearestStationForRestroom()` - Коррелированный подзапрос
**Метод:** `setNearestStationForRestroom(restroomId: UUID, lat: Double, lon: Double)`

**Проблема:** Используется коррелированный подзапрос с `RESTROOMS.CITY_ID` в WHERE условии подзапроса. Это может быть неэффективно при большом количестве станций метро.

**Текущая реализация:**
```kotlin
val nearestStationSelect = ctx
    .select(SUBWAY_STATIONS.ID)
    .from(SUBWAY_STATIONS)
    .join(SUBWAY_LINES)
    .on(SUBWAY_STATIONS.SUBWAY_LINE_ID.eq(SUBWAY_LINES.ID))
    .where(
        SUBWAY_LINES.CITY_ID.eq(RESTROOMS.CITY_ID)  // Коррелированный подзапрос
            .and(SUBWAY_STATIONS.IS_DELETED.isFalse)
            .and(SUBWAY_LINES.IS_DELETED.isFalse)
    )
    .orderBy(SUBWAY_STATIONS.COORDINATES.knnOrderTo(lat, lon))
    .limit(1)
```

**Рекомендация:** 
- Если метод вызывается часто, рассмотреть кэширование результатов по `cityId`
- Убедиться, что есть индекс на `subway_lines.city_id` (должен быть, так как это FK)
- Убедиться, что есть GIST индекс на `subway_stations.coordinates` (должен быть для KNN поиска)

**Статус:** Требует мониторинга производительности в production. Если станет узким местом, можно оптимизировать.

---

### 2. `SubwayRepositoryImpl.batchUpdateStationsForCity()` - Массовое обновление с подзапросом
**Метод:** `batchUpdateStationsForCity(cityId: UUID, forceUpdate: Boolean)`

**Проблема:** Выполняет массовое обновление всех туалетов в городе с коррелированным подзапросом для каждой строки. При большом количестве туалетов в городе (1000+) это может быть медленно.

**Текущая реализация:**
```kotlin
val subquery = ctx
    .select(SUBWAY_STATIONS.ID)
    .from(SUBWAY_STATIONS)
    .join(SUBWAY_LINES)
    .on(SUBWAY_STATIONS.SUBWAY_LINE_ID.eq(SUBWAY_LINES.ID))
    .where(
        SUBWAY_LINES.CITY_ID.eq(restroomsCityIdField)  // Коррелированный подзапрос
            .and(SUBWAY_STATIONS.IS_DELETED.isFalse)
            .and(SUBWAY_LINES.IS_DELETED.isFalse)
    )
    .orderBy(...)
    .limit(1)
    .asField<UUID>("nearest_id")

ctx.update(RESTROOMS)
    .set(RESTROOMS.SUBWAY_STATION_ID, subquery)
    .set(RESTROOMS.UPDATED_AT, Instant.now())
    .where(condition)
    .execute()
```

**Рекомендация:**
- Если метод вызывается часто или для городов с большим количеством туалетов, рассмотреть:
  1. Пакетную обработку (например, по 100 записей за раз)
  2. Использование CTE (WITH) для предварительного вычисления ближайших станций
  3. Добавление индекса на `restrooms.city_id` (уже есть: `idx_restrooms_city_id`)
  4. Мониторинг времени выполнения в production

**Статус:** Требует мониторинга. Оптимизация может потребоваться при масштабировании.

---

### 3. `RestroomRepositoryImpl.findNearestByLocation()` - JOIN с несколькими таблицами
**Метод:** `findNearestByLocation(latitude: Double, longitude: Double, limit: Int?, distanceMeters: Int?)`

**Проблема:** Выполняет LEFT JOIN с тремя таблицами (`buildings`, `subway_stations`, `subway_lines`) и выбирает много полей. При большом количестве данных это может быть медленно.

**Текущая реализация:**
```kotlin
ctx.select(*selectFields.toTypedArray())  // ~30 полей
    .from(RESTROOMS)
    .leftJoin(b).on(RESTROOMS.BUILDING_ID.eq(b.ID))
    .leftJoin(s).on(RESTROOMS.SUBWAY_STATION_ID.eq(s.ID))
    .leftJoin(l).on(s.SUBWAY_LINE_ID.eq(l.ID))
    .where(...)
    .orderBy(knnField.asc())
    .limit(limit ?: 5)
```

**Рекомендация:**
- Убедиться, что есть индексы на FK:
  - `idx_restrooms_building_id` (уже есть)
  - Индекс на `subway_stations.subway_line_id` (должен быть, так как это FK)
- Рассмотреть использование `SELECT *` только для необходимых полей (уже реализовано через проекции)
- KNN поиск использует GIST индекс на `coordinates` (уже есть: `idx_restrooms_coordinates`)

**Статус:** Оптимально для текущего использования. Требует мониторинга при росте данных.

---

### 4. `CityRepositoryImpl.findByName()` - Поиск по LIKE без индекса
**Метод:** `findByName(name: String, pagination: PaginationRequest)`

**Проблема:** Использует `LIKE` с `ILIKE` для поиска по `name_ru` и `name_en`, что не может использовать обычные B-Tree индексы эффективно.

**Текущая реализация:**
```kotlin
val condition = DSL.or(
    CITIES.NAME_RU.likeIgnoreCase(searchPattern).escape('\\'),
    CITIES.NAME_EN.likeIgnoreCase(searchPattern).escape('\\')
).and(CITIES.IS_DELETED.isFalse)
```

**Рекомендация:**
- Для полнотекстового поиска рассмотреть использование PostgreSQL Full-Text Search (FTS) с индексами `GIN` на `tsvector`
- Или использовать расширение `pg_trgm` для триграммного поиска (более гибкий, чем FTS)
- Альтернатива: использовать внешний поисковый движок (Elasticsearch, Typesense) для сложных поисковых запросов

**Статус:** Приемлемо для небольшого количества городов (< 1000). Требует оптимизации при масштабировании.

---

## ✅ Проверенные индексы

### Существующие индексы (из схемы БД):
- ✅ `idx_restrooms_coordinates` - GIST на `restrooms.coordinates`
- ✅ `idx_restrooms_external_maps` - GIN на `restrooms.external_maps`
- ✅ `idx_restrooms_filters` - B-Tree на `(fee_type, accessibility_type, place_type) WHERE is_deleted = false`
- ✅ `idx_restrooms_building_id` - B-Tree на `restrooms.building_id`
- ✅ `idx_restrooms_status` - B-Tree на `restrooms.status WHERE is_deleted = false`
- ✅ `idx_restrooms_city_id` - B-Tree на `restrooms.city_id WHERE is_deleted = false`
- ✅ `idx_buildings_external_ids` - GIN на `buildings.external_ids`
- ✅ `idx_cities_bounds` - GIST на `cities.city_bounds`
- ✅ `idx_cities_coordinates` - GIST на `cities.coordinates`

### Рекомендуемые дополнительные индексы (если еще нет):
- Проверить наличие индекса на `subway_stations.subway_line_id` (FK, должен быть автоматически)
- Проверить наличие индекса на `subway_stations.coordinates` (GIST для KNN поиска)
- Рассмотреть составной индекс на `restrooms(city_id, status, is_deleted)` для часто используемых запросов

---

## 📊 Метрики для мониторинга

Рекомендуется отслеживать следующие метрики в production:

1. **Время выполнения запросов:**
   - `findByExternalMap()` / `findByExternalId()` - должно быть < 10ms даже при 100k+ записей
   - `findNearestByLocation()` - должно быть < 50ms при 10k+ записей
   - `batchUpdateStationsForCity()` - время зависит от количества туалетов в городе

2. **Использование индексов:**
   - Проверить через `EXPLAIN ANALYZE`, что запросы используют индексы
   - Убедиться, что нет `Seq Scan` на больших таблицах

3. **Размер таблиц:**
   - Мониторить рост таблиц `restrooms`, `buildings`, `cities`
   - При достижении 100k+ записей пересмотреть стратегию индексирования

---

## 🎯 Итоговые рекомендации

1. ✅ **Исправлено:** Все лишние проверки `isNull` удалены
2. ✅ **Исправлено:** Оптимизирован поиск в JSONB через оператор `@>`
3. ⚠️ **Требует мониторинга:** Массовые операции (`batchUpdateStationsForCity`)
4. ⚠️ **Требует оптимизации при масштабировании:** Поиск по имени города (`findByName`)

**Общий вывод:** Код оптимизирован для текущих объемов данных. При росте данных (10k+ записей) потребуется дополнительная оптимизация массовых операций и полнотекстового поиска.
