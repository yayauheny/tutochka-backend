# Архитектура import subsystem

## Обзор

Импорт теперь оформлен как отдельный backend subsystem под `yayauheny.by.importing`.

Цель подсистемы:
- принимать provider-specific payload
- нормализовать его в внутреннюю import-команду
- выполнять детерминированный dedup и canonical persistence
- вести inbox/audit историю в `restroom_imports`
- не загрязнять обычные `RestroomRepository` и `BuildingRepository` import-методами

Поддерживаемые HTTP routes не менялись:
- `POST /api/v1/import`
- `POST /api/v1/import/batch`

## Слои

```text
ImportController
  -> ImportService
    -> ImportBatchProcessor
      -> ImportAdapterRegistry
      -> ImportSourceAdapter
      -> ImportPipeline
        -> ImportInboxRepository
        -> BuildingImportRepository
        -> RestroomImportRepository
        -> DuplicateSuspicionRepository
```

## Пакеты

```text
backend/src/main/kotlin/yayauheny/by/importing/
  api/
  dedup/
  exception/
  mapper/
  model/
  provider/
    twogis/
    yandex/
  repository/
  service/
```

## Ответственности

### API

- `api/ImportController.kt`
  - читает headers/body
  - делегирует в `ImportService`
  - не содержит import business logic

### Application

- `service/ImportService.kt`
  - проверяет supported `(provider, payloadType)`
  - проверяет существование `cityId`
  - оркестрирует single и batch import

- `service/ImportBatchProcessor.kt`
  - режет batch на чанки
  - создаёт inbox row до обработки item
  - управляет chunk transaction + item savepoint isolation
  - агрегирует `ImportBatchSummary`

- `service/ImportPipeline.kt`
  - generic import flow для одного item
  - resolve building
  - resolve existing restroom
  - deterministic dedup
  - canonical persistence
  - external/provider linking
  - nearby duplicate suspicion logging

### Provider adapters

- `provider/ImportSourceAdapter.kt`
  - provider-specific contract
  - не знает о транзакциях и persistence orchestration

- `provider/twogis/TwoGisImportAdapter.kt`
- `provider/yandex/YandexImportAdapter.kt`
  - parse raw JSON
  - extract provider-local inbox metadata
  - normalize payload to internal command

### Import persistence

- `repository/ImportInboxRepository.kt`
  - owns `restroom_imports` inbox/audit behavior

- `repository/BuildingImportRepository.kt`
  - external id lookup/linking
  - deterministic building match-key lookup
  - imported building upsert

- `repository/RestroomImportRepository.kt`
  - origin lookup
  - external map lookup/linking
  - restroom match-key lookup
  - imported restroom upsert

- `repository/DuplicateSuspicionRepository.kt`
  - writes nearby duplicate suspicion rows

### Shared import internals

- `dedup/MatchKeyGenerator.kt`
  - deterministic text normalization
  - building/restroom match key generation

- `dedup/PayloadHashing.kt`
  - canonical JSON hashing
  - inbox payload hash support

- `mapper/RestroomImportMapper.kt`
  - normalized import candidate -> `RestroomCreateDto`

## Generic import flow

1. controller receives provider, payload type, city, payload
2. `ImportService` validates capability and city
3. `ImportBatchProcessor` asks adapter to parse each raw item
4. adapter returns `ProviderImportEnvelope`
   - `InboxMetadata`
   - normalized command
   - raw payload
5. inbox row is inserted or updated as `PENDING`
6. `ImportPipeline` resolves building
7. `ImportPipeline` resolves existing restroom
8. dedup priority is applied:
   - provider origin / external id
   - deterministic match key
   - nearby ambiguity -> suspicion log only
9. canonical entity is persisted
10. provider links are written
11. inbox row is marked `SUCCESS` or `FAILED`
12. batch summary is returned with compact per-item results

## Transaction model

- one transaction per chunk
- one savepoint per item inside a chunk
- item failure rolls back only that item
- whole batch does not roll back because of one bad payload

## Current boundaries

### Domain repositories

These stay domain-oriented:
- `repository/RestroomRepository.kt`
- `repository/BuildingRepository.kt`

They no longer expose import-only APIs.

### Import repositories

Import-only SQL lives behind importing-specific repository interfaces.

The current implementation still reuses helper methods inside:
- `repository/impl/RestroomRepositoryImpl.kt`
- `repository/impl/BuildingRepositoryImpl.kt`

But that reuse is now internal to import persistence, not part of the public domain repository contract.

## Provider-specific code

Provider-specific parsing and normalization now lives only under:
- `importing/provider/twogis/*`
- `importing/provider/yandex/*`

Generic pipeline code does not read raw provider JSON fields like `id`, `placeId`, `url`, or `scrapedAt` directly.

## Known limitations

- import still runs synchronously inside HTTP requests
- `NormalizedImportCommand` is currently a typealias to `NormalizedRestroomCandidate`, not a richer provider-agnostic command model
- cross-provider entity resolution is still conservative and deterministic only
- schedule normalization is still not unified across all providers
