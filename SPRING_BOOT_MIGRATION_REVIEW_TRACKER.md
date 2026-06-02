# Spring Boot Migration Review Tracker

## Goal

Review the whole project after the Ktor -> Spring Boot migration layer by layer and confirm:

- business behavior is still correct
- Spring Boot wiring is correct
- there are no obvious code errors or migration leftovers
- each layer has adequate tests

## Review Workflow

Use this file as the working checklist.

1. Start from layer 1 and review in order.
2. For each layer, check code, related tests, and migration risks.
3. Mark the layer when review is finished.
4. Record findings directly under the layer section.

## Layer Checklist

- [x] 1. Build, bootstrap, and runtime entrypoints
- [x] 2. API and web layer
- [x] 3. Core service layer
- [x] 4. DTOs, request models, response models, and contracts
- [x] 5. Repository and query layer
- [ ] 6. Entity and database mapping layer
- [ ] 7. Importing subsystem
- [ ] 8. Telegram bot subsystem
- [ ] 9. Analytics subsystem
- [ ] 10. Shared infrastructure, configuration, errors, utilities, and metrics
- [ ] 11. Database migrations and runtime resources
- [ ] 12. Test infrastructure and migration leftovers

## 1. Build, Bootstrap, and Runtime Entrypoints

### Review Focus

- Gradle modules and dependencies are correct for Spring Boot
- application entrypoint and startup wiring are correct
- no Ktor bootstrap leftovers remain in production code paths

### Files

```text
build.gradle.kts
settings.gradle.kts
gradle.properties
libs.versions.toml
backend/build.gradle.kts
common/build.gradle.kts
backend/src/main/kotlin/yayauheny/by/Application.kt
backend/src/main/resources/application.yml
backend/src/main/resources/logback.xml
backend/Dockerfile
docker/docker-compose.yml
docker/docker-compose-local.yml
docker/docker-compose.prod.yml
docker/docker-compose-debug.yml
docker/Caddyfile
```

### Notes

- Findings:
  Docker packaging still referenced Ktor-era outputs before the fix:
  `backend/Dockerfile` used `:backend:buildFatJar` and copied `backend-all.jar`, but the Spring Boot project exposes `bootJar` and produces `app.jar` after the Gradle fix in `backend/build.gradle.kts`.
- Questions:

## 2. API and Web Layer

### Review Focus

- controllers are thin
- request validation and HTTP mapping are correct
- status codes and error contracts are consistent
- migrated endpoints behave the same as before

### Main Files

```text
backend/src/main/kotlin/yayauheny/by/controller/CityController.kt
backend/src/main/kotlin/yayauheny/by/controller/CountryController.kt
backend/src/main/kotlin/yayauheny/by/controller/HealthController.kt
backend/src/main/kotlin/yayauheny/by/controller/RestroomController.kt
backend/src/main/kotlin/yayauheny/by/controller/TelegramWebhookController.kt
backend/src/main/kotlin/yayauheny/by/analytics/api/AnalyticsController.kt
backend/src/main/kotlin/yayauheny/by/analytics/api/PublicAnalyticsController.kt
backend/src/main/kotlin/yayauheny/by/importing/api/ImportController.kt
```

### Related Tests

```text
backend/src/test/kotlin/yayauheny/by/unit/controller/AnalyticsControllerTest.kt
backend/src/test/kotlin/yayauheny/by/unit/controller/CityControllerTest.kt
backend/src/test/kotlin/yayauheny/by/unit/controller/CountryControllerTest.kt
backend/src/test/kotlin/yayauheny/by/unit/controller/HealthControllerTest.kt
backend/src/test/kotlin/yayauheny/by/unit/controller/HttpStatusCodesTest.kt
backend/src/test/kotlin/yayauheny/by/unit/controller/PublicAnalyticsControllerTest.kt
backend/src/test/kotlin/yayauheny/by/unit/controller/RestroomControllerTest.kt
backend/src/test/kotlin/yayauheny/by/unit/controller/RoutingTestBase.kt
backend/src/test/kotlin/yayauheny/by/controller/TelegramWebhookControllerTest.kt
backend/src/test/kotlin/yayauheny/by/importing/api/ImportControllerTest.kt
backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/analytics/AnalyticsApiTest.kt
backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/analytics/AnalyticsUserUpsertIntegrationTest.kt
backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/analytics/PublicAnalyticsApiTest.kt
backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/cities/CityApiTest.kt
backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/countries/CountryApiTest.kt
backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/error/PSQLExceptionHandlingTest.kt
backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/importing/ImportBatchApiIntegrationTest.kt
backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/restrooms/RestroomApiTest.kt
backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/restrooms/RestroomMetricsIntegrationTest.kt
backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/telegram/TelegramWebhookFlowIntegrationTest.kt
```

### Notes

- Findings:
  `backend/src/main/kotlin/yayauheny/by/controller/RestroomController.kt` defaulted nearest-search analytics to `TELEGRAM_BOT` whenever the `X-Analytics-Source` header was missing. That misattributed plain API traffic and blocked anonymous nearest-search tracking, because anonymous analytics are persisted only for `API` source. Fixed by resolving the default source from available Telegram identity: Telegram requests stay `TELEGRAM_BOT`, otherwise the endpoint now defaults to `API`.
- Questions:

## 3. Core Service Layer

### Review Focus

- business logic sits in services, not controllers
- transactional boundaries and orchestration are correct
- service interfaces and implementations are consistent

### Main Files

```text
backend/src/main/kotlin/yayauheny/by/service/BotUpdateService.kt
backend/src/main/kotlin/yayauheny/by/service/BotUpdateServiceImpl.kt
backend/src/main/kotlin/yayauheny/by/service/CityService.kt
backend/src/main/kotlin/yayauheny/by/service/CountryService.kt
backend/src/main/kotlin/yayauheny/by/service/RestroomService.kt
backend/src/main/kotlin/yayauheny/by/service/import/schedule/ScheduleAdapter.kt
backend/src/main/kotlin/yayauheny/by/service/import/schedule/ScheduleMappingService.kt
backend/src/main/kotlin/yayauheny/by/service/import/schedule/TwoGisScheduleAdapter.kt
```

