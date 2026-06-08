package yayauheny.by.helpers

import java.time.Instant
import java.sql.Timestamp
import java.util.UUID
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Point
import org.springframework.jdbc.core.JdbcTemplate
import yayauheny.by.analytics.entity.AnalyticsEventEntity
import yayauheny.by.analytics.entity.UserAnalyticsEntity
import yayauheny.by.analytics.entity.UserEntity
import yayauheny.by.analytics.repository.AnalyticsEventJpaRepository
import yayauheny.by.analytics.repository.UserAnalyticsJpaRepository
import yayauheny.by.analytics.repository.UserJpaRepository
import yayauheny.by.importing.dedup.MatchKeyGenerator
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.DataSourceType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.GenderType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.enums.LocationType
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.model.analytics.AcquisitionTag
import yayauheny.by.model.analytics.AnalyticsSource
import yayauheny.by.entity.BuildingEntity
import yayauheny.by.entity.CityEntity
import yayauheny.by.entity.CountryEntity
import yayauheny.by.entity.RestroomEntity
import yayauheny.by.entity.SubwayLineEntity
import yayauheny.by.entity.SubwayStationEntity
import yayauheny.by.repository.BuildingJpaRepository
import yayauheny.by.repository.CityJpaRepository
import yayauheny.by.repository.CountryJpaRepository
import yayauheny.by.repository.RestroomJpaRepository
import yayauheny.by.repository.SubwayLineJpaRepository
import yayauheny.by.repository.SubwayStationJpaRepository
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive

private val geometryFactory = GeometryFactory()

private fun point(
    lon: Double,
    lat: Double
): Point = geometryFactory.createPoint(Coordinate(lon, lat))

private fun JsonObject?.toBooleanMap(): Map<String, Any>? =
    this?.values?.entries?.associate { (key, value) ->
        key to when (value) {
            is JsonPrimitive -> value.booleanOrNull ?: value.content
            else -> value.toString()
        }
    }

private fun JsonObject?.toStringMap(): Map<String, String>? =
    this?.values?.entries?.associate { (key, value) -> key to value.jsonPrimitive.content }

private fun JsonObject?.toStringList(): List<String>? =
    this?.values?.values?.map { it.jsonPrimitive.content }

data class TestCountryData(
    val nameRu: String = "Test Country RU",
    val nameEn: String = "Test Country EN",
    val code: String = "TC"
)

data class TestCityData(
    val nameRu: String = "Test City RU ${UUID.randomUUID().toString().take(8)}",
    val nameEn: String = "Test City EN ${UUID.randomUUID().toString().take(8)}",
    val lat: Double = 55.7558 + (Math.random() * 0.1 - 0.05),
    val lon: Double = 37.6176 + (Math.random() * 0.1 - 0.05)
)

data class TestRestroomData(
    val name: String? = "Test Restroom",
    val address: String = "Test Address 123",
    val phones: JsonObject? = buildJsonObject { put("main", JsonPrimitive("+1234567890")) },
    val workTime: JsonObject? = buildJsonObject { put("monday", JsonPrimitive("09:00-18:00")) },
    val feeType: FeeType = FeeType.FREE,
    val genderType: GenderType = GenderType.UNISEX,
    val accessibilityType: AccessibilityType = AccessibilityType.UNKNOWN,
    val placeType: PlaceType = PlaceType.OTHER,
    val lat: Double = 55.7558 + (Math.random() * 0.1 - 0.05),
    val lon: Double = 37.6176 + (Math.random() * 0.1 - 0.05),
    val dataSource: DataSourceType = DataSourceType.MANUAL,
    val status: RestroomStatus = RestroomStatus.ACTIVE,
    val amenities: JsonObject = buildJsonObject { put("wifi", JsonPrimitive("true")) },
    val buildingId: UUID? = null,
    val subwayStationId: UUID? = null,
    val externalMaps: JsonObject? =
        buildJsonObject {
            put("yandex", JsonPrimitive("https://yandex.ru/maps/-/CCUqM0VhXD"))
            put("google", JsonPrimitive("https://maps.google.com/?q=53.9006,27.5590"))
        },
    val originProvider: ImportProvider = ImportProvider.MANUAL,
    val originId: String? = null,
    val accessNote: String? = "Доступен для людей с ограниченными возможностями",
    val directionGuide: String? = "Находится на первом этаже, рядом с главным входом",
    val inheritBuildingSchedule: Boolean = false,
    val hasPhotos: Boolean = true
)

