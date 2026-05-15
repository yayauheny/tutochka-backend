# Import architecture audit

## Current state

The canonical runtime import flow is now:

```text
ImportController
  -> ImportService
    -> ImportBatchProcessor
      -> ImportAdapterRegistry
      -> ImportSourceAdapter
      -> ImportPipeline
```

Legacy strategy-based orchestration under `yayauheny.by.service.import` has been removed from runtime.

## What is isolated correctly

### Provider concerns

- 2GIS parsing, DTOs, metadata extraction, and normalization live under `importing/provider/twogis`
- Yandex parsing, DTOs, metadata extraction, and normalization live under `importing/provider/yandex`
- adapters do not own transaction scope or repository orchestration

### Generic import concerns

- chunking and savepoint isolation live in `ImportBatchProcessor`
- deterministic building/restroom resolution lives in `ImportPipeline`
- inbox/audit writes live in `ImportInboxRepository`
- duplicate suspicion logging lives in `DuplicateSuspicionRepository`

### Domain boundaries

- public `RestroomRepository` and `BuildingRepository` interfaces are no longer polluted with import-only methods
- import-specific persistence is behind importing-local repositories

## What the subsystem supports well

- compact single and batch responses
- partial batch success
- exact provider-origin dedup
- deterministic building match-key dedup
- deterministic restroom match-key dedup
- nearby ambiguity logging without auto-merge
- inbox/audit row tracking in `restroom_imports`

## Remaining architectural debt

### 1. Import still runs inline in HTTP

The subsystem is cleaner, but it is still request/response import, not job-based ingestion.

Implication:
- large batches still compete with request timeouts and DB latency

### 2. Internal normalized command is still thin

`NormalizedImportCommand` currently aliases `NormalizedRestroomCandidate`.

Implication:
- provider-neutral flow exists, but the command model is still restroom-centric rather than a richer import command model

### 3. Import persistence reuses domain repository impl helpers

`RestroomImportRepositoryImpl` and `BuildingImportRepositoryImpl` currently reuse internal methods from:
- `repository/impl/RestroomRepositoryImpl.kt`
- `repository/impl/BuildingRepositoryImpl.kt`

Implication:
- boundary is clean at the interface level, but SQL ownership is not fully separated yet

### 4. Schedule normalization is still provider-shaped

Provider adapters preserve raw or semi-raw schedule information.

Implication:
- import architecture is clean, but schedule semantics are not yet unified across providers

## Recommended next steps

1. Move import execution to async jobs when batch size or latency starts to hurt
2. Replace the `NormalizedImportCommand` typealias with an explicit provider-agnostic command model
3. Pull remaining import SQL fully out of domain repository impls if that reuse starts creating coupling again
4. Normalize schedule handling behind one import-friendly schedule contract

## Test coverage now aligned with architecture

- adapter registry tests
- provider parser/normalizer tests
- adapter metadata extraction tests
- match key and payload hashing tests
- pipeline integration scenarios for:
  - partial batch success
  - repeated same-provider update
  - exact building match
  - exact restroom match
  - nearby suspicion logging
  - same-provider concurrency convergence
