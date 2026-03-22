# Аудит архитектуры импорта данных (туалеты)

**Вход:** `importProvider` (enum из заголовка), `data` (JSON body с `items`).  
**Цель аудита:** карта архитектуры, потоки данных, проблемы/риски, улучшения, план рефакторинга, тестовая стратегия.

### Провайдеры и форматы (на момент аудита)

| Провайдер | PayloadType | Реализация | Статус |
|-----------|-------------|------------|--------|
| TWO_GIS | TWO_GIS_SCRAPED_PLACE_JSON | TwoGisScrapedImportStrategy | Реализован |
| YANDEX_MAPS | YANDEX_MAPS_SCRAPED_PLACE_JSON | — | Нет реализации |
| GOOGLE_MAPS | — | — | Нет |
| OSM | — | — | Нет |
| USER, MANUAL | MANUAL | — | Не для HTTP-импорта |

Тело запроса: `{ "items": [ { ... } ] }`. Для одиночного импорта — один объект в массиве; для batch — несколько. Заголовки: `Import-Provider`, `Import-Payload-Type`, `Import-City-Id` (опционально).

---

## 1. Текущая архитектура и потоки данных

### 1.1 Слои и ответственности

| Слой | Класс/файл | Ответственность |
|------|------------|-----------------|
| **HTTP** | `ImportController` | Роуты `/import`, `/import/batch`; чтение заголовков (`getImportHeaders`); парсинг body → `ImportRequestDto`; валидация Konform (`ImportItemsParams`); разрешение `cityId` (header или первый item `city`); вызов `ImportService.import` / `importBatch`; ответ DTO. |
| **Оркестрация** | `ImportService` | Выбор стратегии по `provider` (Map); создание записи в `restroom_imports` (PENDING); вызов `strategy.importObject` / `importBatch`; `markSuccess` / `markFailed`; возврат `ImportExecutionResult` / `ImportBatchResult`. |
| **Стратегия** | `ImportStrategy` (интерфейс), `TwoGisScrapedImportStrategy` | Один провайдер = одна реализация. Внутри: extract → parse → normalize → resolve building → map → upsert + subway; транзакция на весь batch. |
| **Извлечение** | `PayloadExtractor` → `ArrayOrSingleExtractor` | Из payload: `items` array или сам объект как единственный item. |
| **Парсинг** | `Parser<T>` → `TwoGisScrapedParser` | `JsonObject` → `TwoGisScrapedPlace` (id, title, category, address, location, workingHours, attributeGroups, rubrics). Бросает `IllegalArgumentException` при отсутствии полей. |
| **Нормализация** | `Normalizer<T>` → `TwoGisScrapedNormalizer` | Provider DTO → `NormalizedRestroomCandidate` (locationType из Category/Rubric, buildingContext при INSIDE_BUILDING, feeType, amenities, status). |
| **Маппинг** | `RestroomCandidateMapper` | `NormalizedRestroomCandidate` → `RestroomCreateDto` (опционально `buildingId`, `inheritBuildingSchedule`; при наследовании расписания `workTime = null`). |
| **Персистенция** | `RestroomRepository`, `BuildingRepository`, `RestroomImportRepository` | Сохранение/обновление restroom, создание/поиск building по external_id, запись в `restroom_imports`. |

### 1.2 Основные сущности и роли

| Сущность | Роль |
|----------|------|
| `ImportHeaders` | provider (enum), payloadType (enum), cityId (UUID?). |
| `ImportRequestDto` | Только `items: List<JsonObject>`. |
| `ImportExecutionResult` | importId, restroomId, buildingId, status. |
| `ImportBatchResult` | importId, totalProcessed, successful, failed, results: List<ImportItemResult>. |
| `ImportObjectResult` | restroomId, buildingId (результат одного объекта из стратегии). |
| `NormalizedRestroomCandidate` | Каноническая модель: provider, providerObjectId, cityId, name, address, lat/lng, placeType, locationType, feeType, status, amenities, rawSchedule, buildingContext?. |
| `BuildingContext` | name, address, workTime, externalId — для создания/поиска здания при INSIDE_BUILDING. |

### 1.3 Схема потока: request → выбор парсера → parse → map → validate → persist → report

