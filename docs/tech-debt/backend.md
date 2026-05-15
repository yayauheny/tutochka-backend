# Техдолг backend

Список отсортирован по приоритету переписывания: сначала security и crash-risk, потом архитектурный долг и только затем cleanup.

## Critical

- [ ] **Import endpoints открыты без auth-слоя**
   Файлы: `backend/src/main/kotlin/yayauheny/by/Application.kt:35-73`, `backend/src/main/kotlin/yayauheny/by/importing/api/ImportController.kt`, `backend/src/main/resources/openapi.yaml:72-168`.

   Проблема: приложение не устанавливает auth-плагин, а `ImportController` полагается только на заголовки и валидацию тела. Это означает, что write-endpoint для импорта доступен без отдельной проверки прав.

   Почему это критично: если backend доступен не только в приватной сети, любой клиент сможет инициировать запись импортных данных. Это прямой путь к несанкционированной модификации базы.

   Что делать: ввести явную аутентификацию или хотя бы сетевую изоляцию для ingestion-эндпоинтов, а публичные read-методы отделить от private write surface.

- [ ] **transactionSuspend блокирует coroutine inside transaction и использует `result!!`**
   Файл: `backend/src/main/kotlin/yayauheny/by/util/TransactionExtensions.kt:23-44`.

   Проблема: внутри `transaction {}` вызывается `runBlocking`, а результат сохраняется во внешний nullable `result`, после чего возвращается через `result!!`.

   Почему это критично: блокирование coroutine-потока внутри транзакции ухудшает конкуренцию и усложняет cancellation semantics. `result!!` добавляет ещё и лишний crash-path на пустом/некорректном состоянии.

   Что делать: переписать транзакционный helper на coroutine-friendly паттерн без `runBlocking` внутри transaction block и без `!!` на возврате результата.

- [ ] **Большой batch import выполняется как один длинный sync-request с множеством round-trip к БД**
   Файлы: `backend/src/main/kotlin/yayauheny/by/importing/api/ImportController.kt`, `backend/src/main/kotlin/yayauheny/by/importing/service/ImportService.kt`, `backend/src/main/kotlin/yayauheny/by/importing/service/ImportBatchProcessor.kt`, `backend/src/main/kotlin/yayauheny/by/importing/service/ImportPipeline.kt`.

   Проблема: batch обрабатывается синхронно в рамках одного HTTP-запроса и одной транзакции, при этом для каждого элемента выполняется серия отдельных SQL-операций (create pending, lookup/update/insert, mark success и т.д.).

   Почему это критично: при межрегионной задержке между backend и БД (например, сервер в США, БД в Европе) время выполнения растёт линейно по числу элементов и легко упирается в infra timeout (reverse proxy / LB / DB statement timeout), как в кейсе с ~1000 объектов.

   Что делать: перейти на job-based импорт (enqueue + async worker), обрабатывать батчи чанками (например, 50-200), коммитить по чанкам, а не одним большим tx, и использовать bulk SQL там, где возможно.

## Medium

- [ ] **QueryExecutor содержит логическую ошибку в флаге `last`**
   Файл: `backend/src/main/kotlin/yayauheny/by/common/query/builder/QueryExecutor.kt:94-125`.

   Проблема: `last` вычисляется как `fetchCount && request.page >= totalPages`. При нумерации страниц с нуля это даёт неверный результат на последней странице.

   Почему это плохо: pagination metadata становится недостоверной, а клиенты и bot могут строить неправильную навигацию по страницам.

   Что делать: пересчитать `last` на основе zero-based индекса страницы или выводить его из `content.size` и общего числа элементов.

- [ ] **ErrorHandlingPlugin ловит `Throwable` и маскирует системные ошибки**
   Файл: `backend/src/main/kotlin/yayauheny/by/common/plugins/ErrorHandlingPlugin.kt:27-189`.

   Проблема: в конце status pages стоит `exception<Throwable>`, который превращает любые неожиданные сбои в generic 500 response.

   Почему это плохо: такая ловушка может проглотить cancellation, programmer errors и другие ситуации, которые лучше не скрывать. Это делает отладку и анализ инцидентов заметно хуже.

   Что делать: ловить более узкий тип, а для критичных случаев явно rethrow-ить cancellation и non-recoverable errors.

