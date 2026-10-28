# Redis restroom cache architecture

Date: 2026-06-25

## Goal

Move the first nearest-restroom lookup from PostgreSQL/PostGIS to Redis without making Redis the source of truth.

The authoritative restroom data remains in PostgreSQL. Redis stores two derived read models:

- a minimal geospatial read model for `GET /api/v1/restrooms/nearest`;
- a bounded full-detail cache for the 100 most frequently used restroom details.

Current data volume is small: roughly 4,463 restrooms and about 6 MB of full restroom data. The near-term target is 7,000-10,000 restrooms. This is small enough for Redis geospatial indexes, but the implementation should still avoid loading full JPA entities and avoid one large Redis transaction.

## Non-goals

- Do not cache every `nearest` response by user coordinates.
- Do not store the full `RestroomResponseDto` for every restroom in Redis.
- Do not make Redis responsible for durable restroom state.
- Do not move detail lookup for cold restrooms away from PostgreSQL.

## Recommended libraries

Use Spring Data Redis with Lettuce through `spring-boot-starter-data-redis`.

Reasons:

- Spring Boot already manages Redis/Lettuce auto-configuration through `application.yml`.
- Spring Data Redis exposes `GeoOperations`, `HashOperations`, `ZSetOperations`, and pipelining, which cover this design directly.
- Redisson is not required for the core design. Its local cached maps support LRU/LFU policies, but this project needs a deterministic top-100 cross-instance cache, not only JVM-local eviction.

Optional future dependency:

- Redisson can be considered later if the project needs distributed locks or advanced map/cache primitives, but it should not be introduced just for Redis GEO or top-100 tracking.

Primary references:

- Redis geospatial overview: https://redis.io/docs/latest/develop/data-types/geospatial/
- Redis `GEOADD`: https://redis.io/docs/latest/commands/geoadd/
- Redis `GEOSEARCH`: https://redis.io/docs/latest/commands/geosearch/
- Redis eviction policies: https://redis.io/docs/latest/develop/reference/eviction/
- Redis pipelining: https://redis.io/docs/latest/develop/clients/jedis/transpipe/
- Spring Data Redis template operations: https://docs.spring.io/spring-data/redis/reference/redis/template.html
- Spring Data Redis pipelining: https://docs.spring.io/spring-data/redis/reference/redis/pipelining.html
- Spring Boot application properties: https://docs.spring.io/spring-boot/appendix/application-properties/index.html
- Redisson cache APIs, optional reference: https://redisson.pro/docs/cache-api-implementations/

## Runtime configuration

All behavior must be configurable from `application.yml`.

Suggested properties:

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: ${REDIS_DATABASE:0}
      timeout: 2s

app:
  restroom-cache:
    enabled: true

    nearest:
      enabled: true
      key-prefix: restrooms:nearest
      startup-load-mode: full
      refresh-mode: incremental
      fallback-to-database: true
      max-search-radius-meters: 20000
      max-result-limit: 100
      candidate-limit-multiplier: 10
      max-candidate-limit: 500
      load-page-size: 1000
      redis-pipeline-batch-size: 500
      max-incremental-change-ids: 1000
      old-version-ttl: 30m
      rebuild-lock-ttl: 2m
      min-ready-items: 1

    popular-details:
      enabled: true
      key-prefix: restrooms:popular
      algorithm: lfu
      max-items: 100
      detail-ttl: 24h
      score-ttl: 30d
      refresh-interval: 5m
      lfu-decay-interval: 1d
      lfu-decay-factor: 0.9
      load-page-size: 100
      redis-pipeline-batch-size: 100
