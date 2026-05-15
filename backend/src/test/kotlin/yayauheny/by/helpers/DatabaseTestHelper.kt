package yayauheny.by.helpers

import java.time.Instant
import java.util.UUID
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.jooq.DSLContext
import org.jooq.impl.DSL
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.DataSourceType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.GenderType
import yayauheny.by.model.enums.LocationType
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.importing.dedup.MatchKeyGenerator
import yayauheny.by.model.analytics.AnalyticsSource
import yayauheny.by.tables.references.BUILDINGS
import yayauheny.by.tables.references.ANALYTICS_EVENTS
import yayauheny.by.tables.references.CITIES
import yayauheny.by.tables.references.COUNTRIES
import yayauheny.by.tables.references.RESTROOM_IMPORTS
import yayauheny.by.tables.references.RESTROOMS
import yayauheny.by.tables.references.USER_ANALYTICS
import yayauheny.by.tables.references.USERS
import yayauheny.by.util.pointExpr
import yayauheny.by.util.toJSONB

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
    val accessNote: String? = "Доступен для людей с ограниченными возможностями",
    val directionGuide: String? = "Находится на первом этаже, рядом с главным входом",
    val inheritBuildingSchedule: Boolean = false,
    val hasPhotos: Boolean = true
)

data class TestAnalyticsUserData(
    val tgUserId: String = "enc-user-${UUID.randomUUID()}",
    val tgChatId: String = "enc-chat-${UUID.randomUUID()}",
    val source: String = AnalyticsSource.TELEGRAM_BOT.value,
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
        accessNote,
        directionGuide,
        inheritBuildingSchedule,
        hasPhotos
    )

    fun createTestAnalyticsUserData(
        tgUserId: String = "enc-user-${UUID.randomUUID()}",
        tgChatId: String = "enc-chat-${UUID.randomUUID()}",
        source: String = AnalyticsSource.TELEGRAM_BOT.value,
        username: String? = null,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = createdAt
    ) = TestAnalyticsUserData(tgUserId, tgChatId, source, username, createdAt, updatedAt)

    fun createTestAnalyticsEventData(
        event: String,
        userId: UUID? = null,
        source: String = AnalyticsSource.TELEGRAM_BOT.value,
        resultsCount: Int? = null,
        createdAt: Instant = Instant.now()
    ) = TestAnalyticsEventData(event, userId, source, resultsCount, createdAt)

    fun insertTestCountry(
        dslContext: DSLContext,
        data: TestCountryData = createTestCountryData()
    ): UUID =
        dslContext.transactionResult { configuration ->
            val ctx = DSL.using(configuration)
            val now = Instant.now()
            val id = UUID.randomUUID()

            ctx
                .insertInto(COUNTRIES)
                .set(COUNTRIES.ID, id)
                .set(COUNTRIES.CODE, data.code)
                .set(COUNTRIES.NAME_RU, data.nameRu)
                .set(COUNTRIES.NAME_EN, data.nameEn)
                .set(COUNTRIES.CREATED_AT, now)
                .set(COUNTRIES.UPDATED_AT, now)
                .returning(COUNTRIES.ID)
                .fetchOne()
                ?.getValue(COUNTRIES.ID)
                ?: error("Failed to insert test country")
        }

    fun insertTestCity(
        dslContext: DSLContext,
        countryId: UUID,
        data: TestCityData = createTestCityData()
    ): UUID =
        dslContext.transactionResult { configuration ->
            val ctx = DSL.using(configuration)
            val now = Instant.now()
            val id = UUID.randomUUID()

            ctx
                .insertInto(CITIES)
                .set(CITIES.ID, id)
                .set(CITIES.COUNTRY_ID, countryId)
                .set(CITIES.NAME_RU, data.nameRu)
                .set(CITIES.NAME_EN, data.nameEn)
                .set(
                    CITIES.COORDINATES,
                    pointExpr(data.lon, data.lat, CITIES.COORDINATES)
                ).set(CITIES.CREATED_AT, now)
                .set(CITIES.UPDATED_AT, now)
                .returning(CITIES.ID)
                .fetchOne()
                ?.getValue(CITIES.ID)
                ?: error("Failed to insert test city")
        }

    fun insertTestRestroom(
        dslContext: DSLContext,
        cityId: UUID? = null,
        data: TestRestroomData = createTestRestroomData()
    ): UUID =
        dslContext.transactionResult { configuration ->
            val ctx = DSL.using(configuration)
            val now = Instant.now()
            val id = UUID.randomUUID()

            ctx
                .insertInto(RESTROOMS)
                .set(RESTROOMS.ID, id)
                .set(RESTROOMS.CITY_ID, cityId)
                .set(RESTROOMS.BUILDING_ID, data.buildingId)
                .set(RESTROOMS.SUBWAY_STATION_ID, data.subwayStationId)
                .set(RESTROOMS.NAME, data.name)
                .set(RESTROOMS.ADDRESS, data.address)
                .set(RESTROOMS.PHONES, data.phones.toJSONB())
                .set(RESTROOMS.WORK_TIME, data.workTime.toJSONB())
                .set(RESTROOMS.FEE_TYPE, data.feeType.name)
                .set(
                    DSL.field(
                        "gender_type",
                        org.jooq.impl.SQLDataType
                            .VARCHAR(20)
                    ),
                    data.genderType.name
                ).set(RESTROOMS.ACCESSIBILITY_TYPE, data.accessibilityType.name)
                .set(RESTROOMS.PLACE_TYPE, data.placeType.code)
                .set(
                    RESTROOMS.COORDINATES,
                    pointExpr(data.lon, data.lat, RESTROOMS.COORDINATES)
                ).set(RESTROOMS.DATA_SOURCE, data.dataSource.name)
                .set(RESTROOMS.STATUS, data.status.name)
                .set(RESTROOMS.AMENITIES, data.amenities.toJSONB())
                .set(RESTROOMS.EXTERNAL_MAPS, data.externalMaps.toJSONB())
                .set(RESTROOMS.ACCESS_NOTE, data.accessNote)
                .set(RESTROOMS.DIRECTION_GUIDE, data.directionGuide)
                .set(RESTROOMS.INHERIT_BUILDING_SCHEDULE, data.inheritBuildingSchedule)
                .set(RESTROOMS.HAS_PHOTOS, data.hasPhotos)
                .set(RESTROOMS.CREATED_AT, now)
                .set(RESTROOMS.UPDATED_AT, now)
                .returning(RESTROOMS.ID)
                .fetchOne()
                ?.getValue(RESTROOMS.ID)
                ?: error("Failed to insert test restroom")
        }

    fun insertTestBuilding(
        dslContext: DSLContext,
        cityId: UUID,
        address: String,
        name: String? = null,
        lat: Double,
        lon: Double
    ): UUID =
        dslContext.transactionResult { configuration ->
            val ctx = DSL.using(configuration)
            val now = Instant.now()
            val id = UUID.randomUUID()
            val buildingMatchKeyField =
                DSL.field(
                    "building_match_key",
                    org.jooq.impl.SQLDataType
                        .VARCHAR(64)
                )

            ctx
                .insertInto(BUILDINGS)
                .set(BUILDINGS.ID, id)
                .set(BUILDINGS.CITY_ID, cityId)
                .set(BUILDINGS.NAME, name)
                .set(BUILDINGS.ADDRESS, address)
                .set(buildingMatchKeyField, MatchKeyGenerator.buildingMatchKey(cityId, address, lat, lon))
                .set(BUILDINGS.COORDINATES, pointExpr(lon, lat, BUILDINGS.COORDINATES))
                .set(BUILDINGS.CREATED_AT, now)
                .set(BUILDINGS.UPDATED_AT, now)
                .returning(BUILDINGS.ID)
                .fetchOne()
                ?.getValue(BUILDINGS.ID)
                ?: error("Failed to insert test building")
        }

    fun insertStandaloneRestroom(
        dslContext: DSLContext,
        cityId: UUID,
        name: String,
        address: String,
        lat: Double,
        lon: Double
    ): UUID =
        dslContext.transactionResult { configuration ->
            val ctx = DSL.using(configuration)
            val now = Instant.now()
            val id = UUID.randomUUID()
            val locationTypeField =
                DSL.field(
                    "location_type",
                    org.jooq.impl.SQLDataType
                        .VARCHAR(20)
                )
            val restroomMatchKeyField =
                DSL.field(
                    "restroom_match_key",
                    org.jooq.impl.SQLDataType
                        .VARCHAR(64)
                )

            ctx
                .insertInto(RESTROOMS)
                .set(RESTROOMS.ID, id)
                .set(RESTROOMS.CITY_ID, cityId)
                .set(RESTROOMS.NAME, name)
                .set(RESTROOMS.ADDRESS, address)
                .set(RESTROOMS.FEE_TYPE, FeeType.FREE.name)
                .set(locationTypeField, LocationType.STANDALONE.name)
                .set(RESTROOMS.ACCESSIBILITY_TYPE, AccessibilityType.UNKNOWN.name)
                .set(RESTROOMS.PLACE_TYPE, PlaceType.PUBLIC.code)
                .set(RESTROOMS.STATUS, RestroomStatus.ACTIVE.name)
                .set(RESTROOMS.DATA_SOURCE, DataSourceType.MANUAL.name)
                .set(RESTROOMS.AMENITIES, buildJsonObject { }.toJSONB())
                .set(
                    restroomMatchKeyField,
                    MatchKeyGenerator.restroomMatchKey(
                        cityId = cityId,
                        buildingId = null,
                        address = address,
                        name = name,
                        lat = lat,
                        lon = lon,
                        locationType = LocationType.STANDALONE
                    )
                ).set(RESTROOMS.COORDINATES, pointExpr(lon, lat, RESTROOMS.COORDINATES))
                .set(RESTROOMS.CREATED_AT, now)
                .set(RESTROOMS.UPDATED_AT, now)
                .returning(RESTROOMS.ID)
                .fetchOne()
                ?.getValue(RESTROOMS.ID)
                ?: error("Failed to insert standalone restroom")
        }

    fun insertAnalyticsUser(
        dslContext: DSLContext,
        data: TestAnalyticsUserData = createTestAnalyticsUserData()
    ): UUID =
        dslContext.transactionResult { configuration ->
            val ctx = DSL.using(configuration)
            val id = UUID.randomUUID()

            ctx
                .insertInto(USERS)
                .set(USERS.ID, id)
                .set(USERS.TG_USER_ID, data.tgUserId)
                .set(USERS.TG_CHAT_ID, data.tgChatId)
                .set(USERS.SOURCE, data.source)
                .set(USERS.USERNAME, data.username)
                .set(USERS.CREATED_AT, data.createdAt)
                .set(USERS.UPDATED_AT, data.updatedAt)
                .returning(USERS.ID)
                .fetchOne()
                ?.getValue(USERS.ID)
                ?: error("Failed to insert analytics user")
        }

    fun insertAnalyticsEvent(
        dslContext: DSLContext,
        data: TestAnalyticsEventData
    ): UUID =
        dslContext.transactionResult { configuration ->
            val ctx = DSL.using(configuration)
            val id = UUID.randomUUID()

            ctx
                .insertInto(ANALYTICS_EVENTS)
                .set(ANALYTICS_EVENTS.ID, id)
                .set(ANALYTICS_EVENTS.EVENT, data.event)
                .set(ANALYTICS_EVENTS.CREATED_AT, data.createdAt)
                .set(ANALYTICS_EVENTS.USER_ID, data.userId)
                .set(ANALYTICS_EVENTS.SOURCE, data.source)
                .set(ANALYTICS_EVENTS.RESULTS_COUNT, data.resultsCount)
                .returning(ANALYTICS_EVENTS.ID)
                .fetchOne()
                ?.getValue(ANALYTICS_EVENTS.ID)
                ?: error("Failed to insert analytics event")
        }

    fun createTestEnvironment(dslContext: DSLContext): TestEnvironment {
        val countryId = insertTestCountry(dslContext)
        val cityId = insertTestCity(dslContext, countryId)
        return TestEnvironment(countryId, cityId)
    }

    fun truncateAllTables(dslContext: DSLContext) {
        dslContext.transaction { configuration ->
            val ctx = DSL.using(configuration)
            ctx.deleteFrom(ANALYTICS_EVENTS).execute()
            ctx.deleteFrom(USER_ANALYTICS).execute()
            ctx.deleteFrom(USERS).execute()
            ctx.deleteFrom(RESTROOM_IMPORTS).execute()
            ctx.deleteFrom(RESTROOMS).execute()
            ctx.deleteFrom(BUILDINGS).execute()
            ctx.deleteFrom(CITIES).execute()
            ctx.deleteFrom(COUNTRIES).execute()
        }
    }

    data class TestEnvironment(
        val countryId: UUID,
        val cityId: UUID
    )
}