- [ ] **Query abstraction слишком обобщён и требует unsafe casts**
   Файлы: `backend/src/main/kotlin/yayauheny/by/common/query/builder/QueryBuilder.kt:11-107`, `backend/src/main/kotlin/yayauheny/by/common/query/QueryExtensions.kt:10-59`, `backend/src/main/kotlin/yayauheny/by/common/query/builder/QueryExecutor.kt:16-76`.

   Проблема: helpers для query stage вынуждены кастовать `SelectWhereStep` к `SelectConditionStep`, а часть поведения скрыта за generic-обёртками.

   Почему это плохо: API становится хрупким по отношению к jOOQ-типам и затрудняет понимание, какой именно шаг запроса нужен в каждом месте.

   Что делать: упростить query builder API, разделить helpers по стадиям запроса или убрать лишнюю универсальность там, где она не даёт реальной выгоды.

- [ ] **Фильтры кодируются через custom string DSL в query params**
   Файл: `backend/src/main/kotlin/yayauheny/by/util/ApplicationCallExtensions.kt:54-90`.

   Проблема: `filters` передаются как одна строка с делителями, а парсинг опирается на `split`, `FilterOperator.valueOf` и ручную обработку ошибок.

   Почему это плохо: значения с делителями ломают формат, а OpenAPI не описывает этот DSL как нормальный контракт. Клиенты не могут надёжно валидировать такой запрос заранее.

   Что делать: перевести фильтры на структурированные query params или body payload и отразить их в OpenAPI-схеме.

- [ ] **Controllers смешивают transport, validation, metrics и response shaping**
   Файлы: `backend/src/main/kotlin/yayauheny/by/controller/RestroomController.kt:36-154`, `backend/src/main/kotlin/yayauheny/by/controller/CityController.kt:27-96`, `backend/src/main/kotlin/yayauheny/by/controller/CountryController.kt:22-75`, `backend/src/main/kotlin/yayauheny/by/importing/api/ImportController.kt`.

   Проблема: в контроллерах живут и маршрутизация, и валидация, и логирование, и часть сборки response DTO. В `ImportController` single и batch ветки ещё и почти полностью дублируют друг друга.

   Почему это плохо: transport layer разрастается и начинает тащить на себе бизнес-решения, которые должны жить ниже. Такой код сложнее тестировать и читать.

   Что делать: оставить контроллерам только вход и выход, а валидацию, assembly и повторяющиеся сценарии вынести в сервисы или отдельные assemblers.

- [ ] **ImportService смешивает orchestration с broad exception handling**
   Файл: `backend/src/main/kotlin/yayauheny/by/importing/service/ImportService.kt`.

   Проблема: `import()` всё ещё ловит `Throwable` вокруг single-item flow и вручную помечает inbox row как failed.

   Почему это плохо: catch-all на уровне сервиса может скрыть cancellation и другие нештатные ошибки, даже если batch orchestration уже вынесена в `ImportBatchProcessor`.

   Что делать: сузить перехват исключений до import-domain ошибок и оставить неожиданные runtime failures выше по стеку.