```

Property meaning:

| Property | Meaning |
| --- | --- |
| `enabled` | Turns the whole cache feature on/off. |
| `nearest.startup-load-mode` | `full` loads all active restrooms on startup; `lazy` creates no startup snapshot and relies on later events/manual refresh. Default should be `full`. |
| `nearest.refresh-mode` | `incremental` applies single-restroom changes; `full` rebuilds the whole nearest index after changes. Default should be `incremental`. |
| `fallback-to-database` | Allows current PostGIS query if Redis is unavailable or empty. Should be `true` at rollout, can become `false` later. |
| `max-search-radius-meters` | Upper guard for Redis `GEOSEARCH`, must not exceed API limits. |
| `max-result-limit` | Upper guard for returned nearest results, must not exceed API limits. |
| `candidate-limit-multiplier` | Multiplier for Redis candidate count before application filtering. |
| `max-candidate-limit` | Upper bound for Redis candidate count before application filtering. |
| `load-page-size` | PostgreSQL projection page size during full rebuild. |
| `redis-pipeline-batch-size` | Number of Redis write commands grouped in one pipeline batch. |
| `max-incremental-change-ids` | Maximum changed ids handled incrementally before scheduling a full rebuild. |
| `old-version-ttl` | How long old versioned keys live after a successful rebuild. |
| `rebuild-lock-ttl` | Redis lock TTL to prevent concurrent full rebuilds. |
| `min-ready-items` | Minimum item count required before publishing a rebuilt version. |
| `popular-details.algorithm` | `lfu` is the chosen algorithm. |
| `popular-details.max-items` | Bounded full-detail cache size. Default: `100`. |
| `popular-details.detail-ttl` | TTL for full cached detail objects. |
| `popular-details.score-ttl` | Window for popularity scores. |
| `lfu-decay-*` | Periodic score decay so old popularity does not dominate forever. |

## Nearest cache data model

The nearest cache stores only fields needed for initial nearby results.

```kotlin
data class NearestRestroomCacheItem(
    val id: UUID,
    val lat: Double,
    val lon: Double,
    val displayName: String,
    val feeType: FeeType
)
```

Do not store `status` and `isHidden` in the minimal item. The cache must contain only restrooms that are visible to nearest search:

```sql
WHERE status = 'ACTIVE'
  AND is_hidden = false
```

Do not store full detail fields here. `genderType`, `accessibilityType`, `placeType`, `amenities`, or schedules can be added later when the public nearest endpoint actually supports those filters. Until then, keep the nearest item minimal.

## Nearest Redis keys

Use versioned keys so a full rebuild does not create a period where readers see an empty cache.

```text
restrooms:nearest:active-version -> "{version}"
restrooms:nearest:{version}:geo
restrooms:nearest:{version}:items
restrooms:nearest:{version}:meta
restrooms:nearest:rebuild-lock
```

`geo` is a Redis geospatial index:

```text
GEOADD restrooms:nearest:{version}:geo lon lat restroomId
```

`items` is a Redis hash:

```text
HSET restrooms:nearest:{version}:items restroomId "{NearestRestroomCacheItem JSON}"
```

`meta` stores diagnostic values:

```text
HSET restrooms:nearest:{version}:meta itemCount 4463 rebuiltAt 2026-06-25T12:00:00Z source database
```

## Nearest read flow

`RestroomController` remains unchanged. `RestroomService.findNearestRestrooms` delegates to a cache-facing service.

Flow:

1. Validate API `limit` and `distanceMeters` against existing API config and cache limits.
2. Read `restrooms:nearest:active-version`.
3. Run Redis `GEOSEARCH` on `restrooms:nearest:{version}:geo` from user lon/lat by radius in meters, sorted ascending.
4. Fetch item JSON through `HMGET restrooms:nearest:{version}:items`.
5. Compute exact `distanceMeters` in Kotlin using the same service-side distance logic used today.
6. Sort by computed distance and take `limit`.
7. Return `NearestRestroomSlimDto`.

Do not use `COUNT limit` before application-side filtering if filters are added later. Redis could return 5 nearby paid toilets, then the Kotlin filter could remove them all even though the 6th item matches. For current minimal fields and no extra filters, `COUNT limit` is acceptable, but the safer long-term default is:

- use `COUNT nearestRedisCandidateLimit`, where candidate limit is configurable and larger than user limit;
- default candidate limit can be `min(500, max(limit * 10, limit))`.

For 7,000-10,000 total restrooms, fetching a few hundred nearby candidates is acceptable.

## Startup loading

Use `ApplicationReadyEvent`, not heavy logic inside `@PostConstruct`.

Reason:

- `ApplicationReadyEvent` runs after Spring Boot startup and database migrations.
- The application can expose a clear startup/refresh log and metrics boundary.
- A heavy `@PostConstruct` can fail bean creation and make debugging harder.

`startup-load-mode`:

| Mode | Behavior |
| --- | --- |
| `full` | Build a fresh version from PostgreSQL at startup and publish it as active. Recommended default. |
| `lazy` | Do not load at startup. The first change event or manual refresh builds Redis. Only use for local/dev or emergency rollout. |

Full load must use a projection query, not `findAll()` JPA entities.

Required projection query shape:

```sql
SELECT
  id,
  ST_Y(coordinates) AS lat,
  ST_X(coordinates) AS lon,
  COALESCE(NULLIF(name, ''), NULLIF(address, ''), 'Unknown') AS display_name,
  fee_type