data class TestAnalyticsUserData(
    val tgUserId: String = "enc-user-${UUID.randomUUID()}",
    val tgChatId: String = "enc-chat-${UUID.randomUUID()}",
    val source: String = AnalyticsSource.TELEGRAM_BOT.value,
    val acquisitionTag: AcquisitionTag = AcquisitionTag.DIRECT,
    val username: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = createdAt
)

data class TestAnalyticsEventData(
    val event: String,
    val userId: UUID? = null,
    val source: String = AnalyticsSource.TELEGRAM_BOT.value,
    val resultsCount: Int? = null,
    val createdAt: Instant = Instant.now()
)

object DatabaseTestHelper {
    private val excludedTableNames = setOf("databasechangelog", "databasechangeloglock", "spatial_ref_sys")
    private val geometryFactory = GeometryFactory()

    lateinit var jdbcTemplate: JdbcTemplate
    lateinit var countryRepository: CountryJpaRepository
    lateinit var cityRepository: CityJpaRepository
    lateinit var restroomRepository: RestroomJpaRepository
    lateinit var buildingRepository: BuildingJpaRepository
    lateinit var subwayLineRepository: SubwayLineJpaRepository
    lateinit var subwayStationRepository: SubwayStationJpaRepository
    lateinit var userRepository: UserJpaRepository
    lateinit var userAnalyticsRepository: UserAnalyticsJpaRepository
    lateinit var analyticsEventRepository: AnalyticsEventJpaRepository

    fun createTestCountryData(
        nameRu: String = "Test Country RU",
        nameEn: String = "Test Country EN",
        code: String = "TC${UUID.randomUUID().toString().take(8).uppercase()}"
    ) = TestCountryData(nameRu, nameEn, code)

    fun createTestCityData(
        nameRu: String = "Test City RU ${UUID.randomUUID().toString().take(8)}",
        nameEn: String = "Test City EN ${UUID.randomUUID().toString().take(8)}",
        lat: Double = 55.7558 + (Math.random() * 0.1 - 0.05),
        lon: Double = 37.6176 + (Math.random() * 0.1 - 0.05)
    ) = TestCityData(nameRu, nameEn, lat, lon)

    fun createTestRestroomData(
        name: String? = "Test Restroom",
        address: String = "Test Address 123",
        phones: JsonObject? = buildJsonObject { put("main", JsonPrimitive("+1234567890")) },
        workTime: JsonObject? = buildJsonObject { put("monday", JsonPrimitive("09:00-18:00")) },
        feeType: FeeType = FeeType.FREE,
        genderType: GenderType = GenderType.UNISEX,
        accessibilityType: AccessibilityType = AccessibilityType.WHEELCHAIR,
        placeType: PlaceType = PlaceType.OTHER,
        lat: Double = 55.7558 + (Math.random() * 0.1 - 0.05),
        lon: Double = 37.6176 + (Math.random() * 0.1 - 0.05),
        dataSource: DataSourceType = DataSourceType.MANUAL,
        status: RestroomStatus = RestroomStatus.ACTIVE,
        amenities: JsonObject = buildJsonObject { put("wifi", JsonPrimitive("true")) },
        buildingId: UUID? = null,
        subwayStationId: UUID? = null,
        externalMaps: JsonObject? =
            buildJsonObject {
                put("yandex", JsonPrimitive("https://yandex.ru/maps/-/CCUqM0VhXD"))
                put("google", JsonPrimitive("https://maps.google.com/?q=53.9006,27.5590"))
            },
        originProvider: ImportProvider = ImportProvider.MANUAL,
        originId: String? = null,
        accessNote: String? = "Доступен для людей с ограниченными возможностями",
        directionGuide: String? = "Находится на первом этаже, рядом с главным входом",
        inheritBuildingSchedule: Boolean = false,
        hasPhotos: Boolean = true
    ) = TestRestroomData(
        name,
        address,
        phones,
        workTime,
        feeType,
        genderType,
        accessibilityType,
        placeType,
        lat,
        lon,
        dataSource,
        status,
        amenities,
        buildingId,
        subwayStationId,
        externalMaps,
        originProvider,
        originId,
        accessNote,
        directionGuide,
        inheritBuildingSchedule,
        hasPhotos
    )