- [ ] **Provider-specific import pipeline слишком жёстко привязан к concrete classes**
   Файлы: `backend/src/main/kotlin/yayauheny/by/importing/service/ImportBatchProcessor.kt`, `backend/src/main/kotlin/yayauheny/by/importing/service/ImportPipeline.kt`, `backend/src/main/kotlin/yayauheny/by/importing/provider/twogis/TwoGisImportAdapter.kt`, `backend/src/main/kotlin/yayauheny/by/importing/provider/yandex/YandexImportAdapter.kt`, `backend/src/main/kotlin/yayauheny/by/importing/repository/RestroomImportRepositoryImpl.kt`, `backend/src/main/kotlin/yayauheny/by/importing/repository/BuildingImportRepositoryImpl.kt`.

   Проблема: runtime уже разделён на adapter + pipeline, но import repositories всё ещё переиспользуют helper-методы из domain repository impls, а provider normalizers по-прежнему содержат эвристики и string matching, которые трудно переиспользовать между источниками.

   Почему это плохо: интерфейсные границы стали чище, но SQL ownership и normalization rules всё ещё частично связаны с concrete implementation details.

   Что делать: полностью вынести import SQL из domain repository impl helpers, а повторяющиеся normalization rules держать в importing-local shared helpers, а не размазывать по provider normalizers.

- [ ] **Repository и mapper классы слишком большие и дублируют projection/mapping logic**
   Файлы: `backend/src/main/kotlin/yayauheny/by/repository/impl/RestroomRepositoryImpl.kt:46-500`, `backend/src/main/kotlin/yayauheny/by/common/mapper/RestroomMapper.kt:28-239`, `backend/src/main/kotlin/yayauheny/by/repository/impl/CityRepositoryImpl.kt:31-320`, `backend/src/main/kotlin/yayauheny/by/repository/impl/CountryRepositoryImpl.kt:24-186`, `backend/src/main/kotlin/yayauheny/by/repository/impl/BuildingRepositoryImpl.kt:28-242`.

   Проблема: вручную собираются длинные списки полей, join-части маппинга продублированы, а `!!`-assumptions размазаны по большим объектам.

   Почему это плохо: schema change требует правок в нескольких местах, а любая nullability-ошибка маскируется большим объёмом ручного кода.

   Что делать: выделить переиспользуемые projection fragments, smaller mappers и общие join helpers, чтобы уменьшить объём ручной синхронизации.

- [ ] **Uniqueness checks выполняются в application code до insert**
   Файлы: `backend/src/main/kotlin/yayauheny/by/service/CityService.kt:32-60`, `backend/src/main/kotlin/yayauheny/by/service/CountryService.kt:26-35`.

   Проблема: перед созданием сущности сервисы делают предварительный поиск на уникальность имени или кода.

   Почему это плохо: такой check не является корректной защитой от гонки и добавляет лишний запрос в happy path. Истинный guard уже обеспечивает база.

   Что делать: опираться на unique constraint в базе и конвертировать нарушение в conflict response. Предварительную проверку оставлять только если она реально улучшает UX.

- [ ] **Schedule normalization и open-now contract разнесены по нескольким классам**
   Файлы: `backend/src/main/kotlin/yayauheny/by/util/ScheduleUtils.kt:10-89`, `backend/src/main/kotlin/yayauheny/by/model/import/Schedule.kt:7-18`, `backend/src/main/kotlin/yayauheny/by/service/import/twogis/TwoGisScheduleAdapter.kt:25-106`.

   Проблема: специальные случаи вроде `24:00`, `is24x7` и open-now semantics живут в нескольких местах и интерпретируются не совсем одинаково.

   Почему это плохо: время и расписания уже сейчас являются самым тонким доменным правилом. Любая правка логики потребует синхронно менять backend и bot-formatting.

   Что делать: свести расписание к одной нормализованной модели и сделать один контракт на выходе backend, а не размазывать интерпретацию по нескольким helper-ам.