FROM restrooms
WHERE status = 'ACTIVE'
  AND is_hidden = false
ORDER BY id
LIMIT :limit
OFFSET :offset
```

Load pages by `nearest.load-page-size`. Write to Redis with pipelining by `nearest.redis-pipeline-batch-size`.

Do not wrap the whole load in one Redis `MULTI/EXEC` transaction. Pipelining is enough because readers continue using the old active version until the new version is fully written and published.

## Full rebuild publish algorithm

1. Acquire `restrooms:nearest:rebuild-lock` with TTL.
2. Generate new version, for example `20260625T120000Z-uuid`.
3. Page through PostgreSQL projection.
4. Pipeline `GEOADD` and `HSET` into new version keys.
5. Write `meta`.
6. Verify `itemCount >= min-ready-items`.
7. Set `restrooms:nearest:active-version` to the new version.
8. Set TTL on previous version keys using `old-version-ttl`.
9. Release the rebuild lock.

If any step fails before active version switch, keep the old version active.

## Incremental updates

New toilets must not be lost between full refreshes.

Default `nearest.refresh-mode` should be `incremental`.

After commit, publish or handle a domain event:

```kotlin
data class RestroomChangedEvent(
    val restroomIds: Set<UUID>,
    val reason: RestroomChangeReason
)
```

Event sources:

- `RestroomService.createRestroom`
- `RestroomService.updateRestroom`
- `RestroomService.deleteRestroom`
- import batch when at least one item is `CREATED`, `UPDATED`, or `LINKED_DUPLICATE`

Use `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` so Redis changes happen only after PostgreSQL commits.

Incremental event handling:

1. Load changed restroom projection rows by ids from PostgreSQL.
2. Read current active version.
3. For each changed id:
   - if row exists and is visible: `GEOADD` active geo key and `HSET` active item hash;
   - if row does not exist or is no longer visible: `ZREM` active geo key and `HDEL` active item hash.

Redis GEO indexes are sorted sets internally, so removal uses `ZREM`.

If incremental update fails, log it and schedule a full rebuild. Do not silently ignore it.

`refresh-mode: full` is a safe alternative for early rollout:

- every committed restroom change schedules one full rebuild;
- imports should coalesce changes and trigger one rebuild after the whole batch, not per item.

## Import behavior

The import pipeline currently processes many items. Redis must not be updated per imported item inside the import item transaction.

Recommended behavior:

1. Import writes PostgreSQL as it does today.
2. Import collects changed restroom ids or at least detects that changes happened.
3. After the batch completes and commits, publish one `RestroomChangedEvent`.
4. If `refresh-mode = incremental`, update only changed ids.
5. If `refresh-mode = full`, schedule one full rebuild.

For very large import batches, cap changed-id collection through config:

```yaml
app:
  restroom-cache:
    nearest:
      max-incremental-change-ids: 1000
