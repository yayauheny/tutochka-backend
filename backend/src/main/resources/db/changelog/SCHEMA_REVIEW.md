# План для агента: сборка clean-схемы БД

## Цель

Собрать новую clean БД с нуля, в понятной и поддерживаемой структуре, без исторических `ALTER` и без смешения разных зон ответственности в одном файле.

Этот документ является рабочим планом для агента.

## Главные правила

1. Старую историю миграций не трогать. Она остаётся в `legacy/` как fallback.
2. Новую clean-схему собирать только через новые `CREATE TABLE` и отдельные data-load файлы.
3. Не смешивать в одном SQL:
   - базовые справочники и operational-таблицы;
   - users/privacy;
   - analytics;
   - индексы;
   - CSV-данные.
4. Справочные данные держать отдельно в CSV.
5. Redis и будущие доработки не включать в текущую реализацию clean-схемы.

## Что должно получиться по файлам

Нужно привести clean-структуру к такому виду:

- `001_extensions.sql`
- `010_core_tables.sql`
- `011_user_tables.sql`
- `012_analytics_tables.sql`
- `020_indexes.sql`
- `030_constraints.sql`
- `040_seed_core.sql`
- `050_checks.sql`
- `db.changelog-clean.yml`

Данные справочников:

- `reference_data/countries.csv`
- `reference_data/cities.csv`
- `reference_data/subway_lines.csv`
- `reference_data/subway_stations.csv`

Загрузка справочников:

- `reference_load/031_load_countries.yml`
- `reference_load/032_load_cities.yml`
- `reference_load/033_load_subway_lines.yml`
- `reference_load/034_load_subway_stations.yml`

## Что в каком файле делать

### `001_extensions.sql`

Только расширения БД, если реально нужны clean-схеме.

Сюда не класть таблицы, индексы, данные и ограничения.

### `010_core_tables.sql`

Сюда входят только базовые справочники и operational-таблицы:

- `countries`
- `cities`
- `subway_lines`
- `subway_stations`
- `buildings`
- `restrooms`
- `restroom_imports`
- `restroom_duplicate_suspicions`

Это первый основной SQL-файл, который поднимает ядро сервиса.

### `011_user_tables.sql`

Сюда входят только таблицы, связанные с пользователями и персональными данными:

- `users`
- `user_privacy_consents`
- `user_data_requests`

Если позже появятся отдельные user-centered сущности, они должны идти сюда или в отдельный user-focused SQL, но не в core.

### `012_analytics_tables.sql`

Сюда входят только таблицы аналитики:

- `analytics_events`
- `analytics_aggregates`

`user_analytics` в первую clean-схему не входит.

### `020_indexes.sql`

Сюда вынести все индексы:

- `UNIQUE`
- btree на FK
- рабочие составные индексы
- partial unique индексы

В этом файле не должно быть `CREATE TABLE`.

### `030_constraints.sql`

Сюда вынести ограничения схемы:

- foreign keys
- `NOT NULL`, если они не заданы сразу при `CREATE TABLE`
- structural constraints, которые не относятся к enum-like статусам

В этом файле не делать `CHECK` для бизнес-статусов и enum-like полей.

### `040_seed_core.sql`

Только базовые SQL-сиды, если они не относятся к справочникам CSV.

Если данные можно хранить как reference data, они не должны попадать сюда.

### `050_checks.sql`

Только технические проверки после bootstrap:

- контроль наличия обязательных строк
- сверка связей
- валидационные select/check SQL

Без создания таблиц, индексов и боевых данных.

## Что грузить через CSV

Через CSV и Liquibase load-файлы грузить:

- страны
- города
- ветки метро
- станции метро

Правила:

- UUID в CSV стабильные и заранее определённые
- не использовать генерацию UUID на лету
- не держать reference data большими SQL insert-блоками
- reference data должна быть читаемой и пригодной для ручной проверки

## Что нужно привести в соответствие в коде

Помимо SQL-скриптов и CSV, агент должен синхронизировать код приложения с новой clean-схемой.

Обязательный минимум:

- обновить ORM/jOOQ/генерируемые классы и схему кода под новые таблицы, поля, индексы и связи;
- обновить entity/model-классы для:
  - `users`
  - `restrooms`
  - `buildings`
  - `cities`
  - `subway_lines`
  - `subway_stations`
  - `restroom_imports`
  - `analytics_events`
  - `analytics_aggregates`