### Related Tests

```text
backend/src/test/kotlin/yayauheny/by/unit/service/BotUpdateServiceImplTest.kt
backend/src/test/kotlin/yayauheny/by/unit/service/CityServiceTest.kt
backend/src/test/kotlin/yayauheny/by/unit/service/CountryServiceTest.kt
backend/src/test/kotlin/yayauheny/by/unit/service/RestroomServiceTest.kt
backend/src/test/kotlin/yayauheny/by/unit/service/ValidationTest.kt
backend/src/test/kotlin/yayauheny/by/unit/service/EdgeCaseValidationTest.kt
backend/src/test/kotlin/yayauheny/by/service/import/schedule/TwoGisScheduleAdapterTest.kt
backend/src/test/kotlin/yayauheny/by/unit/service/import/schedule/ScheduleMappingServiceTest.kt
```

### Notes

- Findings:
  `backend/src/main/kotlin/yayauheny/by/service/CityService.kt` allowed `updateCity` to bypass the duplicate-name checks that already exist in `createCity`, even though the database enforces unique `(country_id, name_ru)` and `(country_id, name_en)` constraints. That meant a normal business conflict would surface as a lower-level persistence failure instead of a service-level `ConflictException`. Fixed by validating duplicate Russian and English city names before saving.
- Questions:

## 4. DTOs, Request Models, Response Models, and Contracts

### Review Focus

- DTO contracts match controller and service usage
- serialized field names and nullability are correct
- shared `common` module still matches backend expectations

### Main Files

```text
common/src/main/kotlin/yayauheny/by/contract/BackendClient.kt
common/src/main/kotlin/yayauheny/by/model/analytics/AcquisitionTag.kt
common/src/main/kotlin/yayauheny/by/model/analytics/AnalyticsEventCommand.kt
common/src/main/kotlin/yayauheny/by/model/analytics/AnalyticsEventRequest.kt
common/src/main/kotlin/yayauheny/by/model/analytics/AnalyticsSource.kt
common/src/main/kotlin/yayauheny/by/model/analytics/ProductAnalyticsEvent.kt
common/src/main/kotlin/yayauheny/by/model/building/BuildingCreateDto.kt
common/src/main/kotlin/yayauheny/by/model/building/BuildingResponseDto.kt
common/src/main/kotlin/yayauheny/by/model/building/BuildingUpdateDto.kt
common/src/main/kotlin/yayauheny/by/model/city/CityCreateDto.kt
common/src/main/kotlin/yayauheny/by/model/city/CityResponseDto.kt
common/src/main/kotlin/yayauheny/by/model/city/CityUpdateDto.kt
common/src/main/kotlin/yayauheny/by/model/country/CountryCreateDto.kt
common/src/main/kotlin/yayauheny/by/model/country/CountryResponseDto.kt
common/src/main/kotlin/yayauheny/by/model/country/CountryUpdateDto.kt
common/src/main/kotlin/yayauheny/by/model/dto/Coordinates.kt
common/src/main/kotlin/yayauheny/by/model/enums/AccessibilityType.kt
common/src/main/kotlin/yayauheny/by/model/enums/BuildingImportStatus.kt
common/src/main/kotlin/yayauheny/by/model/enums/DataSourceType.kt
common/src/main/kotlin/yayauheny/by/model/enums/FeeType.kt
common/src/main/kotlin/yayauheny/by/model/enums/GenderType.kt
common/src/main/kotlin/yayauheny/by/model/enums/ImportJobStatus.kt
common/src/main/kotlin/yayauheny/by/model/enums/ImportPayloadType.kt
common/src/main/kotlin/yayauheny/by/model/enums/ImportProvider.kt
common/src/main/kotlin/yayauheny/by/model/enums/LocationType.kt
common/src/main/kotlin/yayauheny/by/model/enums/PlaceType.kt
common/src/main/kotlin/yayauheny/by/model/enums/RestroomStatus.kt
common/src/main/kotlin/yayauheny/by/model/import/ImportBatchResponseDto.kt
common/src/main/kotlin/yayauheny/by/model/import/ImportRequestDto.kt
common/src/main/kotlin/yayauheny/by/model/import/ImportResponseDto.kt
common/src/main/kotlin/yayauheny/by/model/restroom/NearestRestroomSlimDto.kt
common/src/main/kotlin/yayauheny/by/model/restroom/RestroomCreateDto.kt
common/src/main/kotlin/yayauheny/by/model/restroom/RestroomResponseDto.kt
common/src/main/kotlin/yayauheny/by/model/restroom/RestroomUpdateDto.kt
common/src/main/kotlin/yayauheny/by/model/subway/SubwayLineCreateDto.kt
common/src/main/kotlin/yayauheny/by/model/subway/SubwayLineResponseDto.kt
common/src/main/kotlin/yayauheny/by/model/subway/SubwayStationCreateDto.kt
common/src/main/kotlin/yayauheny/by/model/subway/SubwayStationResponseDto.kt
backend/src/main/kotlin/yayauheny/by/analytics/model/AnalyticsIdentityHeaders.kt
backend/src/main/kotlin/yayauheny/by/analytics/model/EncryptedAnalyticsEventCommand.kt
backend/src/main/kotlin/yayauheny/by/analytics/model/PublicAnalyticsModels.kt
backend/src/main/kotlin/yayauheny/by/common/query/PageResponse.kt
backend/src/main/kotlin/yayauheny/by/common/query/PaginationRequest.kt
backend/src/main/kotlin/yayauheny/by/common/query/SortDirection.kt
backend/src/main/kotlin/yayauheny/by/importing/model/ImportAdapterContext.kt
backend/src/main/kotlin/yayauheny/by/importing/model/ImportBatchSummary.kt
backend/src/main/kotlin/yayauheny/by/importing/model/ImportEntityType.kt
backend/src/main/kotlin/yayauheny/by/importing/model/ImportOriginKey.kt
backend/src/main/kotlin/yayauheny/by/importing/model/ImportPipelineResult.kt
backend/src/main/kotlin/yayauheny/by/importing/model/InboxMetadata.kt
backend/src/main/kotlin/yayauheny/by/importing/model/NormalizedImportCommand.kt
backend/src/main/kotlin/yayauheny/by/importing/model/SourceLocationHint.kt
backend/src/main/kotlin/yayauheny/by/model/city/CityFilter.kt
backend/src/main/kotlin/yayauheny/by/model/country/CountryFilter.kt
backend/src/main/kotlin/yayauheny/by/model/enums/TwoGisAttributeGroup.kt
backend/src/main/kotlin/yayauheny/by/model/enums/TwoGisCategory.kt
backend/src/main/kotlin/yayauheny/by/model/enums/TwoGisRubric.kt
backend/src/main/kotlin/yayauheny/by/model/import/ImportBatchResult.kt
backend/src/main/kotlin/yayauheny/by/model/import/ImportExecutionResult.kt
backend/src/main/kotlin/yayauheny/by/model/import/NormalizedRestroomCandidate.kt
backend/src/main/kotlin/yayauheny/by/model/restroom/RestroomFilter.kt
backend/src/main/kotlin/yayauheny/by/model/schedule/DaySchedule.kt
backend/src/main/kotlin/yayauheny/by/model/schedule/Schedule.kt
backend/src/main/kotlin/yayauheny/by/model/schedule/Weekday.kt
backend/src/main/kotlin/yayauheny/by/model/schedule/WorkingInterval.kt
```