    fun createTestAnalyticsUserData(
        tgUserId: String = "enc-user-${UUID.randomUUID()}",
        tgChatId: String = "enc-chat-${UUID.randomUUID()}",
        source: String = AnalyticsSource.TELEGRAM_BOT.value,
        acquisitionTag: AcquisitionTag = AcquisitionTag.DIRECT,
        username: String? = null,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = createdAt
    ) = TestAnalyticsUserData(tgUserId, tgChatId, source, acquisitionTag, username, createdAt, updatedAt)

    fun createTestAnalyticsEventData(
        event: String,
        userId: UUID? = null,
        source: String = AnalyticsSource.TELEGRAM_BOT.value,
        resultsCount: Int? = null,
        createdAt: Instant = Instant.now()
    ) = TestAnalyticsEventData(event, userId, source, resultsCount, createdAt)

    fun insertTestCountry(data: TestCountryData = createTestCountryData()): UUID {
        val entity =
            CountryEntity().apply {
                code = data.code
                nameRu = data.nameRu
                nameEn = data.nameEn
            }
        return countryRepository.saveAndFlush(entity).id ?: error("Failed to insert test country")
    }

    fun insertTestCity(
        countryId: UUID,
        data: TestCityData = createTestCityData()
    ): UUID {
        val country = countryRepository.findById(countryId).orElseThrow { error("Country not found") }
        val entity =
            CityEntity().apply {
                this.country = country
                this.nameRu = data.nameRu
                this.nameEn = data.nameEn
                this.coordinates = geometryFactory.createPoint(Coordinate(data.lon, data.lat))
            }
        return cityRepository.saveAndFlush(entity).id ?: error("Failed to insert test city")
    }

    fun insertTestRestroom(
        cityId: UUID? = null,
        data: TestRestroomData = createTestRestroomData()
    ): UUID {
        val entity =
            RestroomEntity().apply {
                this.city = cityId?.let { cityRepository.findById(it).orElseThrow { error("City not found") } }
                this.building = data.buildingId?.let { buildingRepository.findById(it).orElseThrow { error("Building not found") } }
                this.subwayStation = data.subwayStationId?.let { subwayStationRepository.findById(it).orElseThrow { error("Subway station not found") } }
                this.name = data.name
                this.address = data.address
                this.phones = data.phones.toStringList()
                this.workTime = data.workTime.toStringMap()?.toMutableMap()
                this.feeType = data.feeType
                this.genderType = data.genderType
                this.accessibilityType = data.accessibilityType
                this.placeType = data.placeType
                this.coordinates = point(data.lon, data.lat)
                this.dataSource = data.dataSource
                this.status = data.status
                this.amenities = data.amenities.toBooleanMap()?.toMutableMap()
                this.externalMaps = data.externalMaps.toStringMap()?.toMutableMap()
                this.accessNote = data.accessNote
                this.directionGuide = data.directionGuide
                this.inheritBuildingSchedule = data.inheritBuildingSchedule
                this.hasPhotos = data.hasPhotos
                this.locationType = if (data.buildingId != null) LocationType.INSIDE_BUILDING else LocationType.STANDALONE
                this.originProvider = data.originProvider
                this.originId = data.originId
                this.isHidden = false
            }
        return restroomRepository.saveAndFlush(entity).id ?: error("Failed to insert test restroom")
    }