- обновить repository-слой под:
  - новые unique provider id поля
  - `slug` и `code`
  - новые nullable/non-nullable связи
  - partial unique по `origin_provider + origin_id`
- обновить service-слой и use-case логику под:
  - `hard delete`
  - `is_hidden`
  - `moderation_status`
  - новые правила дедупликации
  - отсутствие `tg_chat_id` в `users`
  - retention по `analytics_events`
- обновить import pipeline под:
  - `two_gis_id`, `yandex_id`, `google_id`, `apple_id`
  - `provider_reference`
  - `city_slug`
  - новые snapshot/reference поля в `restroom_imports`
- обновить enum-валидацию на стороне бэкенда, так как `CHECK` в БД для статусных полей не будет;
- обновить DTO/response/request-модели, если они завязаны на старые поля или старые названия;
- обновить bootstrap/init/config, если где-то жёстко прошиты старые пути changelog или старые имена SQL-файлов;
- обновить генерацию тестовых данных, factory/fixture-слой и seed helper-утилиты;
- обновить тесты:
  - unit-тесты сервисов
  - integration-тесты репозиториев
  - миграционные проверки bootstrap clean БД
  - проверки загрузки CSV-справочников

Отдельно проверить и привести в соответствие:

- поиск по городу и метро через `slug`/`code`;
- логику удаления города, здания и туалета;
- дедупликацию по provider id и `origin_provider + origin_id`;
- логику скрытия туалетов из выдачи;
- обработку пользовательских согласий и запросов на удаление/анонимизацию;
- аналитические джобы или scheduled cleanup, если они уже есть в коде.

## Финальные решения по схеме

### Удаление и связи

Использовать:

- `countries -> cities`: `ON DELETE CASCADE`
- `cities -> subway_lines`: `ON DELETE RESTRICT`
- `cities -> buildings`: `ON DELETE RESTRICT`
- `cities -> restrooms`: `ON DELETE RESTRICT`
- `subway_lines -> subway_stations`: `ON DELETE CASCADE`
- `buildings -> restrooms`: `ON DELETE CASCADE`

Дополнительно:

- `restroom_imports` не должна блокировать удаление operational-сущностей
- ссылки из `restroom_imports` на `city`, `building`, `restroom` допускаются как nullable FK
- `subway_stations` не должна хранить дублирующую ссылку на `city`, если город уже выводится через `subway_line_id`

### Видимость и жизненный цикл

Для `restrooms` использовать:

- `status`
- `is_hidden`
- `moderation_status`

Правило:

- `is_hidden` отвечает только за скрытие из выдачи
- `status` отвечает за жизненный цикл
- `moderation_status` нужен уже в первой clean-схеме

### Stable keys

Использовать:

- `countries.code`
- `cities.slug`
- `subway_lines.code`
- `subway_stations.slug`

Формат:

- `cities.slug`: lowercase kebab-case с префиксом страны, например `by-minsk`
- `subway_stations.slug`: lowercase kebab-case, уникальный в рамках линии

### Внешние provider id

В `buildings` и `restrooms` использовать только:

- `two_gis_id`
- `yandex_id`
- `google_id`
- `apple_id`

Не использовать JSONB для provider id.

### JSONB

Оставить только:

- `work_time`
- `amenities`
- `raw_payload`

### Время

Во всей новой БД использовать только `TIMESTAMPTZ`.

### История импорта

`restroom_imports` должна содержать:

- nullable FK на `city`, `building`, `restroom`
- `provider_reference`
- `city_slug`
- `resolved_building_provider_id`
- `resolved_restroom_origin_key`

Правило хранения:

- raw import history хранить 180 дней

### Пользователи и ПД

В `users` оставить:

- `id`
- `tg_user_id`
- `username`
- `source`
- `acquisition_tag`
- `is_blocked`
- `account_status`
- `last_seen_at`
- `created_at`
- `updated_at`

Не хранить в `users`:

- `tg_chat_id`
- согласия
- запросы на удаление
- баланс
- настройки
- репутацию
- referral history

### Согласия

`user_privacy_consents` делать append-only журналом.

На старте поддерживать только:

- `PRIVACY`
- `TERMS`

Отдельную таблицу current state не делать.

### Запросы пользователя

`user_data_requests` должна поддерживать:

- `DELETE`
- `ANONYMIZE`
- `EXPORT`

Исполнение:

- асинхронное
- со статусным циклом в БД

### Аналитика

В clean-схему включить:

- `analytics_events`
- `analytics_aggregates`

Не включать:

- `user_analytics`