```
HTTP POST /import или /import/batch
  Body: { "items": [ {...}, ... ] }
  Headers: Import-Provider, Import-Payload-Type, Import-City-Id?
    ↓
ImportController
  getImportHeaders() → ImportProvider.valueOf(header), ImportPayloadType.valueOf(header), cityId?
  parseImportBody() → ImportRequestDto(items)
  validateImportItemsParams: items не пусто, для /import ровно 1 item
  resolveCityId(headers, items): header.cityId ?: первый item["city"] → CityRepository.findByName
    ↓
ImportService.import(provider, payloadType, cityId, payload)
  strategiesByProvider[provider] ?: error("Unsupported import provider")
  restroomImportRepository.createPending(...) → importId
  strategy.importObject(cityId, payloadType, payload)  [или importBatch с payload = { items }]
    ↓
TwoGisScrapedImportStrategy
  if payloadType != TWO_GIS_SCRAPED_PLACE_JSON → throw
  extractor.extractItems(payload) → List<JsonObject>
  transactionSuspend {
    for each item:
      parser.parse(item) → TwoGisScrapedPlace
      normalizer.normalize(cityId, place, payloadType) → NormalizedRestroomCandidate
      resolveBuildingAndCreateDto(candidate, txBuildingRepo) → (RestroomCreateDto, buildingId?)
      txRestroomRepo.findByOrigin(originProvider, originId) → existing?
      if existing → update; else → save
      txSubwayRepo.setNearestStationForRestroom(restroomId, lat, lon)
      → ImportObjectResult(restroomId, buildingId)
  }
  (при любом throw — транзакция откатывается, исключение пробрасывается)
    ↓
ImportService: restroomImportRepository.markSuccess(importId, buildingId, restroomId)
  return ImportExecutionResult(importId, restroomId, buildingId, SUCCESS)
    ↓
ImportController: call.respond(ImportResponseDto(...))
```

**Валидация:** до входа в стратегию — только размер items и формат body. Валидация полей payload (id, location и т.д.) выполняется внутри парсера/нормализатора и приводит к исключениям.

---

## 2. Выбор стратегии парсинга

| Вопрос | Как сейчас |
|--------|------------|
| **По чему выбирается?** | По `importProvider` из заголовка (enum). |
| **Где хранится маппинг?** | В `ImportService`: `strategiesByProvider = strategies.associateBy { it.provider() }`; список стратегий приходит из DI (`List<ImportStrategy>`), в модуле зарегистрирована одна — `TwoGisScrapedImportStrategy`. |
| **Неизвестный provider?** | `strategiesByProvider[provider] ?: error("Unsupported import provider: $provider")` — исключение, запрос падает с 500 (если не перехвачено). |
| **Несовместимая версия/схема?** | Нет. `payloadType` проверяется только внутри стратегии (2ГИС: `if (payloadType != TWO_GIS_SCRAPED_PLACE_JSON) throw`). Связка provider ↔ payloadType не валидируется на входе (можно отправить YANDEX_MAPS + TWO_GIS_SCRAPED_PLACE_JSON — упадёт по отсутствию стратегии для YANDEX). |
| **Авто-детект (sniff)?** | Нет. |

---

## 3. Проблемы и риски (приоритеты)

### P0 (критично)

- **Batch: один failed item валит весь импорт.** В `TwoGisScrapedImportStrategy.importBatch` при любом `throw` в цикле откатывается вся транзакция, в `ImportService` вызывается `markFailed`, исключение пробрасывается. Partial import невозможен.
- **Нет явного контракта ParseResult/ImportReport.** Результат — либо успех с ID, либо исключение. Нет типизированных ошибок по элементу (parse error vs validation vs DB).

### P1 (важно)

- **Ошибки выбора провайдера:** `error("Unsupported...")` и `IllegalArgumentException` в парсере не маппятся в 4xx; клиент получает 500.
- **provider и payloadType не связаны:** не проверяется, что для данного provider допустим переданный payloadType (и наоборот).
- **cityId не проверяется на существование** до импорта (при неверном UUID ошибка на уровне БД).
- **В batch в отчёт всегда successful = size, failed = 0** — при успехе; при падении весь batch failed, но `ImportBatchResult` с failed > 0 по текущему коду не формируется (сначала throw).
- **Логирование:** нет correlationId, нет времени выполнения импорта, нет размера payload в логах оркестратора.

### P2 (желательно)

