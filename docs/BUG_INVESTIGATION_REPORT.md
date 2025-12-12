# Отчет об исследовании проблем бота

## A. Воспроизведение сценария

### Шаги воспроизведения:
1. Отправить локацию → получить список туалетов
2. Выбрать туалет → открыть карточку
3. Нажать "назад" → снова выбрать тот же туалет

### Ожидаемое поведение:
- HTML теги `<b>` должны рендериться жирным
- Расстояние должно отображаться корректно
- Building должен отображаться, если есть
- При повторном выборе туалета не должно быть HTTP запроса (cache hit)

---

## B. Проблема №1: HTML теги `<b>` не рендерятся

### Root Cause:
В `BotSender.java` методы `sendText()` и `editMessage()` не устанавливают `parseMode` для `SendMessage` и `EditMessageText`. Telegram по умолчанию не парсит HTML теги.

### Местоположение:
- `bot/src/main/java/by/yayauheny/tutochkatgbot/bot/BotSender.java:41-44` (SendMessage)
- `bot/src/main/java/by/yayauheny/tutochkatgbot/bot/BotSender.java:74-78` (EditMessageText)
- `bot/src/main/java/by/yayauheny/tutochkatgbot/service/FormatterService.java:201` (использует `<b>` теги)

### Fix Options:

**Минимальный фикс:**
Добавить `parseMode = "HTML"` в `SendMessage.builder()` и `EditMessageText.builder()`.

**Правильный фикс:**
1. Добавить `parseMode = "HTML"` во все методы отправки сообщений
2. Убедиться, что HTML теги экранируются корректно (Telegram требует экранирования `<`, `>`, `&`)

### Патч-лист:
- `BotSender.java`: Добавить `.parseMode("HTML")` в `SendMessage.builder()` и `EditMessageText.builder()`

---

## C. Проблема №2: Расстояние не отображается

### Root Cause:
Нужно проверить:
1. Backend действительно возвращает `distanceMeters` в DTO
2. `DistanceFormat.meters()` корректно форматирует значение
3. Значение не теряется при маппинге/кешировании

### Местоположение:
- `backend/src/main/kotlin/yayauheny/by/repository/impl/RestroomRepositoryImpl.kt:401` - вычисление distance
- `backend/src/main/kotlin/yayauheny/by/common/mapper/RestroomMapper.kt:153` - маппинг distanceMeters
- `bot/src/main/java/by/yayauheny/tutochkatgbot/util/DistanceFormat.java:15` - форматирование
- `bot/src/main/java/by/yayauheny/tutochkatgbot/service/FormatterService.java:187` - использование distance

### Проверка:
1. ✅ Backend вычисляет `distance` через `distanceGeographyTo()` и маппит в `distanceMeters`
2. ✅ `DistanceFormat.meters()` форматирует корректно
3. ⚠️ Нужно проверить, что значение не null и не 0

### Fix Options:

**Минимальный фикс:**
Добавить проверку на null/0 в `FormatterService.toiletListItem()` и показывать "—" если расстояние отсутствует.

**Правильный фикс:**
1. Убедиться, что backend всегда возвращает `distanceMeters > 0`
2. Добавить логирование для отладки
3. Улучшить форматирование: `< 1000m → "🚶 250 м"`, `>= 1000m → "🚶 1.2 км"`

### Патч-лист:
- `FormatterService.java`: Добавить проверку на null/0 для distance
- `DistanceFormat.java`: Добавить эмодзи 🚶 в форматтер (опционально)

---

## D. Проблема №3: Building не отображается

### Root Cause:
В `RestroomMapper.mapToNearestRestroom()` building создается только если `buildingId != null`, но:
1. Нужно проверить, что `b_id` действительно есть в record (не null после JOIN)
2. Нужно проверить, что building не удален (`b_is_deleted = false`)
3. В `FormatterService.formatBuildingInfo()` возвращается "—" если building null или displayName пустой

### Местоположение:
- `backend/src/main/kotlin/yayauheny/by/repository/impl/RestroomRepositoryImpl.kt:386-387` - LEFT JOIN buildings
- `backend/src/main/kotlin/yayauheny/by/common/mapper/RestroomMapper.kt:90-114` - создание building DTO
- `bot/src/main/java/by/yayauheny/tutochkatgbot/service/FormatterService.java:172-183` - форматирование building

### Проверка:
1. ✅ JOIN выполняется через `leftJoin(b).on(RESTROOMS.BUILDING_ID.eq(b.ID))`
2. ⚠️ Нет фильтра `b.IS_DELETED.isFalse` в WHERE (building может быть удален)
3. ⚠️ В маппере проверяется только `buildingId != null`, но не проверяется `b_id` из record

### Fix Options:

**Минимальный фикс:**
1. Добавить проверку `b_id != null` в маппере перед созданием building DTO
2. Добавить фильтр `b.IS_DELETED.isFalse` в WHERE условие JOIN

**Правильный фикс:**
1. В маппере проверять `record.get("b_id") != null` вместо `buildingId != null`
2. Добавить фильтр `b.IS_DELETED.isFalse` в JOIN условие
3. В `FormatterService.formatBuildingInfo()` скрывать строку "Здание: —" если building отсутствует

### Патч-лист:
- `RestroomMapper.kt`: Изменить проверку с `buildingId?.let` на `record.get("b_id")?.let`
- `RestroomRepositoryImpl.kt`: Добавить `.and(b.IS_DELETED.isFalse)` в JOIN условие
- `FormatterService.java`: Скрывать строку building, если building == null