### Related Tests

```text
common/src/test/kotlin/yayauheny/by/model/ContractSerializationTest.kt
backend/src/test/kotlin/yayauheny/by/model/schedule/ScheduleUtilsTest.kt
```

### Notes

- Findings:
  `common/src/main/kotlin/yayauheny/by/model/restroom/RestroomUpdateDto.kt` encoded update requests inconsistently: several fields behaved like patch fields (`name`, `address`, relations), but `status` and `coordinates` were mandatory and `inheritBuildingSchedule` / `hasPhotos` defaulted to `false`. That let omitted JSON silently overwrite existing restroom state during updates. Fixed by making those update fields nullable and letting the service update them only when explicitly provided.
  `common/src/main/kotlin/yayauheny/by/model/country/CountryUpdateDto.kt` had no validation annotations, so blank country names could pass update requests even though create requests reject them. Fixed by adding the same `NotBlank`/`Size` constraints on update DTO fields.
- Questions:

## 5. Repository and Query Layer

### Review Focus

- Spring Data repositories are correctly defined
- custom repositories and Querydsl usage are correct
- filtering, pagination, sorting, and query composition behave correctly

### Main Files

```text
backend/src/main/kotlin/yayauheny/by/repository/BuildingJpaRepository.kt
backend/src/main/kotlin/yayauheny/by/repository/CityFilterRepository.kt
backend/src/main/kotlin/yayauheny/by/repository/CityFilterRepositoryImpl.kt
backend/src/main/kotlin/yayauheny/by/repository/CityJpaRepository.kt
backend/src/main/kotlin/yayauheny/by/repository/CountryFilterRepository.kt
backend/src/main/kotlin/yayauheny/by/repository/CountryFilterRepositoryImpl.kt
backend/src/main/kotlin/yayauheny/by/repository/CountryJpaRepository.kt
backend/src/main/kotlin/yayauheny/by/repository/RestroomFilterRepository.kt
backend/src/main/kotlin/yayauheny/by/repository/RestroomFilterRepositoryImpl.kt
backend/src/main/kotlin/yayauheny/by/repository/RestroomJpaRepository.kt
backend/src/main/kotlin/yayauheny/by/repository/SubwayLineJpaRepository.kt
backend/src/main/kotlin/yayauheny/by/repository/SubwayStationJpaRepository.kt
backend/src/main/kotlin/yayauheny/by/analytics/repository/AnalyticsEventJpaRepository.kt
backend/src/main/kotlin/yayauheny/by/analytics/repository/PublicAnalyticsRepository.kt
backend/src/main/kotlin/yayauheny/by/analytics/repository/PublicAnalyticsRepositoryImpl.kt
backend/src/main/kotlin/yayauheny/by/analytics/repository/UserAnalyticsJpaRepository.kt
backend/src/main/kotlin/yayauheny/by/analytics/repository/UserJpaRepository.kt
backend/src/main/kotlin/yayauheny/by/importing/repository/DuplicateSuspicionJpaRepository.kt
backend/src/main/kotlin/yayauheny/by/importing/repository/RestroomImportJpaRepository.kt
backend/src/main/kotlin/yayauheny/by/common/querydsl/QuerydslPageSupport.kt
backend/src/main/kotlin/yayauheny/by/common/querydsl/QuerydslPredicates.kt
backend/src/main/kotlin/yayauheny/by/common/querydsl/QuerydslSortMapper.kt
backend/src/main/kotlin/yayauheny/by/util/PaginationUtils.kt
```

### Related Tests

```text
backend/src/test/kotlin/yayauheny/by/unit/util/PaginationEdgeCaseTest.kt
backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/cities/CityApiTest.kt
backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/countries/CountryApiTest.kt
backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/restrooms/RestroomApiTest.kt
backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/analytics/PublicAnalyticsApiTest.kt
backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/analytics/AnalyticsUserUpsertIntegrationTest.kt
backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/importing/ImportBatchApiIntegrationTest.kt
```

### Notes

