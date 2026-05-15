# 2GIS import hardening v1

## Summary

The original hardening work for 2GIS import is now absorbed into the canonical `yayauheny.by.importing` subsystem.

The behavior that was hardened remains:
- bounded batch size
- chunked processing
- per-item failure isolation
- deterministic building/restroom dedup
- inbox upsert behavior in `restroom_imports`
- nearby duplicate suspicion logging
- idempotent same-provider convergence

## Where that behavior lives now

### Runtime flow

- `backend/src/main/kotlin/yayauheny/by/importing/api/ImportController.kt`
- `backend/src/main/kotlin/yayauheny/by/importing/service/ImportService.kt`
- `backend/src/main/kotlin/yayauheny/by/importing/service/ImportBatchProcessor.kt`
- `backend/src/main/kotlin/yayauheny/by/importing/service/ImportPipeline.kt`

### 2GIS adapter

- `backend/src/main/kotlin/yayauheny/by/importing/provider/twogis/TwoGisImportAdapter.kt`
- `backend/src/main/kotlin/yayauheny/by/importing/provider/twogis/TwoGisScrapedParser.kt`
- `backend/src/main/kotlin/yayauheny/by/importing/provider/twogis/TwoGisScrapedNormalizer.kt`
- `backend/src/main/kotlin/yayauheny/by/importing/provider/twogis/TwoGisGenderFromTitleResolver.kt`

### Shared deterministic dedup

- `backend/src/main/kotlin/yayauheny/by/importing/dedup/MatchKeyGenerator.kt`
- `backend/src/main/kotlin/yayauheny/by/importing/dedup/PayloadHashing.kt`

### Import persistence

- `backend/src/main/kotlin/yayauheny/by/importing/repository/ImportInboxRepository.kt`
- `backend/src/main/kotlin/yayauheny/by/importing/repository/BuildingImportRepository.kt`
- `backend/src/main/kotlin/yayauheny/by/importing/repository/RestroomImportRepository.kt`
- `backend/src/main/kotlin/yayauheny/by/importing/repository/DuplicateSuspicionRepository.kt`

## Important architectural change since v1

The old strategy-based 2GIS flow is gone.

2GIS now contributes only:
- provider DTO parsing
- provider-specific metadata extraction
- provider-specific normalization

The generic pipeline owns:
- chunking
- transactions and savepoints
- inbox writes
- dedup
- canonical persistence
- provider linking
- suspicion logging

## Coverage that protects the hardening

- parser tests in `backend/src/test/kotlin/yayauheny/by/unit/importing/provider/twogis`
- metadata extraction tests in `backend/src/test/kotlin/yayauheny/by/unit/importing/provider/ImportAdapterMetadataTest.kt`
- match-key and payload hashing tests in `backend/src/test/kotlin/yayauheny/by/unit/importing/dedup`
- integration scenarios in the importing pipeline integration test suite
