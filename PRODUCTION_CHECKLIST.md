## Production Readiness Checklist (Tutochka Backend)

### Infrastructure
- Java 21 LTS (same version in CI/CD and runtime)
- PostgreSQL 15+ with extensions: `postgis`, `pgcrypto`, `btree_gist`
- Network: allow outbound map services if needed; restrict DB to app/bot only
- Timezone: UTC on app and DB hosts

### Database & Migrations
- Run Liquibase migrations:
  - `./gradlew backend:liquibaseUpdate`
- Verify schema after deploy:
  - tables: `buildings`, `subway_lines`, `subway_stations`, updated `restrooms`
  - indexes: GIST on geometry, GIN on JSONB, FK cascades as per migration
- Backups: enable WAL archiving or daily backups; test restore

### Application Configuration (.env)
```
DB_HOST=prod-db
DB_PORT=5432
DB_NAME=tutochka
DB_USER=...
DB_PASSWORD=...
APP_PORT=8080
APP_ENV=production
```
- Ensure pool settings match prod workload (Hikari): maxPoolSize, connectionTimeout, idleTimeout, maxLifetime
- Disable Swagger UI on public internet (or protect with auth)

### Build & Run
- Build: `./gradlew backend:build`
- Run: `java -jar backend/build/libs/backend-all.jar`
- Healthcheck: `GET /health`
- Logs: JSON or structured; rotate via journald/Logback config

### API & Data Model (v1.0)
- Buildings: `placeType`, `external_ids`, PostGIS point, soft delete
- Restrooms: `placeType`, `fee_type`, `access_note`, `direction_guide`, `external_maps`, `inherit_building_schedule`, `has_photos`, links to building/subway station
- Subway: lines (hexColor, is_deleted), stations (geo, is_deleted), nearest-station assignment
- Filtering uses table columns (not JSONB) for `fee_type`, `place_type`, accessibility

### Monitoring & Alerts
- Metrics: JVM (heap, GC), DB pool (active/idle), HTTP 5xx/latency
- DB: replication lag/backups; PostGIS index bloat checks
- Alerts: migration failures, pool exhaustion, elevated 5xx, slow queries

### Bot Readiness (Telegram)
- Backend client uses:
  - `GET /restrooms/nearest`
  - `GET /restrooms/{id}`
- DTOs aligned with new schema (`PlaceType`, `accessNote`, `directionGuide`, `externalMaps`, `hasPhotos`)
- Color-to-emoji mapping for subway lines present (`SubwayEmoji.getEmojiForColor`)
- To improve for new API:
  - Show building name/address when `buildingId` present
  - Render subway line/emoji if station returned in restroom details
  - Expose fee/accessibility/placeType in list/detail replies
  - Add retries/timeouts for backend calls

### Security
- Secrets only via env/secret store; no secrets in images
- TLS termination in front (ingress/proxy)
- Rate limiting/WAF for public endpoints

### Smoke Tests (post-deploy)
- `GET /health` -> 200
- Create building, create restroom linked to building, fetch restroom -> building data present
- `GET /restrooms/nearest?lat=...&lon=...` -> returns nearest with distance
- `GET /subway/stations` -> returns coordinates; nearest-station assignment works (setNearestStationForRestroom/batchUpdateStationsForCity)
- Bot: send location -> receives list; detail shows address and map link