- Findings:
  `backend/src/main/kotlin/yayauheny/by/repository/RestroomFilterRepositoryImpl.kt` did not support filtering by `originId`, even though `restrooms.origin_id` is indexed, `RestroomEntity` exposes it, import flows populate it, and the repository already allowed sorting by it. That made imported-record lookups incomplete through the normal filtered listing API. Fixed by adding `originId` to `RestroomFilter` and applying an exact-match predicate in the repository filter query.
  `backend/src/main/kotlin/yayauheny/by/analytics/repository/AnalyticsEventJpaRepository.kt` still exposed unused custom count methods with stale semantics (`SEARCH_COMPLETED` event name and `UserEntity.createdAt` as “active users”). They were not called anywhere, but leaving them in the repository API made it easy for future service code to depend on incorrect analytics queries. Fixed by removing the dead custom query methods and keeping the repository focused on the entity operations that are actually used.
- Questions:

## 6. Entity and Database Mapping Layer

### Review Focus

- JPA entities match the database schema
- relationships, converters, IDs, timestamps, and constraints are correct
- migration from former persistence mapping did not break domain assumptions

### Main Files

```text
backend/src/main/kotlin/yayauheny/by/entity/AccessibilityTypeJpaConverter.kt
backend/src/main/kotlin/yayauheny/by/entity/BaseEntity.kt
backend/src/main/kotlin/yayauheny/by/entity/BuildingEntity.kt
backend/src/main/kotlin/yayauheny/by/entity/CityEntity.kt
backend/src/main/kotlin/yayauheny/by/entity/CountryEntity.kt
backend/src/main/kotlin/yayauheny/by/entity/DuplicateSuspicionEntity.kt
backend/src/main/kotlin/yayauheny/by/entity/PlaceTypeJpaConverter.kt
backend/src/main/kotlin/yayauheny/by/entity/RestroomEntity.kt
backend/src/main/kotlin/yayauheny/by/entity/RestroomImportEntity.kt
backend/src/main/kotlin/yayauheny/by/entity/SubwayLineEntity.kt
backend/src/main/kotlin/yayauheny/by/entity/SubwayStationEntity.kt
backend/src/main/kotlin/yayauheny/by/analytics/entity/AnalyticsEventEntity.kt
backend/src/main/kotlin/yayauheny/by/analytics/entity/UserAnalyticsEntity.kt
backend/src/main/kotlin/yayauheny/by/analytics/entity/UserEntity.kt
```

### Related Tests

```text
backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/error/PSQLExceptionHandlingTest.kt
backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/analytics/AnalyticsUserUpsertIntegrationTest.kt
backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/importing/ImportBatchApiIntegrationTest.kt
backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/restrooms/RestroomApiTest.kt
```

### Notes

- Findings:
  `backend/src/main/kotlin/yayauheny/by/entity/AccessibilityTypeJpaConverter.kt` normalized incoming database values for matching, but still called `AccessibilityType.valueOf(...)` on the original raw string. That meant lowercase or padded values such as `"wheelchair"` and `" wheelchair "` deserialized to `UNKNOWN` instead of the correct enum, even though the converter was clearly intended to tolerate normalization. Fixed by looking up the enum from the normalized value and covering both normalized and legacy `"disabled"` inputs with a unit test.
- Questions:

## 7. Importing Subsystem

### Review Focus

- provider adapters and normalization flow are correct
- import orchestration is clean and deterministic
- deduplication and matching logic are safe
- controller, services, mapper, repositories, and models still fit together after migration

### Main Files

```text
backend/src/main/kotlin/yayauheny/by/importing/api/ImportController.kt
backend/src/main/kotlin/yayauheny/by/importing/dedup/MatchKeyGenerator.kt
backend/src/main/kotlin/yayauheny/by/importing/dedup/PayloadHashing.kt
backend/src/main/kotlin/yayauheny/by/importing/dedup/ProviderKeyResolver.kt
backend/src/main/kotlin/yayauheny/by/importing/exception/ImportException.kt
backend/src/main/kotlin/yayauheny/by/importing/mapper/RestroomImportMapper.kt
backend/src/main/kotlin/yayauheny/by/importing/model/ImportAdapterContext.kt
backend/src/main/kotlin/yayauheny/by/importing/model/ImportBatchSummary.kt
backend/src/main/kotlin/yayauheny/by/importing/model/ImportEntityType.kt
backend/src/main/kotlin/yayauheny/by/importing/model/ImportOriginKey.kt
backend/src/main/kotlin/yayauheny/by/importing/model/ImportPipelineResult.kt
backend/src/main/kotlin/yayauheny/by/importing/model/InboxMetadata.kt
backend/src/main/kotlin/yayauheny/by/importing/model/NormalizedImportCommand.kt
backend/src/main/kotlin/yayauheny/by/importing/model/SourceLocationHint.kt
backend/src/main/kotlin/yayauheny/by/importing/provider/ImportCapabilities.kt
backend/src/main/kotlin/yayauheny/by/importing/provider/ImportSourceAdapter.kt
backend/src/main/kotlin/yayauheny/by/importing/provider/JsonObjectImportExtensions.kt
backend/src/main/kotlin/yayauheny/by/importing/provider/Parser.kt
backend/src/main/kotlin/yayauheny/by/importing/provider/ProviderNormalizationProfile.kt
backend/src/main/kotlin/yayauheny/by/importing/provider/osm/OsmImportAdapter.kt
backend/src/main/kotlin/yayauheny/by/importing/provider/osm/OsmNormalizationProfile.kt
backend/src/main/kotlin/yayauheny/by/importing/provider/osm/OsmPayloadTags.kt
backend/src/main/kotlin/yayauheny/by/importing/provider/osm/OsmScrapedModels.kt
backend/src/main/kotlin/yayauheny/by/importing/provider/osm/OsmScrapedParser.kt
backend/src/main/kotlin/yayauheny/by/importing/provider/twogis/TwoGisImportAdapter.kt
backend/src/main/kotlin/yayauheny/by/importing/provider/twogis/TwoGisNormalizationProfile.kt
backend/src/main/kotlin/yayauheny/by/importing/provider/twogis/TwoGisScrapedModels.kt
backend/src/main/kotlin/yayauheny/by/importing/provider/twogis/TwoGisScrapedParser.kt
backend/src/main/kotlin/yayauheny/by/importing/provider/yandex/YandexImportAdapter.kt
backend/src/main/kotlin/yayauheny/by/importing/provider/yandex/YandexMapsScrapedModels.kt
backend/src/main/kotlin/yayauheny/by/importing/provider/yandex/YandexMapsScrapedParser.kt
backend/src/main/kotlin/yayauheny/by/importing/provider/yandex/YandexNormalizationProfile.kt
backend/src/main/kotlin/yayauheny/by/importing/repository/DuplicateSuspicionJpaRepository.kt
backend/src/main/kotlin/yayauheny/by/importing/repository/RestroomImportJpaRepository.kt
backend/src/main/kotlin/yayauheny/by/importing/service/ImportAdapterRegistry.kt
backend/src/main/kotlin/yayauheny/by/importing/service/ImportBatchProcessor.kt
backend/src/main/kotlin/yayauheny/by/importing/service/ImportCityResolver.kt
backend/src/main/kotlin/yayauheny/by/importing/service/ImportPipeline.kt
backend/src/main/kotlin/yayauheny/by/importing/service/ImportService.kt
backend/src/main/kotlin/yayauheny/by/model/import/ImportBatchResult.kt
backend/src/main/kotlin/yayauheny/by/model/import/ImportExecutionResult.kt
backend/src/main/kotlin/yayauheny/by/model/import/NormalizedRestroomCandidate.kt
backend/src/main/kotlin/yayauheny/by/model/enums/TwoGisAttributeGroup.kt
backend/src/main/kotlin/yayauheny/by/model/enums/TwoGisCategory.kt
backend/src/main/kotlin/yayauheny/by/model/enums/TwoGisRubric.kt
backend/src/main/kotlin/yayauheny/by/service/import/schedule/ScheduleAdapter.kt
backend/src/main/kotlin/yayauheny/by/service/import/schedule/ScheduleMappingService.kt
backend/src/main/kotlin/yayauheny/by/service/import/schedule/TwoGisScheduleAdapter.kt
```