- [ ] **OpenAPI контракт расходится с фактическим поведением backend**
   Файлы: `backend/src/main/resources/openapi.yaml:8,192,326,435,485,522,631`, `backend/src/main/kotlin/yayauheny/by/config/ApiConstants.kt:8`, `backend/src/main/kotlin/yayauheny/by/config/RoutingConfig.kt:24-29`, `backend/src/main/kotlin/yayauheny/by/controller/HealthController.kt:20-44`, `backend/src/main/kotlin/yayauheny/by/common/plugins/ErrorHandlingPlugin.kt:100-163`.

   Проблема: спецификация задаёт `size=20`, тогда как runtime использует `DEFAULT_PAGE_SIZE=10`; health-роуты живут вне `/api/v1`, но global server URL в OpenAPI задан с `/api/v1`; также в спецификации отсутствуют реальные 409/503 ответы, которые backend уже отдаёт.

   Почему это плохо: клиентские интеграции и мониторинг опираются на неверный контракт и ломаются на уровне ожиданий, а не кода.

   Что делать: синхронизировать OpenAPI с runtime и добавить contract-test в CI, который валидирует defaults, префиксы роутов и фактические status-коды.

- [ ] **Batch import не ограничен по объёму запроса**
   Файлы: `backend/src/main/kotlin/yayauheny/by/importing/api/ImportController.kt`, `backend/src/main/kotlin/yayauheny/by/service/validation/ImportValidators.kt:13-20`, `backend/src/main/kotlin/yayauheny/by/importing/service/ImportService.kt`.

   Проблема: batch-путь уже ограничивает число `items`, но всё ещё не ограничивает общий размер request body и остаётся synchronous HTTP ingestion path.

   Почему это плохо: один большой запрос может занять много памяти, удерживать длинную транзакцию и деградировать весь ingestion pipeline.

   Что делать: ввести явные лимиты на число элементов/размер тела, возвращать 413/400 при превышении и рассмотреть chunked-обработку больших batch.

- [ ] **Сборка backend имеет side effects: `compileKotlin` запускает форматирование**
   Файл: `backend/build.gradle.kts:353-355`.

   Проблема: обычная сборка/тест может менять исходники, потому что `compileKotlin` зависит от `ktlintFormat`.

   Почему это плохо: verify-команды перестают быть read-only, появляются неожиданные грязные диффы и хуже воспроизводимость CI.

   Что делать: убрать `ktlintFormat` из compile pipeline и оставить отдельные команды для `check` и явного `format`.

- [ ] **Пагинация без явного sort остаётся недетерминированной**
   Файлы: `backend/src/main/kotlin/yayauheny/by/common/query/builder/QueryBuilder.kt:20-34`, `backend/src/main/kotlin/yayauheny/by/common/query/QueryExtensions.kt:22-34,48-58`.

   Проблема: при отсутствии `sort` query builder не добавляет fallback `ORDER BY`.

   Почему это плохо: страницы могут "плавать" между запросами и пересекаться при параллельных записях, что ломает стабильную навигацию и кеширование.

   Что делать: задать стабильный дефолтный сорт (например, по primary key + createdAt) для всех paginated endpoint-ов.

- [ ] **Идемпотентность импорта реализована неатомарно (check-then-insert/update)**
   Файлы: `backend/src/main/kotlin/yayauheny/by/importing/repository/RestroomImportRepositoryImpl.kt`, `backend/src/main/kotlin/yayauheny/by/repository/impl/RestroomRepositoryImpl.kt`, `backend/src/main/resources/db/changelog/migration/1757246414_init_tables.sql:143-145`.

   Проблема: generic pipeline уже безопаснее, но часть lookup + upsert sequencing всё ещё зависит от нескольких шагов в repository helpers и требует аккуратной concurrency-проверки.

   Почему это плохо: при гонке два воркера могут одновременно не увидеть запись и попытаться вставить одну и ту же сущность; один из них упрётся в unique constraint и запрос завершится ошибкой вместо идемпотентного результата.

   Что делать: заменить на атомарный `INSERT ... ON CONFLICT (origin_provider, origin_id) DO UPDATE ... RETURNING id` и покрыть concurrency-тестом.