Правила:

- сырые geo-события хранить 90 дней
- после 90 дней удалять их или переливать в обезличенные агрегаты
- `analytics_aggregates` строить на типизированных колонках
- базовый grain: день + город + тип события

## Финальные замечания по таблицам

### `countries`

- справочная таблица
- без `is_hidden`
- без `deleted_at`
- `code` обязателен и уникален

### `cities`

- `slug` обязателен и глобально уникален
- `coordinates` остаются
- `city_bounds` не включать в первую clean-схему
- `region_code` не включать в первую clean-схему

### `subway_lines`

- `code` обязателен
- `UNIQUE (city_id, code)`
- отдельный `short_label` не нужен

### `subway_stations`

- `slug` обязателен
- `UNIQUE (subway_line_id, slug)`
- `UNIQUE (subway_line_id, line_order)`
- отдельные provider id не нужны
- отдельная таблица пересадочных связей не нужна
- достаточно `is_transfer`

### `buildings`

- отдельный `provider_primary_id` не нужен
- `import_status` не включать в первую clean-схему

### `restrooms`

- `city_id` обязателен
- `status` обязателен
- `is_hidden` обязателен
- `moderation_status` входит в первую clean-схему
- `created_by_user_id` и `updated_by_user_id` сделать nullable и включить сразу
- добавить partial unique индекс на `origin_provider + origin_id`

### `restroom_imports`

- входит в первую clean-схему
- хранит и nullable FK, и snapshot/reference поля
- статусы валидируются бэкендом, без `CHECK`

### `restroom_duplicate_suspicions`

- входит в первую clean-схему
- нужен индекс по `status`
- нужен индекс по `created_at`

### `users`

- `tg_user_id` обязателен и уникален
- `username` nullable
- `source` и `acquisition_tag` включить сразу
- `account_status` и `last_seen_at` включить сразу

### `user_privacy_consents`

- append-only журнал
- только `PRIVACY` и `TERMS` на старте

### `user_data_requests`

- входит в первую clean-схему
- поддерживает `DELETE`, `ANONYMIZE`, `EXPORT`

### `analytics_events`

- входит в первую clean-схему
- хранит сырые события до 90 дней

### `analytics_aggregates`

- входит в первую clean-схему
- строится на типизированных колонках

## Индексы, которые должны быть вынесены в отдельный SQL

Обязательные FK btree-индексы:

- `cities(country_id)`
- `subway_lines(city_id)`
- `subway_stations(subway_line_id)`
- `buildings(city_id)`
- `restrooms(city_id)`
- `restrooms(building_id)`
- `restrooms(subway_station_id)`
- `restroom_imports(city_id)`
- `restroom_imports(building_id)`
- `restroom_imports(restroom_id)`

Обязательные unique-индексы:

- `countries(code)`
- `cities(slug)`
- `subway_lines(city_id, code)`
- `subway_stations(subway_line_id, slug)`
- `subway_stations(subway_line_id, line_order)`
- `users(tg_user_id)`
- `buildings(two_gis_id)` where not null
- `buildings(yandex_id)` where not null
- `buildings(google_id)` where not null
- `buildings(apple_id)` where not null
- `restrooms(two_gis_id)` where not null
- `restrooms(yandex_id)` where not null
- `restrooms(google_id)` where not null
- `restrooms(apple_id)` where not null
- `restrooms(origin_provider, origin_id)` where `origin_id` is not null

Обязательные рабочие индексы:

- `restrooms(city_id, status, is_hidden)`
- `restroom_duplicate_suspicions(status, created_at)`
- `analytics_events(created_at)`
- `analytics_events(user_id, created_at)` если user-bound события остаются в сыром виде до агрегирования

## Порядок работы для агента

1. Не трогать `legacy/`.
2. Подготовить новую структуру clean-файлов с разбиением на `core`, `users`, `analytics`, `indexes`, `constraints`.
3. Заполнить `010_core_tables.sql`.
4. Заполнить `011_user_tables.sql`.
5. Заполнить `012_analytics_tables.sql`.
6. Вынести все индексы в `020_indexes.sql`.
7. Вынести все FK и structural constraints в `030_constraints.sql`.
8. Подготовить CSV для `countries`, `cities`, `subway_lines`, `subway_stations`.
9. Подключить CSV через `reference_load/*.yml`.
10. Добавить базовые проверки в `050_checks.sql`.
11. После этого поднимать новую ветку Neon и проверять bootstrap clean БД с нуля.