### Related Tests

```text
backend/src/test/kotlin/yayauheny/by/importing/api/ImportControllerTest.kt
backend/src/test/kotlin/yayauheny/by/unit/importing/dedup/MatchKeyGeneratorTest.kt
backend/src/test/kotlin/yayauheny/by/unit/importing/dedup/PayloadHashingTest.kt
backend/src/test/kotlin/yayauheny/by/unit/importing/provider/ImportAdapterMetadataTest.kt
backend/src/test/kotlin/yayauheny/by/unit/importing/provider/osm/OsmNormalizationProfileTest.kt
backend/src/test/kotlin/yayauheny/by/unit/importing/provider/osm/OsmScrapedParserTest.kt
backend/src/test/kotlin/yayauheny/by/unit/importing/provider/twogis/TwoGisNormalizationProfileTest.kt
backend/src/test/kotlin/yayauheny/by/unit/importing/provider/twogis/TwoGisScrapedParserDatasetTest.kt
backend/src/test/kotlin/yayauheny/by/unit/importing/provider/yandex/YandexMapsScrapedParserTest.kt
backend/src/test/kotlin/yayauheny/by/unit/importing/provider/yandex/YandexNormalizationProfileTest.kt
backend/src/test/kotlin/yayauheny/by/unit/importing/service/ImportCityResolverTest.kt
backend/src/test/kotlin/yayauheny/by/unit/service/import/ImportAdapterRegistryTest.kt
backend/src/test/kotlin/yayauheny/by/unit/service/import/ImportCapabilitiesTest.kt
backend/src/test/kotlin/yayauheny/by/unit/service/import/RestroomImportMapperTest.kt
backend/src/test/kotlin/yayauheny/by/unit/service/import/schedule/ScheduleMappingServiceTest.kt
backend/src/test/kotlin/yayauheny/by/unit/service/import/twogis/TwoGisScrapedNormalizerTest.kt
backend/src/test/kotlin/yayauheny/by/unit/service/import/twogis/TwoGisScrapedParserTest.kt
backend/src/test/kotlin/yayauheny/by/unit/service/import/yandex/YandexMapsScrapedNormalizerTest.kt
backend/src/test/kotlin/yayauheny/by/service/import/schedule/TwoGisScheduleAdapterTest.kt
backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/importing/ImportBatchApiIntegrationTest.kt
backend/src/test/kotlin/yayauheny/by/testfixtures/OsmOriginalRows.kt
backend/src/test/kotlin/yayauheny/by/helpers/ImportTestResourceHelper.kt
```

### Notes

- Findings:
  `backend/src/main/kotlin/yayauheny/by/importing/service/ImportPipeline.kt` reused create-style field assignment when re-importing an existing restroom. That reset fields the import payload does not own, including moderation state (`isHidden`, `hiddenReason`, `hiddenAt`), manual notes, manual subway linkage, phones, and the `hasPhotos` flag. A normal provider refresh could therefore silently unhide or strip manual enrichment from an existing restroom. Fixed by splitting imported field application so update flows refresh import-owned data without wiping manual or moderation-managed fields.
- Questions:

## 8. Telegram Bot Subsystem

### Review Focus

- webhook, routing, handlers, sender, and formatting flow are correct
- bot logic is not leaking transport details into the wrong layer
- Spring Boot integration for webhook processing is complete

### Main Files