- **Регистрация стратегий:** добавление провайдера требует правки Koin-модуля (новый bean + добавить в список). Реестр по сути есть (Map по provider), но явного Registry/Factory нет.
- **Парсер бросает IllegalArgumentException** с текстом — хорошо для логов, но нет кода ошибки/типа для машинной обработки.
- **Магические строки:** в парсере ключи JSON (`"id"`, `"title"`, `"location"` и т.д.) захардкожены; в нормализаторе строки атрибутов частично вынесены в TwoGisAttributeGroup, но не везде.
- **RestroomRepositoryImpl** в стратегии создаётся без `ScheduleMappingService`; для импорта это не ломает (workTime сохраняется как есть), но конфигурация отличается от основного репозитория.
- **Нет dryRun / "только валидация".**
- **Дубликаты:** дедуп только по (originProvider, originId). Нет политики по geo/имени при отсутствии externalId.

### Скрытые риски

- **NPE:** `createDto.originId!!` в стратегии — при корректном маппере не null, но при изменении контракта возможен NPE.
- **valueOf:** в `getImportHeaders()` используется `ImportProvider.valueOf(providerRaw)` и `ImportPayloadType.valueOf(payloadTypeRaw)` через `runCatching` → при неверном значении бросается ValidationException — ок.
- **Потеря данных:** при throw в середине batch все изменения в транзакции откатываются — данные не теряются, но и частичного результата нет.
- **Silent fail:** нет; ошибки пробрасываются наверх.

---

## 4. Улучшенная архитектура

### 4.1 Минимально-инвазивный вариант

- **Выбор стратегии:** оставить `strategiesByProvider`; при неизвестном provider возвращать 400 с телом `{ "code": "UNSUPPORTED_PROVIDER", "provider": "..." }` вместо 500.
- **Связка provider–payloadType:** в контроллере или в сервисе проверять допустимые пары (например, TWO_GIS → TWO_GIS_SCRAPED_PLACE_JSON) и при несовпадении 400.
- **Проверка cityId:** перед импортом вызывать `CityRepository.findById(cityId)` (или exists); при отсутствии — 400.
- **Partial batch:** в стратегии обрабатывать items в цикле с try/catch на элемент; накапливать `ImportItemResult` (success + restroomId/buildingId или failure + errorMessage); не бросать из цикла при ошибке одного элемента; коммитить транзакцию по батчу или по одному элементу (решение по консистенции). В `ImportService.importBatch` строить `ImportBatchResult` из реальных successful/failed и списка results.
- **Типы результатов:** ввести `ParseResult<T>` (success T | parseError message/code) и внутри стратегии по желанию — `ImportItemResult` с полем errorMessage для failed items.

### 4.2 Вариант «идеальный» (по мере необходимости)

- **Strategy + Registry:** явный `ImportStrategyRegistry` (interface): `getStrategy(provider): ImportStrategy?`, реализация — по Map из DI. Добавление провайдера = новая стратегия + регистрация в модуле без правок оркестратора.
- **Контракты:**  
  - `ParseResult<out T> = Success(T) | ParseError(code, message)`  
  - `ImportItemReport(index, restroomId?, buildingId?, success, errorCode?, errorMessage?)`  
  - `ImportReport(importId, provider, payloadType, total, successful, failed, items: List<ImportItemReport>, durationMs?)`
- **Pipeline/Template method:** абстрактный класс или функция `runImportPipeline(provider, payloadType, cityId, payload): ImportReport` с шагами: validateRequest → createPending → extractItems → forEachItem(parse → normalize → validateItem → resolveBuilding → map → persistItem) → markCompleted(Report). Стратегия по-прежнему даёт parse/normalize и persist, оркестратор собирает отчёт.
- **Обработка ошибок:** в контроллере единый блок: ValidationException → 400, UnsupportedProviderException → 400, NotFoundException (city) → 404, остальное → 500 + лог; в теле ответа код и описание.
- **Пакеты:** оставить `controller`, `service.import`, `service.import.twogis`, `service.validation`, `model.import`; при появлении второго провайдера — `service.import.yandex` и т.д. Опционально вынести выбор стратегии в `service.import.registry`.

---

## 5. План рефакторинга по шагам