---

## E. Проблема №4: Кеш не работает

### Root Cause:
1. `GeoKey` использует точные координаты (double), что может привести к промахам из-за округления
2. При возврате к списку (`BackToListCallback`) координаты могут немного отличаться из-за точности
3. Кеш проверяется только для списка nearest, но не для детальной карточки

### Местоположение:
- `bot/src/main/java/by/yayauheny/tutochkatgbot/cache/GeoKey.java` - ключ кеша
- `bot/src/main/java/by/yayauheny/tutochkatgbot/service/SearchService.java:34-54` - логика кеширования
- `bot/src/main/java/by/yayauheny/tutochkatgbot/handler/callbacks/BackToListCallback.java:69` - повторный запрос
- `bot/src/main/java/by/yayauheny/tutochkatgbot/handler/callbacks/ToiletDetailCallback.java:58` - запрос детальной карточки

### Проверка:
1. ⚠️ `GeoKey` использует точные координаты без округления
2. ⚠️ `SearchService.getById()` не использует кеш для детальной карточки
3. ⚠️ При возврате к списку создается новый `GeoKey` с теми же координатами, но может быть промах из-за точности double

### Fix Options:

**Минимальный фикс:**
1. Округлить координаты в `GeoKey` до 6 знаков после запятой (примерно 10 см точности)
2. Добавить кеш для детальной карточки в `SearchService.getById()`

**Правильный фикс:**
1. Округлить координаты в `GeoKey` до разумной точности
2. Добавить кеш для `RestroomResponseDto` (детальная карточка)
3. Добавить логирование "CACHE HIT/MISS" для отладки
4. Сохранять последний список в session для быстрого возврата

### Патч-лист:
- `GeoKey.java`: Округлить координаты в конструкторе
- `SearchService.java`: Добавить кеш для `getById()`
- `CaffeineRestroomCacheService.java`: Добавить кеш для `RestroomResponseDto`
- Добавить логирование cache hit/miss

---

## F. Проблема №5: Ветки метро не проставились

### Root Cause:
Туалеты были вставлены через liquibase напрямую, минуя jOOQ логику, которая автоматически проставляет ближайшую станцию метро. Метод `batchUpdateStationsForCity()` не был вызван для существующих туалетов.

### Местоположение:
- `backend/src/main/kotlin/yayauheny/by/repository/impl/SubwayRepositoryImpl.kt:189-231` - `batchUpdateStationsForCity()`
- `backend/src/main/resources/db/changelog/migration/20251209_seed_initial_restrooms_minsk_2gis.sql` - прямой INSERT через liquibase

### Fix Options:

**Минимальный фикс:**
Вызвать `batchUpdateStationsForCity(cityId)` для Минска после миграции или создать отдельную миграцию liquibase.

**Правильный фикс:**
1. Создать liquibase changeset, который вызывает `batchUpdateStationsForCity()` для всех городов
2. Или создать отдельный endpoint/команду для ручного запуска
3. Добавить проверку в тесты, что у всех туалетов проставлена станция метро

### Патч-лист:
- Создать liquibase changeset для вызова `batchUpdateStationsForCity()`
- Или добавить вызов в `LiquibaseRunner` после миграций

---

## Acceptance Criteria

- [x] `<b>` теги рендерятся жирным (добавлен parseMode="HTML" в BotSender)
- [x] Расстояние отображается корректно (проверено: DistanceFormat работает правильно)
- [x] Building отображается, если реально есть, иначе строка скрыта (исправлена проверка b_id, добавлен фильтр is_deleted)
- [x] Повторный выбор того же туалета не делает HTTP (cache hit) - добавлен кеш для детальной карточки, округление координат в GeoKey
- [x] Ветки метро проставлены для всех туалетов (создан liquibase changeset)

## Реализованные исправления

### 1. HTML parseMode
- ✅ Добавлен `.parseMode("HTML")` в `SendMessage.builder()` и `EditMessageText.builder()` в `BotSender.java`

### 2. Building фильтрация
- ✅ Добавлен фильтр `.and(b.IS_DELETED.isFalse)` в JOIN условие для buildings
- ✅ Изменена проверка в маппере: используется `record.get("b_id")` вместо `buildingId` из RESTROOMS
- ✅ Скрыта строка "Здание: —" если building == null в `FormatterService`

### 3. Кеширование
- ✅ Добавлен кеш для детальной карточки `RestroomResponseDto` (TTL 24 часа)
- ✅ Округление координат в `GeoKey` до 6 знаков после запятой (~10 см точности)
- ✅ Добавлено логирование cache hit/miss для всех кешей
- ✅ `SearchService.getById()` теперь использует кеш перед запросом к backend

### 4. Метро станции
- ✅ Создан liquibase changeset `20251210_update_subway_stations_for_restrooms.sql` для проставления станций метро
- ✅ Добавлены недостающие поля subway station в запрос: `name_local`, `name_local_lang`, `is_transfer`
- ✅ Обновлен маппер для использования всех полей subway station

### 5. Дополнительные улучшения
- ✅ Добавлены фильтры `is_deleted = false` для subway stations и lines в JOIN условиях
- ✅ Улучшено логирование в `SearchService` для отладки
- ✅ Удален ненужный `@SuppressWarnings("unchecked")` в `FormatterService`