```text
backend/src/main/kotlin/yayauheny/by/controller/TelegramWebhookController.kt
backend/src/main/kotlin/yayauheny/by/service/BotUpdateService.kt
backend/src/main/kotlin/yayauheny/by/service/BotUpdateServiceImpl.kt
backend/src/main/kotlin/yayauheny/by/bot/MessageSender.kt
backend/src/main/kotlin/yayauheny/by/bot/NoOpMessageSender.kt
backend/src/main/kotlin/yayauheny/by/bot/TelegramMessageSender.kt
backend/src/main/kotlin/yayauheny/by/bot/cache/BackListSnapshotCache.kt
backend/src/main/kotlin/yayauheny/by/bot/callback/CallbackData.kt
backend/src/main/kotlin/yayauheny/by/bot/handler/CallbackHandler.kt
backend/src/main/kotlin/yayauheny/by/bot/handler/CommandHandler.kt
backend/src/main/kotlin/yayauheny/by/bot/handler/MessageHandler.kt
backend/src/main/kotlin/yayauheny/by/bot/handler/UpdateContext.kt
backend/src/main/kotlin/yayauheny/by/bot/handler/callbacks/BackToListCallback.kt
backend/src/main/kotlin/yayauheny/by/bot/handler/callbacks/RadiusCallback.kt
backend/src/main/kotlin/yayauheny/by/bot/handler/callbacks/ToiletDetailCallback.kt
backend/src/main/kotlin/yayauheny/by/bot/handler/commands/HelpCommand.kt
backend/src/main/kotlin/yayauheny/by/bot/handler/commands/StartCommand.kt
backend/src/main/kotlin/yayauheny/by/bot/handler/messages/LocationMessageHandler.kt
backend/src/main/kotlin/yayauheny/by/bot/handler/messages/PlainTextFallbackMessageHandler.kt
backend/src/main/kotlin/yayauheny/by/bot/keyboard/InlineKeyboardFactory.kt
backend/src/main/kotlin/yayauheny/by/bot/keyboard/ReplyKeyboardFactory.kt
backend/src/main/kotlin/yayauheny/by/bot/messages/Messages.kt
backend/src/main/kotlin/yayauheny/by/bot/metrics/BotMetricLabelWhitelist.kt
backend/src/main/kotlin/yayauheny/by/bot/metrics/BotMetrics.kt
backend/src/main/kotlin/yayauheny/by/bot/router/UpdateRouter.kt
backend/src/main/kotlin/yayauheny/by/bot/service/FormatterService.kt
backend/src/main/kotlin/yayauheny/by/bot/service/UpdateHandlingService.kt
backend/src/main/kotlin/yayauheny/by/bot/service/UserService.kt
backend/src/main/kotlin/yayauheny/by/bot/session/UserSession.kt
backend/src/main/kotlin/yayauheny/by/bot/util/CommandUtils.kt
backend/src/main/kotlin/yayauheny/by/bot/util/EmojiConstants.kt
backend/src/main/kotlin/yayauheny/by/bot/util/Links.kt
backend/src/main/kotlin/yayauheny/by/bot/util/Text.kt
backend/src/main/kotlin/yayauheny/by/bot/util/WorkTimeFormatter.kt
backend/src/main/kotlin/yayauheny/by/common/config/TelegramBotProperties.kt
backend/src/main/kotlin/yayauheny/by/common/config/TelegramBotRuntimeConfig.kt
backend/src/main/kotlin/yayauheny/by/common/config/TelegramBotStartupValidator.kt
backend/src/main/kotlin/yayauheny/by/common/config/TelegramWebhookConfig.kt
backend/src/main/kotlin/yayauheny/by/common/config/TelegramWebhookRegistrar.kt
backend/src/main/kotlin/yayauheny/by/common/config/TelegramWebhookSecretTokenFilter.kt
```

### Related Tests

```text
backend/src/test/kotlin/yayauheny/by/controller/TelegramWebhookControllerTest.kt
backend/src/test/kotlin/yayauheny/by/common/config/TelegramBotPropertiesTest.kt
backend/src/test/kotlin/yayauheny/by/common/config/TelegramBotRuntimeConfigTest.kt
backend/src/test/kotlin/yayauheny/by/common/config/TelegramBotStartupValidatorTest.kt
backend/src/test/kotlin/yayauheny/by/common/config/TelegramWebhookConfigTest.kt
backend/src/test/kotlin/yayauheny/by/common/config/TelegramWebhookRegistrarTest.kt
backend/src/test/kotlin/yayauheny/by/bot/TelegramMessageSenderTest.kt
backend/src/test/kotlin/yayauheny/by/bot/cache/BackListSnapshotCacheTest.kt
backend/src/test/kotlin/yayauheny/by/bot/handler/callbacks/BackToListCallbackTest.kt
backend/src/test/kotlin/yayauheny/by/bot/handler/callbacks/RadiusCallbackTest.kt
backend/src/test/kotlin/yayauheny/by/bot/handler/callbacks/ToiletDetailCallbackTest.kt
backend/src/test/kotlin/yayauheny/by/bot/handler/commands/HelpCommandTest.kt
backend/src/test/kotlin/yayauheny/by/bot/handler/commands/StartCommandTest.kt
backend/src/test/kotlin/yayauheny/by/bot/handler/messages/LocationMessageHandlerTest.kt
backend/src/test/kotlin/yayauheny/by/bot/handler/messages/PlainTextFallbackMessageHandlerTest.kt
backend/src/test/kotlin/yayauheny/by/bot/keyboard/InlineKeyboardFactoryTest.kt
backend/src/test/kotlin/yayauheny/by/bot/router/UpdateRouterTest.kt
backend/src/test/kotlin/yayauheny/by/bot/service/FormatterServiceTest.kt
backend/src/test/kotlin/yayauheny/by/bot/service/UpdateHandlingServiceTest.kt
backend/src/test/kotlin/yayauheny/by/bot/service/UserServiceTest.kt
backend/src/test/kotlin/yayauheny/by/unit/service/BotUpdateServiceImplTest.kt
backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/telegram/TelegramWebhookFlowIntegrationTest.kt
```

### Notes