| Шаг | Что менять | Что проверять | Критерий готовности |
|-----|------------|----------------|---------------------|
| **1. Вынос выбора парсера** | Ввести `ImportStrategyRegistry` (или оставить выбор в ImportService, но возвращать результат типа Result/сеaled вместо error()). При неизвестном provider — явное исключение (например UnsupportedImportProviderException) и маппинг в 400 в контроллере. | Существующие вызовы импорта (одиночный и batch). | Неизвестный provider → 400, тело с кодом; текущие сценарии работают. |
| **2. Унификация DTO** | Зафиксировать контракт: допустимые пары (provider, payloadType); при несовпадении — 400. Опционально — общий тип запроса с provider/payloadType/cityId/items. | Контроллер и клиенты. | Невалидная пара provider+payloadType → 400. |
| **3. Централизованная обработка ошибок** | В контроллере: перехват UnsupportedImportProvider, ValidationException, «city not found»; ответы 400/404 с единым форматом тела. Проверка существования city перед импортом. | Все сценарии ошибок (неверный header, несуществующий city, неверный body). | Нет 500 на «клиентских» ошибках; в логах остаются только серверные. |
| **4. Тесты** | Unit: парсер на реальных payload (golden); нормализатор; маппер; валидатор. Integration: контроллер с моком сервиса (успех + неизвестный provider + validation). Контракт: один реальный JSON 2ГИС в тестах. | Покрытие новых веток (ошибки провайдера, валидация). | Зелёные тесты; добавление нового провайдера не ломает старые. |
| **5. Метрики/логирование** | В ImportService: логировать provider, payloadType, cityId, размер items, время выполнения; при batch — successful/failed. Опционально: correlationId (header или сгенерировать). | Логи в тестовом прогоне. | По логам можно восстановить кто, что и сколько импортировал и за какое время. |

Отдельным шагом (после 1–3) можно ввести **partial batch**: в стратегии не бросать из цикла, а собирать успехи/ошибки по каждому item и возвращать список `ImportObjectResult` с полем error; в сервисе считать successful/failed и вызывать markSuccess с «последним» успешным или ввести markPartial(importId, report).

---

## 6. Тестовая стратегия

- **Unit парсеров:** на реальных JSON (golden files): валидный объект 2ГИС → ожидаемый `TwoGisScrapedPlace`; отсутствие id/location → исключение с понятным сообщением.
- **Unit нормализатора:** на выходе парсера: category/rubrics → ожидаемый locationType и buildingContext; атрибуты → feeType, amenities, status.
- **Unit маппера:** `NormalizedRestroomCandidate` → `RestroomCreateDto` (с/без buildingId и inheritBuildingSchedule).
- **Unit валидаторов:** Konform — пустой items, неверный размер для single/batch.
- **Registry:** при наличии — тест «по provider возвращается нужная стратегия», «неизвестный provider → null или исключение».
- **Контрактные тесты:** один-два реальных payload 2ГИС (из примеров) через полный стек до БД (integration) — проверка, что создаётся restroom (и при необходимости building) с ожидаемыми полями.
- **Идемпотентность:** два одинаковых импорта подряд → один и тот же restroomId, без дубликатов.
- **Property-based/fuzz:** опционально; при наличии — генерация JSON с обязательными полями и проверка, что парсер либо успешен, либо бросает с понятным сообщением.

---

## 7. Краткое резюме

| Действие | Что |
|----------|-----|
| **Оставить как есть** | Общий поток: Controller → Service → Strategy; extract → parse → normalize → map → persist; реестр стратегий через Map по provider; upsert по (originProvider, originId); запись в restroom_imports. |
| **Обязательно переделать** | (P0) Partial batch или явное решение «всё или ничего» с документированием; (P1) маппинг «неизвестный provider» / неверный payloadType в 400; проверка cityId; (P1) реальные successful/failed в batch-ответе при введении partial. |
| **Можно позже** | Явный ParseResult/ImportReport; dryRun; авто-детект по sniff; дедуп по geo/имени; вынос констант JSON в модели; единый формат ошибок в теле ответа (уже частично через ValidationException). |

---

## 8. Чеклист по разделам

### A) Архитектура и ответственность

| Вопрос | Ответ |
|--------|--------|
| Единый оркестратор? | Да — ImportService. |
| Разделены ли выбор парсера / парсинг / маппинг / валидация / сохранение / репортинг? | Частично: выбор стратегии и сохранение — в сервисе; парсинг/нормализация/маппинг и persist — в стратегии; валидация входа — в контроллере. Валидация отдельного item не вынесена в отдельный слой. |
| God method? | Нет одного метода на 200+ строк; логика стратегии разбита на extract → parse → normalize → resolveBuilding → map → upsert. |
| Границы пакетов? | Да: controller, service.import, service.import.twogis, model.import, service.validation. |

