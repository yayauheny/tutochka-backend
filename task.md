# Migration Parity Checklist

## Bot behavior parity

- [x] `/start`
  Current backend evidence: `backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/telegram/TelegramWebhookFlowIntegrationTest.kt`
  Reference: `bot/src/test/java/by/yayauheny/tutochkatgbot/integration/StartCommandIntegrationTest.java`
- [x] location search flow
  Current backend evidence: `backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/telegram/TelegramWebhookFlowIntegrationTest.kt`
  Reference: `bot/src/test/java/by/yayauheny/tutochkatgbot/integration/LocationSearchIntegrationTest.java`
- [x] nearest restroom search flow
  Current backend evidence: `backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/telegram/TelegramWebhookFlowIntegrationTest.kt`
  Reference: `bot/src/test/java/by/yayauheny/tutochkatgbot/integration/LocationSearchIntegrationTest.java`
- [x] restroom detail flow
  Current backend evidence: `backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/telegram/TelegramWebhookFlowIntegrationTest.kt`
  Reference: `bot/src/test/java/by/yayauheny/tutochkatgbot/integration/DetailFlowIntegrationTest.java`
- [x] callback flow
  Current backend evidence: `backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/telegram/TelegramWebhookFlowIntegrationTest.kt`
  Reference: `bot/src/test/java/by/yayauheny/tutochkatgbot/integration/DetailFlowIntegrationTest.java`, `bot/src/test/java/by/yayauheny/tutochkatgbot/integration/BackToListCacheIntegrationTest.java`
- [x] fallback/error replies
  Current backend evidence: `backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/telegram/TelegramWebhookFlowIntegrationTest.kt`
  Reference: `bot/src/test/java/by/yayauheny/tutochkatgbot/error/BotExceptionHandlerTest.java`, `bot/src/test/java/by/yayauheny/tutochkatgbot/handler/messages/PlainTextFallbackMessageHandlerTest.java`
- [x] analytics side effects
  Current backend evidence: `backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/telegram/TelegramWebhookFlowIntegrationTest.kt`, `backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/analytics/AnalyticsUserUpsertIntegrationTest.kt`
  Reference: `bot/src/test/java/by/yayauheny/tutochkatgbot/integration/BotMetricsIntegrationTest.java`, `bot/src/test/java/by/yayauheny/tutochkatgbot/integration/StartCommandIntegrationTest.java`
- [x] user creation/update side effects
  Current backend evidence: `backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/telegram/TelegramWebhookFlowIntegrationTest.kt`, `backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/analytics/AnalyticsUserUpsertIntegrationTest.kt`
  Reference: `bot/src/test/java/by/yayauheny/tutochkatgbot/integration/StartCommandIntegrationTest.java`
- [x] Telegram webhook accepts real update payloads
  Current backend evidence: `backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/telegram/TelegramWebhookFlowIntegrationTest.kt`, fixtures in `backend/src/test/resources/telegram/`
  Reference: `bot/src/test/java/by/yayauheny/tutochkatgbot/ingress/WebhookControllerTest.java`

## Import behavior parity

- [x] batch import request handling
  Current backend evidence: `backend/src/main/kotlin/yayauheny/by/importing/api/ImportController.kt`, `backend/src/test/kotlin/yayauheny/by/importing/api/ImportControllerTest.kt`, `backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/importing/ImportBatchApiIntegrationTest.kt`
  Reference: shared contract in `common/src/main/kotlin/yayauheny/by/model/import/*`, docs in `docs/architecture/IMPORT_ARCHITECTURE.md`
- [x] provider detection and validation
  Current backend evidence: adapter registry and capabilities tests
  Reference: `backend/src/test/kotlin/yayauheny/by/unit/service/import/ImportAdapterRegistryTest.kt`, `backend/src/test/kotlin/yayauheny/by/unit/service/import/ImportCapabilitiesTest.kt`
- [x] payload compatibility checks
  Current backend evidence: `backend/src/test/kotlin/yayauheny/by/importing/api/ImportControllerTest.kt`, `backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/importing/ImportBatchApiIntegrationTest.kt`
  Reference: `backend/src/test/kotlin/yayauheny/by/unit/importing/provider/*`
- [x] city resolution
  Current backend evidence: `backend/src/main/kotlin/yayauheny/by/importing/service/ImportCityResolver.kt`, `backend/src/test/kotlin/yayauheny/by/unit/importing/service/ImportCityResolverTest.kt`
  Reference: fixture metadata in `backend/src/test/resources/import/*`, seed cities migration `backend/src/main/resources/db/changelog/migration/20260515_seed_observed_countries_and_cities.sql`
- [x] building matching
  Current backend evidence: `backend/src/main/kotlin/yayauheny/by/importing/service/ImportPipeline.kt`, `backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/importing/ImportBatchApiIntegrationTest.kt`
  Reference: `docs/architecture/2026-05-13-2gis-import-hardening-adr.md`
- [x] restroom matching
  Current backend evidence: `backend/src/main/kotlin/yayauheny/by/importing/service/ImportPipeline.kt`, `backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/importing/ImportBatchApiIntegrationTest.kt`
  Reference: `docs/architecture/2026-05-13-2gis-import-hardening-adr.md`
- [x] dedup behavior when importing the same provider object again
  Current backend evidence: `backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/importing/ImportBatchApiIntegrationTest.kt`
  Reference: `docs/architecture/2026-05-13-2gis-import-hardening-adr.md`
- [x] update behavior when re-importing changed provider data
  Current backend evidence: `backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/importing/ImportBatchApiIntegrationTest.kt`
  Reference: `docs/architecture/IMPORT_AUDIT.md`
- [x] created/updated/duplicate/failed import result statuses
  Current backend evidence: `backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/importing/ImportBatchApiIntegrationTest.kt`
  Reference: `common/src/main/kotlin/yayauheny/by/model/import/ImportBatchResponseDto.kt`
- [x] partial success behavior for mixed valid/invalid batch items
  Current backend evidence: `backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/importing/ImportBatchApiIntegrationTest.kt`
  Reference: `docs/architecture/IMPORT_ARCHITECTURE.md`
- [x] error reporting
  Current backend evidence: `backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/importing/ImportBatchApiIntegrationTest.kt`, `backend/src/main/kotlin/yayauheny/by/common/errors/GlobalExceptionHandler.kt`
  Reference: `common/src/main/kotlin/yayauheny/by/model/import/ImportBatchResponseDto.kt`

## Phase gates

- [x] Port missing high-value bot parity tests into `backend`
- [x] Restore Spring import orchestration
- [x] Add focused import unit tests
- [x] Add focused import integration tests with real 2GIS and Yandex fixtures
- [x] Verify imported restroom business fields, not only row counts
- [x] Re-enable import API contract and docs after runtime works
- [x] Delete `bot/` only after backend parity tests and import tests are green
- [x] Run final full-suite verification