- Findings:
  `backend/src/main/kotlin/yayauheny/by/bot/handler/messages/LocationMessageHandler.kt`, `backend/src/main/kotlin/yayauheny/by/bot/handler/callbacks/RadiusCallback.kt`, and `backend/src/main/kotlin/yayauheny/by/bot/service/UserService.kt` carried a broken session contract: the bot stored a per-user search radius in `UserSession`, but never updated it on radius callbacks and never used it for later location searches. After a user expanded to `2000m`, the next shared location silently fell back to `500m`. Fixed by persisting radius changes in `UserService`, updating the session in `RadiusCallback`, and reading the saved radius in `LocationMessageHandler`.
- Questions:

## 9. Analytics Subsystem

### Review Focus

- analytics controllers, services, models, entities, and repositories align
- privacy-related handling and identity mapping are correct
- metrics and event persistence still behave correctly after migration

### Main Files

```text
backend/src/main/kotlin/yayauheny/by/analytics/api/AnalyticsController.kt
backend/src/main/kotlin/yayauheny/by/analytics/api/PublicAnalyticsController.kt
backend/src/main/kotlin/yayauheny/by/analytics/entity/AnalyticsEventEntity.kt
backend/src/main/kotlin/yayauheny/by/analytics/entity/UserAnalyticsEntity.kt
backend/src/main/kotlin/yayauheny/by/analytics/entity/UserEntity.kt
backend/src/main/kotlin/yayauheny/by/analytics/model/AnalyticsIdentityHeaders.kt
backend/src/main/kotlin/yayauheny/by/analytics/model/EncryptedAnalyticsEventCommand.kt
backend/src/main/kotlin/yayauheny/by/analytics/model/PublicAnalyticsModels.kt
backend/src/main/kotlin/yayauheny/by/analytics/repository/AnalyticsEventJpaRepository.kt
backend/src/main/kotlin/yayauheny/by/analytics/repository/PublicAnalyticsRepository.kt
backend/src/main/kotlin/yayauheny/by/analytics/repository/PublicAnalyticsRepositoryImpl.kt
backend/src/main/kotlin/yayauheny/by/analytics/repository/UserAnalyticsJpaRepository.kt
backend/src/main/kotlin/yayauheny/by/analytics/repository/UserJpaRepository.kt
backend/src/main/kotlin/yayauheny/by/analytics/service/AnalyticsService.kt
backend/src/main/kotlin/yayauheny/by/analytics/service/PublicAnalyticsService.kt
common/src/main/kotlin/yayauheny/by/model/analytics/AcquisitionTag.kt
common/src/main/kotlin/yayauheny/by/model/analytics/AnalyticsEventCommand.kt
common/src/main/kotlin/yayauheny/by/model/analytics/AnalyticsEventRequest.kt
common/src/main/kotlin/yayauheny/by/model/analytics/AnalyticsSource.kt
common/src/main/kotlin/yayauheny/by/model/analytics/ProductAnalyticsEvent.kt
```

### Related Tests

```text
backend/src/test/kotlin/yayauheny/by/unit/analytics/service/AnalyticsServiceTest.kt
backend/src/test/kotlin/yayauheny/by/unit/analytics/service/PublicAnalyticsServiceTest.kt
backend/src/test/kotlin/yayauheny/by/unit/controller/AnalyticsControllerTest.kt
backend/src/test/kotlin/yayauheny/by/unit/controller/PublicAnalyticsControllerTest.kt
backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/analytics/AnalyticsApiTest.kt
backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/analytics/AnalyticsUserUpsertIntegrationTest.kt
backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/analytics/PublicAnalyticsApiTest.kt
```

### Notes

- Findings:
- Questions:

## 10. Shared Infrastructure, Configuration, Errors, Utilities, and Metrics

### Review Focus

- Spring configuration and beans are explicit and correct
- global exception handling is correct
- utilities are still needed and not carrying Ktor assumptions
- metrics code is wired correctly for Spring Boot runtime

### Main Files

```text
backend/src/main/kotlin/yayauheny/by/common/config/ApiProperties.kt
backend/src/main/kotlin/yayauheny/by/common/config/RequestLoggingConfig.kt
backend/src/main/kotlin/yayauheny/by/common/config/TelegramBotProperties.kt
backend/src/main/kotlin/yayauheny/by/common/config/TelegramBotRuntimeConfig.kt
backend/src/main/kotlin/yayauheny/by/common/config/TelegramBotStartupValidator.kt
backend/src/main/kotlin/yayauheny/by/common/config/TelegramWebhookConfig.kt
backend/src/main/kotlin/yayauheny/by/common/config/TelegramWebhookRegistrar.kt
backend/src/main/kotlin/yayauheny/by/common/config/TelegramWebhookSecretTokenFilter.kt
backend/src/main/kotlin/yayauheny/by/common/config/TimeConfig.kt
backend/src/main/kotlin/yayauheny/by/common/errors/ApiError.kt
backend/src/main/kotlin/yayauheny/by/common/errors/ErrorResponse.kt
backend/src/main/kotlin/yayauheny/by/common/errors/GlobalExceptionHandler.kt
backend/src/main/kotlin/yayauheny/by/common/errors/RestExceptions.kt
backend/src/main/kotlin/yayauheny/by/common/util/NumberExtensions.kt
backend/src/main/kotlin/yayauheny/by/metrics/BackendSearchMetrics.kt
backend/src/main/kotlin/yayauheny/by/metrics/MetricLabelWhitelist.kt
backend/src/main/kotlin/yayauheny/by/metrics/SearchMetricBuckets.kt
backend/src/main/kotlin/yayauheny/by/bot/metrics/BotMetricLabelWhitelist.kt
backend/src/main/kotlin/yayauheny/by/bot/metrics/BotMetrics.kt
backend/src/main/kotlin/yayauheny/by/util/EncryptionService.kt
backend/src/main/kotlin/yayauheny/by/util/EnvironmentExtensions.kt
backend/src/main/kotlin/yayauheny/by/util/PaginationUtils.kt
backend/src/main/kotlin/yayauheny/by/util/ScheduleUtils.kt
```

### Related Tests