### B) Выбор стратегии (Strategy + Registry)

| Вопрос | Ответ |
|--------|--------|
| Выбор не через гигантский when? | Да — через Map по provider. |
| Реестр provider → parser? | Стратегия = парсер + нормализатор + маппер + persist; реестр по сути provider → strategy. |
| Неизвестный provider? | error() → 500; нужно 400. |
| Версия схемы / совместимость? | Нет. |
| Авто-детект? | Нет. |

### C) Контракты и типы данных

| Вопрос | Ответ |
|--------|--------|
| Парсер возвращает DTO, не домен? | Да — TwoGisScrapedPlace, затем NormalizedRestroomCandidate, затем RestroomCreateDto. |
| ParseResult / ImportReport? | Нет типизированного ParseResult; ImportExecutionResult/ImportBatchResult есть, но без кодов ошибок по элементу. |
| Обязательные поля / валидация? | В парсере id, location обязательны; остальное с дефолтами. Нет единого списка обязательных полей на уровне контракта. |
| Магические строки JSON? | Есть в парсере; частично вынесены в TwoGisAttributeGroup/TwoGisCategory/TwoGisRubric. |

### D) Обработка ошибок и устойчивость

| Вопрос | Ответ |
|--------|--------|
| Опасные valueOf/!!/cast? | valueOf в заголовках обёрнуты в runCatching → ValidationException. В стратегии originId!! — потенциальный риск при изменении контракта. |
| Ошибка одного элемента не валит импорт? | Нет — при ошибке в одном item падает весь batch. |
| Классификация fatal / per-item / warning? | Нет. |
| Логи: provider, correlationId, кол-во, время? | Частично: provider, payloadType, cityId есть; correlationId и длительность — нет. |

### E) Идемпотентность, дедуп, merge

| Вопрос | Ответ |
|--------|--------|
| Ключ идемпотентности? | (originProvider, originId). |
| Upsert/merge описаны? | Да — findByOrigin → update или save. |
| Дедуп по geo/имени при отсутствии externalId? | Нет, не вынесен. |
| Повторный импорт не создаёт дубликаты? | Да — по originId обновляется существующий. |

### F) Производительность и память

| Вопрос | Ответ |
|--------|--------|
| Большие JSON / streaming? | Весь payload в памяти; для очень больших batch может понадобиться чанкинг. |
| N+1 при сохранении? | Нет; в одной транзакции цикл по items, каждый item — один upsert + setNearestStation. |
| Индексы под ключи идемпотентности? | Да — поиск по origin_provider + origin_id. |
| Время импорта в метриках/логе? | Нет. |

### G) Транзакции и консистентность

| Вопрос | Ответ |
|--------|--------|
| Границы транзакции? | Одна транзакция на весь batch в стратегии. |
| Падение части записей? | При ошибке откат всего batch. |
| dryRun? | Нет. |

### H) Тестируемость

| Вопрос | Ответ |
|--------|--------|
| Парсеры на реальных payload? | Есть unit-тесты нормализатора и парсера. |
| Реестр тестируется? | Нет отдельного теста реестра (один провайдер). |
| Валидаторы/нормализаторы отдельно? | Да, unit-тесты. |
| Идемпотентность (два прогона)? | Есть в integration (update при повторном импорте). |

### I) Читабельность и стиль Kotlin

| Вопрос | Ответ |
|--------|--------|
| sealed/Result/Either? | Нет; исключения. |
| Парсеры без побочных эффектов? | Да. |
| Инфраструктура не внутри парсера? | Да; парсер только парсит. |
| Константы/enum вынесены? | Частично (2GIS enums). |

### J) Расширяемость

| Вопрос | Ответ |
|--------|--------|
| Новый провайдер без правок оркестратора? | Да — новая стратегия + регистрация в DI. |
| Переиспользование шагов пайплайна? | Общие: PayloadExtractor, маппер кандидата в CreateDto; парсер и нормализатор — свои на провайдера. |
| Документация по провайдерам? | Есть IMPORT_ARCHITECTURE.md; список поддерживаемых и как добавить — можно усилить. |

---

*Документ составлен по коду на момент аудита; при изменениях в коде актуализируйте разделы 1–3 и чеклист.*
