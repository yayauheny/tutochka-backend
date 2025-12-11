package yayauheny.by.helpers

import by.yayauheny.shared.enums.AccessibilityType
import by.yayauheny.shared.enums.DataSourceType
import by.yayauheny.shared.enums.FeeType
import by.yayauheny.shared.enums.PlaceType
import by.yayauheny.shared.enums.RestroomStatus
import java.time.Instant
import java.util.UUID
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.jooq.DSLContext
import org.jooq.impl.DSL
import yayauheny.by.tables.references.CITIES
import yayauheny.by.tables.references.COUNTRIES
import yayauheny.by.tables.references.RESTROOMS
import yayauheny.by.util.pointExpr
import yayauheny.by.util.toJSONBOrEmpty

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
    val accessibilityType: AccessibilityType = AccessibilityType.UNISEX,
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
        accessibilityType: AccessibilityType = AccessibilityType.UNISEX,
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
                .set(RESTROOMS.PHONES, data.phones.toJSONBOrEmpty())
                .set(RESTROOMS.WORK_TIME, data.workTime.toJSONBOrEmpty())
                .set(RESTROOMS.FEE_TYPE, data.feeType.name)
                .set(RESTROOMS.ACCESSIBILITY_TYPE, data.accessibilityType.name)
                .set(RESTROOMS.PLACE_TYPE, data.placeType.id)
                .set(
                    RESTROOMS.COORDINATES,
                    pointExpr(data.lon, data.lat, RESTROOMS.COORDINATES)
                ).set(RESTROOMS.DATA_SOURCE, data.dataSource.name)
                .set(RESTROOMS.STATUS, data.status.name)
                .set(RESTROOMS.AMENITIES, data.amenities.toJSONBOrEmpty())
                .set(RESTROOMS.EXTERNAL_MAPS, data.externalMaps.toJSONBOrEmpty())
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

    fun createTestEnvironment(dslContext: DSLContext): TestEnvironment {
        val countryId = insertTestCountry(dslContext)
        val cityId = insertTestCity(dslContext, countryId)
        return TestEnvironment(countryId, cityId)
    }

    fun truncateAllTables(dslContext: DSLContext) {
        dslContext.transaction { configuration ->
            val ctx = DSL.using(configuration)
            ctx.deleteFrom(RESTROOMS).execute()
            ctx.deleteFrom(CITIES).execute()
            ctx.deleteFrom(COUNTRIES).execute()
        }
    }

    data class TestEnvironment(
        val countryId: UUID,
        val cityId: UUID
    )
}
