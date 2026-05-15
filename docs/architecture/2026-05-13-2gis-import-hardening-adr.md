# ADR-0002: 2GIS import hardening and deterministic dedup v1

Date: 2026-05-13

## Status

Accepted

## Context

The 2GIS scraped import flow needed to support repeated batch imports without turning duplicates, transient bad records, or concurrent retries into broken writes.

Before this change the import path had several problems:

- batch execution was effectively fail-fast
- the whole batch could be coupled to one transaction
- same-provider imports relied on `find -> save/update` sequences that were race-prone
- import audit rows were not used as a real inbox
- duplicate handling depended on per-item lookups instead of bounded bulk prefetch
- ambiguous nearby matches had no explicit review trail

We needed a first pass that is safe and deterministic, but still simple:

- bounded batch size
- partial success for syntactically valid batches
- atomic same-provider idempotency
- exact-match dedup for buildings and restrooms
- logging for ambiguous nearby duplicates without auto-merge

## Decision

We hardened the 2GIS import pipeline around chunked execution, inbox upserts, and deterministic dedup keys.

### Batch execution

- Batch import is capped at 200 items.
- Processing runs in chunks of 50 items.
- Each chunk uses its own transaction.
- A valid batch returns per-item outcomes instead of failing the whole request on the first bad item.
- Per-item results expose created, updated, linked-duplicate, skipped-duplicate, and failed outcomes.

### Import inbox

- `restroom_imports` is treated as the import inbox and audit table.
- Inbox rows are keyed by `(provider, entity_type, external_id)`.
- Inbox writes use `ON CONFLICT DO UPDATE` so repeated imports refresh the same row.
- `payload_hash` is derived from canonicalized raw provider JSON so semantically identical payloads hash the same way regardless of field order.

### Atomic idempotency

- Same-provider persistence paths use single-statement upserts instead of `findByOrigin -> save/update`.
- Repository APIs support bulk lookup by origin, external map, and deterministic match key.
- This keeps concurrent imports of the same provider object race-safe.

### Deterministic dedup

- Building resolution priority is:
  - same provider external building id
  - existing external id link
  - exact `building_match_key`
- Restroom resolution priority is:
  - same `(origin_provider, origin_id)`
  - existing external map/provider link
  - exact `restroom_match_key`
- Match keys are generated only when the required normalized fields are present.
- Normalization is used only for keys and matching, not for raw payload storage.

### Ambiguous duplicates

- If no exact restroom match exists, the import may search for nearby candidates in the same city within 15 meters.
- Nearby candidates are not auto-merged.
- The new restroom is inserted separately and a `restroom_duplicate_suspicions` row is created with `PENDING` status for later review.

## Consequences

### Positive

- Repeated 2GIS imports are bounded and safe to rerun.
- Partial failures no longer discard successful items from the same valid batch.
- Concurrent retries of the same provider object converge on one imported restroom.
- Deterministic dedup is cheap to prefetch in bulk and predictable to reason about.
- Ambiguous duplicate cases are preserved for review without blocking import throughput.

### Tradeoffs

- This pass only handles high-confidence exact matches and intentionally avoids fuzzy auto-merge.
- Chunk-level transactions still mean several items share one transaction boundary, even though individual item failures are converted into response results.
- Suspicion logging adds review data, but there is no moderation workflow in this pass.

## Verification

The implementation is covered by focused unit and integration tests for:

- canonical JSON hash stability across field order
- deterministic normalization
- suppression of match-key generation when required fields are missing
- mixed-validity batches committing successful items and returning item failures
- repeated same-provider imports updating one restroom
- inbox row collapse by unique key
- exact building and restroom duplicate linking
- ambiguous nearby duplicate suspicion creation
- concurrent same-provider imports resolving to one restroom
