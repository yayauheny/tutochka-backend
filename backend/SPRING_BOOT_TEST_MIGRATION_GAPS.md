# Spring Boot Test Migration Gaps

## Open

## Fixed
- `AnalyticsControllerTest` / `GIVEN analytics event request WHEN POST /api/v1/analytics/events THEN track event with headers`
  - expected behavior: trimmed `X-Tg-Username` header is forwarded in the analytics command
  - actual behavior: controller forwarded the raw header value with surrounding spaces
  - exact wrong field: `username`
  - suspected production files: `backend/src/main/kotlin/yayauheny/by/analytics/api/AnalyticsController.kt`, `backend/src/main/kotlin/yayauheny/by/controller/RestroomController.kt`
  - status date: 2026-05-31
- `AnalyticsApiTest` / `POST /api/v1/analytics/events stores encrypted user and event data`
  - expected behavior: analytics event tracking persists the encrypted user, analytics summary row, and event row
  - actual behavior: the controller returned `202 Accepted`, but the shared-primary-key analytics rows were not being seeded or persisted correctly under Spring
  - exact wrong field / endpoint / validation / repository behavior: `user_analytics` persistence and event timestamp seeding
  - suspected production files: `backend/src/main/kotlin/yayauheny/by/analytics/service/AnalyticsService.kt`, `backend/src/main/kotlin/yayauheny/by/analytics/repository/PublicAnalyticsRepositoryImpl.kt`, `backend/src/test/kotlin/yayauheny/by/helpers/DatabaseTestHelper.kt`
  - status date: 2026-05-31
- `CityApiTest` / `GIVEN non-existing countryId WHEN POST city THEN return 400`
  - expected behavior: a missing country reference is rejected as a client error
  - actual behavior: the request produced `500 Internal Server Error`
  - exact wrong field / endpoint / validation / repository behavior: `POST /api/v1/cities` country lookup failure
  - suspected production files: `backend/src/main/kotlin/yayauheny/by/common/errors/GlobalExceptionHandler.kt`, `backend/src/main/kotlin/yayauheny/by/service/CityService.kt`
  - status date: 2026-05-31
- `PublicAnalyticsApiTest` / `GIVEN seeded analytics WHEN GET public analytics THEN return grouped safe dashboard data`
  - expected behavior: public analytics dashboard returns seeded counts and series data
  - actual behavior: the dashboard query failed against PostgreSQL because the SQL grouped the daily series incorrectly and counted the wrong event key case
  - exact wrong field / endpoint / validation / repository behavior: public analytics aggregation queries in `GET /api/v1/public/analytics`
  - suspected production files: `backend/src/main/kotlin/yayauheny/by/analytics/repository/PublicAnalyticsRepositoryImpl.kt`
  - status date: 2026-05-31
- `RestroomApiTest` / `GIVEN active and inactive restrooms WHEN GET nearest THEN return only active restrooms`
  - expected behavior: inactive restrooms are excluded from nearest-search results
  - actual behavior: nearest-search initially returned inactive restrooms before the query was constrained to active, visible, non-deleted rows
  - exact wrong field / endpoint / validation / repository behavior: `GET /api/v1/restrooms/nearest` missing `status` filter
  - suspected production files: `backend/src/main/kotlin/yayauheny/by/repository/RestroomJpaRepository.kt`
  - status date: 2026-05-31
- `RestroomApiTest` / `GET /api/v1/restrooms/nearest with out-of-range coordinates returns 400 with validation errors`
  - expected behavior: invalid latitude is rejected with field validation errors
  - actual behavior: the endpoint returned a bare `400` without field errors before the exception handler was updated
  - exact wrong field / endpoint / validation / repository behavior: `lat` validation on `GET /api/v1/restrooms/nearest`
  - suspected production files: `backend/src/main/kotlin/yayauheny/by/common/errors/GlobalExceptionHandler.kt`, `backend/src/main/kotlin/yayauheny/by/controller/RestroomController.kt`
  - status date: 2026-05-31
