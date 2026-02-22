
# Архитектура импорта данных

## Обзор

Текущая реализация импорта позволяет загружать данные о туалетах из внешних источников (провайдеров) через REST API. Поддерживается формат 2ГИС scraped JSON.

---

## Текущая архитектура

### Слои

```
HTTP Request (ImportController)
    → ImportService
        → ImportStrategy (по provider)
            → PayloadExtractor → Parser → Normalizer → RestroomCandidateMapper
            → RestroomRepository / RestroomImportRepository
```

### Компоненты

| Компонент | Назначение |
|-----------|------------|
| **ImportController** | REST endpoints: `POST /api/v1/import` (одиночный), `POST /api/v1/import/batch` (пакетный) |
| **ImportService** | Координирует импорт, выбирает стратегию по provider, создаёт записи в restroom_imports |
| **ImportStrategy** | Интерфейс стратегии; реализация — TwoGisScrapedImportStrategy |
| **PayloadExtractor** | Извлекает items из payload: `{"items": [...]}` или одиночный объект |
| **Parser** | JsonObject → TwoGisScrapedPlace (и другие DTO провайдеров) |
| **Normalizer** | TwoGisScrapedPlace → NormalizedRestroomCandidate (каноническая модель) |
| **RestroomCandidateMapper** | NormalizedRestroomCandidate → RestroomCreateDto |
| **ScheduleMappingService** | Конвертирует расписание провайдера в канонический Schedule (используется в RestroomRepositoryImpl для isOpen, **не используется в импорте**) |

### Поток данных (2ГИС)

1. **Request**: `{ provider: "TWO_GIS", payloadType: "TWO_GIS_SCRAPED_PLACE_JSON", cityId: "uuid", payload: {...} }`
2. **Extract**: ArrayOrSingleExtractor выдаёт `List<JsonObject>` из `payload["items"]` или `[payload]`
3. **Parse**: TwoGisScrapedParser → `TwoGisScrapedPlace(id, title, category, address, location, working_hours, attributeGroups, rubrics)`
4. **Normalize**: TwoGisScrapedNormalizer → `NormalizedRestroomCandidate` (placeType, feeType, status, amenities, rawSchedule)
5. **Map**: RestroomCandidateMapper → `RestroomCreateDto` (workTime = rawSchedule в формате провайдера)
6. **Upsert**: findByOrigin(originProvider, originId) — при совпадении update, иначе save
7. **Subway**: setNearestStationForRestroom для каждого туалета
8. **Import record**: createPending → markSuccess/markFailed в restroom_imports

---

## Что уже есть

### Endpoints

- `POST /api/v1/import` — импорт одного объекта или `{"items": [obj]}`
- `POST /api/v1/import/batch` — пакетный импорт (тот же payload, внутри — массив items)

### Валидация

- Проверка payload на пустоту
- Проверка payloadType (сейчас только TWO_GIS_SCRAPED_PLACE_JSON)
- Проверка обязательных полей: `id`, `title`, `location` с `lat`, `lng` (address опционален — при null используется "")

### Модели

- ImportRequestDto, ImportResponseDto, ImportBatchResponseDto
- ImportPayloadType: TWO_GIS_SCRAPED_PLACE_JSON, YANDEX_MAPS_SCRAPED_PLACE_JSON, MANUAL
- ImportProvider: TWO_GIS, YANDEX_MAPS, GOOGLE_MAPS, OSM
- NormalizedRestroomCandidate (каноническая модель)
- TwoGisScrapedPlace, TwoGisScrapedLocation

### Бизнес-логика

- Upsert по `origin_provider` + `origin_id`
- Маппинг категорий 2ГИС → PlaceType
- Определение feeType (FREE/PAID) по атрибутам
- Определение status (ACTIVE/INACTIVE) по "доступ ограничен"
- Построение amenities (payment_methods, accessible_entrance, ramp, lift и т.д.)
- Привязка к ближайшей станции метро

### Аудит

- Таблица `restroom_imports` (ID, provider, payload_type, city_id, raw_payload, status, building_id, restroom_id, error_message, processed_at)

---

## Проверка формата (туалеты_минск_2гис_1тысзаписей.json)

### Формат файла

Файл представляет собой **корневой JSON-массив** `[{...}, {...}, ...]`. API ожидает payload как **объект** с полем `items`:

```json
{
  "provider": "TWO_GIS",
  "payloadType": "TWO_GIS_SCRAPED_PLACE_JSON",
  "cityId": "<uuid>",
  "payload": {
    "items": [<содержимое файла>]
  }
}
```

**Подготовка запроса:** `jq '{provider: "TWO_GIS", payloadType: "TWO_GIS_SCRAPED_PLACE_JSON", cityId: "<UUID>", payload: {items: .}}' туалеты_минск_2гис_1тысзаписей.json`

### Совместимость парсера