- [ ] **Очередь `restroom_imports` имеет поля для дедупа, но они фактически не используются**
   Файлы: `backend/src/main/resources/db/changelog/migration/20251206_update_restroom_imports_table.sql:3-15`, `backend/src/main/kotlin/yayauheny/by/importing/repository/ImportInboxRepositoryImpl.kt`.

   Проблема: поля now заполняются и inbox работает как upserted audit row, но subsystem всё ещё использует один inbox table и не имеет отдельного retry/queue execution layer.

   Почему это плохо: audit и idempotency уже есть, но operational retry semantics всё ещё ограничены synchronous request flow.

   Что делать: сохранить текущий upserted inbox и при необходимости добавить explicit retry worker semantics поверх него вместо перегрузки HTTP entrypoint.

- [ ] **Нет межпровайдерной дедупликации одинаковых туалетов**
   Файлы: `backend/src/main/resources/db/changelog/migration/1757246414_init_tables.sql:131-145`, `backend/src/main/kotlin/yayauheny/by/importing/service/ImportPipeline.kt`, `backend/src/main/kotlin/yayauheny/by/repository/impl/RestroomRepositoryImpl.kt`.

   Проблема: dedup идёт только по `(origin_provider, origin_id)`, то есть один и тот же физический туалет из 2GIS и Yandex создаст две разные записи.

   Почему это плохо: растут дубликаты в выдаче, ухудшается качество поиска и увеличивается ручная работа по чистке данных.

   Что делать: добавить entity resolution слой (гео+адрес+нормализованное имя, пороги расстояния, confidence score, manual review для спорных случаев) и хранить canonical restroom + provider links.

## Minor

- [ ] **JSON prettyPrint включён в runtime backend**
   Файл: `backend/src/main/kotlin/yayauheny/by/Application.kt:58-69`.

   Проблема: pretty printing включён для всех ответов.

   Почему это мелкий долг: это лишние байты и немного CPU в production без заметной пользы для API.

   Что делать: выключить prettyPrint вне локальной отладки.

- [ ] **ApiError и ErrorResponse дублируют error payload концепции**
   Файлы: `backend/src/main/kotlin/yayauheny/by/common/errors/ApiError.kt`, `backend/src/main/kotlin/yayauheny/by/common/errors/ErrorResponse.kt`.

   Проблема: в проекте есть два похожих типа для ошибок, которые частично перекрывают друг друга по смыслу.

   Почему это плохо: разработчику приходится помнить лишнюю вариативность без явной бизнес-выгоды.

   Что делать: оставить один формат ответа на ошибки, если API не нуждается в обоих вариантах одновременно.

- [ ] **RestroomRepositoryImpl содержит dead helper-логики**
   Файл: `backend/src/main/kotlin/yayauheny/by/repository/impl/RestroomRepositoryImpl.kt:484-499`.

   Проблема: `computeIsOpen` выглядит как мёртвый или полумёртвый helper, который не участвует в основном потоке.

   Почему это плохо: мёртвый код усложняет обзор репозитория и создаёт иллюзию неиспользуемой логики, которую всё равно нужно поддерживать.

   Что делать: удалить helper или встроить его туда, где он действительно нужен.

- [ ] **HealthController пингует БД на каждом readiness request**
   Файл: `backend/src/main/kotlin/yayauheny/by/controller/HealthController.kt:14-63`.

   Проблема: readiness endpoint синхронно проверяет базу при каждом вызове.

   Почему это не критично, но заметно: health checks становятся зависимыми от latency базы и создают лишнюю нагрузку под частыми запросами.

   Что делать: оставить это только если именно такой semantics нужен оркестратору. Иначе разделить лёгкий liveness и более дорогой readiness.

- [ ] **Yandex normalizer теряет часть расписаний, если нет структурного `workingHours`**
   Файл: `backend/src/main/kotlin/yayauheny/by/importing/provider/yandex/YandexMapsScrapedNormalizer.kt`.

   Проблема: `buildRawSchedule` возвращает `null`, если массив `workingHours` пуст, даже когда у провайдера есть текстовый `openingHoursText`.

   Почему это плохо: в карточке туалета исчезает полезная информация о режиме работы, хотя она присутствует в исходных данных.

   Что делать: сохранить fallback-представление расписания из `openingHoursText` и явно маркировать его как text-only schedule.
