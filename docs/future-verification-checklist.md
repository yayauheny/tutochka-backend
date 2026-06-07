# Future Verification Checklist

## 1. Empty search analytics vs user-visible "no toilets found"

- [ ] Reproduce at least one nearest-restroom search that returns an empty list through the same client path users use.
- [ ] Confirm the request hits `GET /api/v1/restrooms/nearest` in `backend/src/main/kotlin/yayauheny/by/controller/RestroomController.kt`.
- [ ] Verify the controller passes `resultsCount = 0` into `analyticsService.trackNearestRestroomsSearch(...)`.
- [ ] Verify `AnalyticsService.trackNearestRestroomsSearch(...)` maps zero results to `ProductAnalyticsEvent.NEAREST_RESTROOMS_NO_RESULTS` in `backend/src/main/kotlin/yayauheny/by/analytics/service/AnalyticsService.kt`.
- [ ] Verify `userAnalyticsJpaRepository.incrementMetrics(...)` increments `emptySearchesInc` for Telegram users in the same service.
- [ ] Verify whether the affected traffic is Telegram or anonymous API traffic. Anonymous API searches are stored as event rows, but do not update per-user counters because `emptySearchesInc` is tied to a resolved user row.
- [ ] Compare dashboard queries against both sources:
  `analytics_events.event = 'nearest_restrooms_no_results'`
  `user_analytics.empty_searches`
- [ ] Check whether the Grafana panel is reading the wrong field or filtering out `source = 'telegram_bot'` / `source = 'api'`.
- [ ] Review warning logs for swallowed analytics failures:
  `Failed to track nearest restroom analytics`
  `Failed to track analytics event`
- [ ] Add or keep an integration test that proves a zero-result nearest search increments the expected analytics signal for the intended client type.

## 2. Import verification, including dedup

- [ ] Run import with a known small fixture before bulk import.
- [ ] Verify the batch response contains expected item statuses from `backend/src/main/kotlin/yayauheny/by/model/import/ImportBatchResult.kt`:
  `CREATED`, `UPDATED`, duplicate-linked/skipped outcomes, and failures.
- [ ] Re-import the exact same payload and confirm it is treated as `SAME_PAYLOAD_HASH` in `backend/src/main/kotlin/yayauheny/by/importing/service/ImportBatchProcessor.kt`.
- [ ] Re-import the same physical restroom with a changed provider object id and confirm deterministic dedup links by `EXACT_RESTROOM_MATCH_KEY` in `backend/src/main/kotlin/yayauheny/by/importing/service/ImportPipeline.kt`.
- [ ] Verify nearby non-exact duplicates create rows in `restroom_duplicate_suspicions` through `DuplicateSuspicionJpaRepository`.
- [ ] Verify `restroom_imports.payload_hash` is populated and stable for semantically identical payloads.
- [ ] Verify imported restroom rows preserve business fields, not only row counts.
- [ ] Verify city resolution failures are visible in batch results and persisted `error_message` values.
- [ ] Check these existing tests before changing import behavior:
  `backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/importing/ImportBatchApiIntegrationTest.kt`
  `backend/src/test/kotlin/yayauheny/by/unit/importing/dedup/PayloadHashingTest.kt`
  `backend/src/test/kotlin/yayauheny/by/unit/importing/dedup/MatchKeyGeneratorTest.kt`
- [ ] Re-read the current import rules before changing dedup:
  `docs/architecture/2026-05-13-2gis-import-hardening-adr.md`
  `docs/architecture/IMPORT_ARCHITECTURE.md`
  `docs/architecture/IMPORT_AUDIT.md`

## 3. Find the source of log errors

- [ ] Check the active backend log file path from `backend/src/main/resources/logback.xml`:
  `${BACKEND_LOG_DIR}/backend.log`
- [ ] Inspect both current and rotated logs because the failing request may already be archived under `${BACKEND_LOG_DIR}/archive/`.
- [ ] Search for `ERROR` and `WARN` around the incident timestamp first, then group by recurring message.
- [ ] Prioritize these known high-signal messages:
  `Unhandled exception`
  `Failed to track nearest restroom analytics`
  `Failed to track analytics event`
  `Failed to register webhook`
- [ ] For import incidents, cross-check backend logs with `restroom_imports.error_message` and failed batch response items.
- [ ] For search incidents, correlate log timestamps with `analytics_events`, search metrics, and user-reported coordinates.
- [ ] Verify whether the error is application-level, webhook-related, import-related, or infrastructure-level.
- [ ] If the same error repeats, capture one representative stack trace, the triggering request, and the affected entity ids before changing code.
- [ ] After fixing a log error, add or update the narrowest regression test that reproduces the original failure.