```

If a batch changes more than this value, publish a full rebuild event instead of thousands of individual updates.

## Popular detail cache

The popular cache stores full detail JSON for at most 100 restroom ids.

This cache is separate from nearest search and should not affect nearest correctness.

Keys:

```text
restrooms:popular:details
restrooms:popular:lfu:scores
restrooms:popular:lfu:seen
```

`details`:

```text
HSET restrooms:popular:details restroomId "{RestroomResponseDto JSON}"
```

`lfu:scores`:

```text
ZINCRBY restrooms:popular:lfu:scores 1 restroomId
```

`lfu:seen` stores last access timestamps for diagnostics and tie-breaking:

```text
HSET restrooms:popular:lfu:seen restroomId epochMillis
```

## LFU choice

Choose LFU, not LRU.

Reason:

- The product likely has toilets that repeatedly appear in dense/high-traffic areas.
- LFU keeps genuinely popular details hot even if a burst of one-off lookups happens nearby.
- LRU is simpler, but it can replace stable popular items with recently accessed but rarely reused items.

Do not rely on Redis global `allkeys-lfu` for the top-100 detail cache. Redis eviction is useful as a memory safety net, but it does not provide a deterministic "exactly top 100 restrooms" application list. Use an application-managed sorted set.

LFU algorithm:

1. On every restroom detail request, call `ZINCRBY restrooms:popular:lfu:scores 1 restroomId`.
2. Update `restrooms:popular:lfu:seen`.
3. If the requested restroom is already cached in `details`, return it.
4. If not cached, load detail from PostgreSQL.
5. Cache it only if it is in the current top `popular-details.max-items`, or if the detail cache has fewer than max items.
6. Periodically trim:
   - keep top `max-items` by score;
   - remove other ids from `details`;
   - optionally decay scores by `lfu-decay-factor` once per `lfu-decay-interval`.

Decay prevents old popularity from dominating forever.

The trim job can run every `popular-details.refresh-interval`.

## Detail read flow

`RestroomService.getRestroomById(id)` can be extended:

1. Increment LFU score.
2. Try `HGET restrooms:popular:details id`.
3. If hit: return cached `RestroomResponseDto`.
4. If miss: load from PostgreSQL through the existing path.
5. Cache if eligible for top-100.

If a restroom is updated or deleted:

- remove it from `restrooms:popular:details`;
- keep or remove LFU score depending on product decision.

Recommended first implementation:

- remove full detail JSON on update/delete;
- keep LFU score on update;
- remove LFU score on delete.

## Failure behavior

Nearest cache:

- Redis hit: serve from Redis.
- Redis unavailable and `fallback-to-database = true`: use current PostGIS repository query and log a warning.
- Redis unavailable and fallback disabled: return a controlled service-unavailable error.
- Active version missing: trigger async full rebuild and use fallback if enabled.

Popular detail cache:

- Redis failure must never break detail lookup.
- On failure, load from PostgreSQL and skip popularity update.

## Metrics and logs

Add Micrometer metrics:

```text
restroom_nearest_cache_requests_total{outcome=hit|miss|fallback|error}
restroom_nearest_cache_items_loaded_total
restroom_nearest_cache_rebuild_duration_seconds
restroom_nearest_cache_incremental_updates_total{outcome=applied|deleted|failed}
restroom_popular_detail_cache_requests_total{outcome=hit|miss|error}
restroom_popular_detail_cache_size
```

Log at info:

- full rebuild started/completed;
- active version switched;
- incremental batch applied;
- fallback activated.

Log at warn:

- Redis unavailable;
- rebuild skipped due to lock;
- incremental update failed and full rebuild was scheduled.

## Implementation outline

Suggested packages:

```text
backend/src/main/kotlin/yayauheny/by/cache/restroom
```

Core interfaces:

```kotlin
interface NearestRestroomCache {
    fun findNearest(latitude: Double, longitude: Double, limit: Int, distanceMeters: Int): List<NearestRestroomSlimDto>?
    fun replaceAll()
    fun applyChanges(restroomIds: Set<UUID>)
}

interface PopularRestroomDetailCache {
    fun get(id: UUID): RestroomResponseDto?
    fun recordAccess(id: UUID)
    fun putIfPopular(restroom: RestroomResponseDto)
    fun evict(id: UUID)
}
```

Supporting classes:

```text
RestroomCacheProperties
RestroomNearestCacheItem
RestroomNearestProjectionRepository
RedisNearestRestroomCache
RedisPopularRestroomDetailCache
RestroomCacheRefreshService
RestroomChangedEvent
RestroomCacheEventListener
```

`RestroomNearestProjectionRepository` should use JDBC or a native projection query, not entity loading.

## Rollout plan

1. Add dependencies and properties with cache disabled by default in test profile.
2. Implement nearest projection query and unit tests for mapping.
3. Implement Redis nearest cache with `full` startup load and DB fallback.
4. Switch `RestroomService.findNearestRestrooms` to Redis-first.
5. Add after-commit events for CRUD and import.
6. Add incremental update mode.
7. Add popular detail LFU cache.
8. Add metrics and integration tests with Testcontainers Redis.

## Test requirements

Unit tests:

- projection maps nullable `name/address` into `displayName`;
- nearest cache computes distance from cached coordinates;
- hidden/inactive restrooms are removed from Redis on incremental update;
- new active restroom is added to Redis on incremental update;
- LFU score increments on detail access;
- top-100 trim removes non-popular details.

Integration tests:

- startup full load builds Redis keys from PostgreSQL;
- `nearest` returns data from Redis with PostgreSQL nearest repository mocked/not used;
- Redis unavailable falls back to PostgreSQL when enabled;
- import-created restroom becomes visible in Redis after commit;
- update to `isHidden = true` removes restroom from nearest cache;
- popular detail cache hit avoids DB detail load.

## Final decision

Use Redis as a derived read model:

- minimal Redis GEO + hash projection for nearest search;
- PostgreSQL remains the source of truth;
- full detail JSON is cached only for the top 100 LFU restrooms;
- all behavior is controlled through `application.yml`;
- startup default is full load;
- change default is incremental update, with full rebuild as configurable fallback.