| Поле в JSON          | Обработка | Примечание |
|----------------------|-----------|------------|
| `id`, `title`        | Обязательны | |
| `address`            | Опционально | При `null` → `""` |
| `location` {lat, lng}| Обязательно | |
| `working_hours`      | Опционально | При `null` → `null`, сохраняется raw JSON |
| `attributeGroups`    | Массив строк | Нестроковые элементы пропускаются |
| `rubrics`            | Массив строк | Нестроковые элементы пропускаются |

### Найденные в файле особенности

- **~80+ записей** с `address: null` — обрабатываются, используется пустая строка
- **Записи** с `working_hours: null` — обрабатываются, workTime = null
- **Формат working_hours** — `{ "Mon": { "from": "10:00", "to": "22:00" }, ... }` (отличается от TwoGisScheduleAdapter, но сохраняется как есть)
- **district: null** — допустимо, не используется в импорте

---

## Проблемы и недоработки

### 1. ScheduleMappingService не используется при импорте

`TwoGisScrapedNormalizer` передаёт `rawSchedule` (JsonObject от 2ГИС) напрямую в `RestroomCreateDto.workTime`. В БД сохраняется сырой JSON 2ГИС.  
`RestroomRepositoryImpl.computeIsOpen()` использует ScheduleMappingService для конвертации — там всё ок.  
Импорт в MVP это не блокирует, но формат work_time в БД остаётся провайдероспецифичным.

### 2. Batch-обработка ошибок

В `ImportService.importBatch()` при ошибке на любом элементе весь batch помечается как failed и исключение пробрасывается. Нет:
- частичного успеха (часть items импортирована, часть — нет);
- детальных ошибок по каждому item.

В TwoGisScrapedImportStrategy при ошибке на любом item исключение пробрасывается, транзакция откатывается.

### 3. RestroomRepositoryImpl без ScheduleMappingService в стратегии

`TwoGisScrapedImportStrategy` создаёт `RestroomRepositoryImpl(txCtx)` без `ScheduleMappingService`. Для импорта это не критично (workTime итак сохраняется как JsonObject), но создаётся расхождение в конфигурации репозитория.

### 4. Отсутствие в OpenAPI

Эндпоинты `/import` и `/import/batch` не описаны в `openapi.yaml`.

### 5. Отсутствие тестов

Нет unit- и integration-тестов для ImportController и ImportService.

### 6. provider / payloadType не связаны

Можно отправить `provider: YANDEX_MAPS` с `payloadType: TWO_GIS_SCRAPED_PLACE_JSON` — стратегия по provider не найдётся (Yandex не реализован), но валидация payloadType пройдёт.

### 7. cityId не проверяется

Проверки существования city в БД нет. При невалидном cityId будет ошибка на уровне БД.

---

## MVP: что минимально нужно для POST-импорта

Текущая реализация уже покрывает MVP:

1. **POST /api/v1/import** — уже есть и работает
2. **Поддержка 2ГИС scraped** — реализована
3. **Upsert** — реализован
4. **Аудит в restroom_imports** — реализован

### Рекомендуемые минимальные доработки для MVP

| Задача | Приоритет | Описание |
|--------|-----------|----------|
| Добавить импорт в OpenAPI | Средний | Описать `/import` и `/import/batch` в openapi.yaml |
| Валидация cityId | Средний | Проверить существование city до импорта |
| Связка provider + payloadType | Низкий | Явно ограничить допустимые комбинации (например, TWO_GIS только с TWO_GIS_SCRAPED_PLACE_JSON) |
| Unit-тест ImportController | Низкий | Мок ImportService, проверка вызовов и формата ответа |

### Что можно отложить

- Частичная обработка batch при ошибках
- Другие провайдеры (Yandex, Google, OSM)
- Конвертация расписания при импорте (можно оставить raw JSON 2ГИС)

---

## Диаграмма потока (MVP)

```
POST /api/v1/import
    ↓
ImportController.validatePayload()
    ↓ (payloadType, items/object structure)
ImportService.import()
    ↓
RestroomImportRepository.createPending()
    ↓
TwoGisScrapedImportStrategy.importObject/importBatch()
    ↓
Extract → Parse → Normalize → Map
    ↓
transactionSuspend {
    for each item:
        findByOrigin → update or save
        setNearestStationForRestroom
}
    ↓
RestroomImportRepository.markSuccess()
    ↓
ImportResponseDto(importId, restroomId, buildingId, status)
```

---

## Структура файлов

```
controller/
  ImportController.kt
service/import/
  ImportService.kt
  ImportStrategy.kt
  Parser.kt
  Normalizer.kt
  PayloadExtractor.kt          (ArrayOrSingleExtractor)
  RestroomCandidateMapper.kt
  twogis/
    TwoGisScrapedImportStrategy.kt
    TwoGisScrapedParser.kt
    TwoGisScrapedNormalizer.kt
  schedule/
    ScheduleAdapter.kt
    ScheduleMappingService.kt
    TwoGisScheduleAdapter.kt
model/import/
  ImportRequestDto.kt
  ImportResponseDto.kt
  ImportBatchResponseDto.kt
  ImportPayloadType.kt
  ImportJobStatus.kt
  NormalizedRestroomCandidate.kt
  twogis/
    TwoGisScrapedModels.kt
```