```text
backend/src/test/kotlin/yayauheny/by/common/config/TelegramBotPropertiesTest.kt
backend/src/test/kotlin/yayauheny/by/common/config/TelegramBotRuntimeConfigTest.kt
backend/src/test/kotlin/yayauheny/by/common/config/TelegramBotStartupValidatorTest.kt
backend/src/test/kotlin/yayauheny/by/common/config/TelegramWebhookConfigTest.kt
backend/src/test/kotlin/yayauheny/by/common/config/TelegramWebhookRegistrarTest.kt
backend/src/test/kotlin/yayauheny/by/unit/metrics/BackendSearchMetricsTest.kt
backend/src/test/kotlin/yayauheny/by/unit/metrics/MetricLabelWhitelistTest.kt
backend/src/test/kotlin/yayauheny/by/unit/metrics/SearchMetricBucketsTest.kt
backend/src/test/kotlin/yayauheny/by/unit/util/EnvironmentConfigTest.kt
backend/src/test/kotlin/yayauheny/by/unit/util/PaginationEdgeCaseTest.kt
backend/src/test/kotlin/yayauheny/by/model/schedule/ScheduleUtilsTest.kt
backend/src/test/kotlin/yayauheny/by/testsupport/GenerateEnvValuesTest.kt
backend/src/test/kotlin/yayauheny/by/testsupport/TestEncryptionKeyset.kt
```

### Notes

- Findings:
- Questions:

## 11. Database Migrations and Runtime Resources

### Review Focus

- Liquibase changelog ordering is correct
- SQL migrations match current entities and repositories
- runtime OpenAPI and logging resources are still valid

### Main Files

```text
backend/src/main/resources/db/changelog/db.changelog-master.yml
backend/src/main/resources/db/changelog/migration/1757246414_init_tables.sql
backend/src/main/resources/db/changelog/migration/20251205_import_minsk_metro.sql
backend/src/main/resources/db/changelog/migration/20251205_upsert_minsk_metro.sql
backend/src/main/resources/db/changelog/migration/20251206_update_restroom_imports_table.sql
backend/src/main/resources/db/changelog/migration/20251209_seed_initial_restrooms_minsk_2gis.sql
backend/src/main/resources/db/changelog/migration/20251210_update_subway_stations_for_restrooms.sql
backend/src/main/resources/db/changelog/migration/20260511_product_analytics_mvp.sql
backend/src/main/resources/db/changelog/migration/20260512_twogis_import_hardening_v1.sql
backend/src/main/resources/db/changelog/migration/20260514_add_restroom_match_key_unique_index.sql
backend/src/main/resources/db/changelog/migration/20260515_fix_building_2gis_index.sql
backend/src/main/resources/db/changelog/migration/20260515_fix_building_external_id_indexes.sql
backend/src/main/resources/db/changelog/migration/20260515_fix_building_match_key_unique_index.sql
backend/src/main/resources/db/changelog/migration/20260515_seed_observed_countries_and_cities.sql
backend/src/main/resources/db/changelog/migration/20260515_validate_observed_countries_and_cities_coordinates.sql
backend/src/main/resources/db/changelog/migration/20260523_add_users_acquisition_tag.sql
backend/src/main/resources/db/changelog/migration/20260523_add_users_acquisition_tag_check.sql
backend/src/main/resources/db/changelog/migration/20260526_fix_restroom_match_key_unique_index.sql
backend/src/main/resources/db/changelog/migration/20260530_fix_restroom_imports_payload_hash.sql
backend/src/main/resources/db/changelog/migration/PREDEFINED_IDS.md
backend/src/main/resources/openapi.yaml
backend/src/main/resources/logback.xml
backend/src/test/resources/application.yml
backend/src/test/resources/telegram/location_update.json
backend/src/test/resources/telegram/start_website_update.json
```

### Notes

- Findings:
- Questions:

## 12. Test Infrastructure and Migration Leftovers

### Review Focus

- test base classes still reflect current Spring Boot runtime
- no Ktor-only test scaffolding remains unless intentionally temporary
- migration gap notes are either still valid or can be removed later

### Main Files

```text
backend/SPRING_BOOT_TEST_MIGRATION_GAPS.md
backend/src/test/kotlin/yayauheny/by/helpers/DatabaseTestHelper.kt
backend/src/test/kotlin/yayauheny/by/helpers/HttpTestHelpers.kt
backend/src/test/kotlin/yayauheny/by/helpers/ImportTestResourceHelper.kt
backend/src/test/kotlin/yayauheny/by/helpers/JsonTestHelpers.kt
backend/src/test/kotlin/yayauheny/by/helpers/MockitoHelpers.kt
backend/src/test/kotlin/yayauheny/by/helpers/TestDataHelpers.kt
backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/base/BaseIntegrationTest.kt
backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/base/KtorTestApplication.kt
backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/base/TestModules.kt
backend/src/test/kotlin/yayauheny/by/integrationTest/kotlin/base/TestPostgresContainer.kt
backend/src/test/kotlin/yayauheny/by/unit/di/ImportingModuleTest.kt
backend/src/test/kotlin/yayauheny/by/unit/di/ServiceModuleTest.kt
backend/src/test/kotlin/kotlinx/serialization/SerializationException.kt
backend/src/test/kotlin/kotlinx/serialization/SerializationStubs.kt
backend/src/test/kotlin/kotlinx/serialization/json/JsonCompat.kt
backend/src/test/kotlin/kotlinx/serialization/modules/SerializersModuleCompat.kt
```

### Notes

- Findings:
- Questions:

## Suggested Review Order After Setup

1. Layer 1: make sure app wiring and runtime assumptions are valid.
2. Layer 2 and layer 10: verify HTTP handling, Spring config, and exception behavior.
3. Layer 3 through layer 6: review the core application path end to end.
4. Layer 7 through layer 9: review complex subsystems one by one.
5. Layer 11 and layer 12: finish with schema, resources, and test migration leftovers.