    fun insertTestBuilding(
        cityId: UUID,
        address: String,
        name: String? = null,
        lat: Double,
        lon: Double
    ): UUID {
        val city = cityRepository.findById(cityId).orElseThrow { error("City not found") }
        val entity =
            BuildingEntity().apply {
                this.city = city
                this.address = address
                this.name = name
                this.coordinates = point(lon, lat)
                this.buildingMatchKey = MatchKeyGenerator.buildingMatchKey(cityId, address, lat, lon)
            }
        return buildingRepository.saveAndFlush(entity).id ?: error("Failed to insert test building")
    }

    fun insertStandaloneRestroom(
        cityId: UUID,
        name: String,
        address: String,
        lat: Double,
        lon: Double
    ): UUID {
        val city = cityRepository.findById(cityId).orElseThrow { error("City not found") }
        val entity =
            RestroomEntity().apply {
                this.city = city
                this.name = name
                this.address = address
                this.feeType = FeeType.FREE
                this.locationType = LocationType.STANDALONE
                this.accessibilityType = AccessibilityType.UNKNOWN
                this.placeType = PlaceType.PUBLIC
                this.status = RestroomStatus.ACTIVE
                this.dataSource = DataSourceType.MANUAL
                this.amenities = emptyMap<String, Any>().toMutableMap()
                this.coordinates = point(lon, lat)
                this.restroomMatchKey =
                    MatchKeyGenerator.restroomMatchKey(
                        cityId = cityId,
                        buildingId = null,
                        address = address,
                        name = name,
                        lat = lat,
                        lon = lon,
                        locationType = LocationType.STANDALONE
                    )
            }
        return restroomRepository.saveAndFlush(entity).id ?: error("Failed to insert standalone restroom")
    }

    fun insertAnalyticsUser(data: TestAnalyticsUserData = createTestAnalyticsUserData()): UUID {
        val userId =
            jdbcTemplate.queryForObject(
                """
                INSERT INTO users (tg_user_id, tg_chat_id, source, username, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id
                """.trimIndent(),
                UUID::class.java,
                data.tgUserId,
                data.tgChatId,
                data.source,
                data.username,
                Timestamp.from(data.createdAt),
                Timestamp.from(data.updatedAt)
            ) ?: error("Failed to insert analytics user")

        jdbcTemplate.update(
            """
            INSERT INTO user_analytics (
                user_id,
                searches_count,
                successful_searches_count,
                empty_searches_count,
                details_opened_count,
                routes_clicked_count,
                last_event,
                last_event_at
            ) VALUES (?, 0, 0, 0, 0, 0, NULL, NULL)
            """.trimIndent(),
            userId
        )
        return userId
    }

    fun insertAnalyticsEvent(data: TestAnalyticsEventData): UUID {
        return jdbcTemplate.queryForObject(
            """
            INSERT INTO analytics_events (event, created_at, user_id, source, results_count)
            VALUES (?, ?, ?, ?, ?)
            RETURNING id
            """.trimIndent(),
            UUID::class.java,
            data.event,
            Timestamp.from(data.createdAt),
            data.userId,
            data.source,
            data.resultsCount
        ) ?: error("Failed to insert analytics event")
    }

    fun createTestEnvironment(): TestEnvironment {
        val countryId = insertTestCountry()
        val cityId = insertTestCity(countryId)
        return TestEnvironment(countryId, cityId)
    }

    fun truncateAllTables() {
        val tableNames =
            jdbcTemplate.queryForList(
                """
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = current_schema()
                  AND table_type = 'BASE TABLE'
                """.trimIndent(),
                String::class.java
            ).filterNot { excludedTableNames.contains(it) }

        if (tableNames.isEmpty()) {
            return
        }

        val joinedTableNames = tableNames.joinToString(", ") { "\"$it\"" }
        jdbcTemplate.execute("TRUNCATE TABLE $joinedTableNames RESTART IDENTITY CASCADE")
    }

    data class TestEnvironment(
        val countryId: UUID,
        val cityId: UUID
    )
}
