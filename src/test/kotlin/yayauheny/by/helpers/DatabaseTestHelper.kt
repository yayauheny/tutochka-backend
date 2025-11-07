package yayauheny.by.helpers

import java.util.UUID
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import yayauheny.by.entity.CityEntity
import yayauheny.by.entity.CountryEntity
import yayauheny.by.entity.RestroomEntity
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.DataSourceType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.RestroomStatus

data class TestCountryData(
    val nameRu: String = "Test Country RU",
    val nameEn: String = "Test Country EN",
    val code: String = "TC"
)

data class TestCityData(
    val nameRu: String = "Test City RU ${UUID.randomUUID().toString().take(8)}",
    val nameEn: String = "Test City EN ${UUID.randomUUID().toString().take(8)}",
    val lat: Double = 55.7558,
    val lon: Double = 37.6176
)

data class TestRestroomData(
    val name: String? = "Test Restroom",
    val description: String? = "Test Description",
    val address: String = "Test Address 123",
    val phones: JsonObject? = buildJsonObject { put("main", JsonPrimitive("+1234567890")) },
    val workTime: JsonObject? = buildJsonObject { put("monday", JsonPrimitive("09:00-18:00")) },
    val feeType: FeeType = FeeType.FREE,
    val accessibilityType: AccessibilityType = AccessibilityType.UNISEX,
    val lat: Double = 55.7558,
    val lon: Double = 37.6176,
    val dataSource: DataSourceType = DataSourceType.MANUAL,
    val status: RestroomStatus = RestroomStatus.ACTIVE,
    val amenities: JsonObject = buildJsonObject { put("wifi", JsonPrimitive("true")) }
)

object DatabaseTestHelper {
    fun createTestCountryData(
        nameRu: String = "Test Country RU",
        nameEn: String = "Test Country EN",
        code: String = "TC"
    ) = TestCountryData(nameRu, nameEn, code)

    fun createTestCityData(
        nameRu: String = "Test City RU ${UUID.randomUUID().toString().take(8)}",
        nameEn: String = "Test City EN ${UUID.randomUUID().toString().take(8)}",
        lat: Double = 55.7558,
        lon: Double = 37.6176
    ) = TestCityData(nameRu, nameEn, lat, lon)

    fun createTestRestroomData(
        name: String? = "Test Restroom",
        description: String? = "Test Description",
        address: String = "Test Address 123",
        phones: JsonObject? = buildJsonObject { put("main", JsonPrimitive("+1234567890")) },
        workTime: JsonObject? = buildJsonObject { put("monday", JsonPrimitive("09:00-18:00")) },
        feeType: FeeType = FeeType.FREE,
        accessibilityType: AccessibilityType = AccessibilityType.UNISEX,
        lat: Double = 55.7558,
        lon: Double = 37.6176,
        dataSource: DataSourceType = DataSourceType.MANUAL,
        status: RestroomStatus = RestroomStatus.ACTIVE,
        amenities: JsonObject = buildJsonObject { put("wifi", JsonPrimitive("true")) }
    ) = TestRestroomData(name, description, address, phones, workTime, feeType, accessibilityType, lat, lon, dataSource, status, amenities)

    fun insertTestCountry(
        database: Database,
        data: TestCountryData = createTestCountryData()
    ): UUID =
        transaction(database) {
            CountryEntity
                .new {
                    this.nameRu = data.nameRu
                    this.nameEn = data.nameEn
                    this.code = data.code
                }.id.value
        }

    fun insertTestCity(
        database: Database,
        countryId: UUID,
        data: TestCityData = createTestCityData()
    ): UUID =
        transaction(database) {
            CityEntity
                .new {
                    this.country = CountryEntity.findById(countryId)!!
                    this.nameRu = data.nameRu
                    this.nameEn = data.nameEn
                    this.lat = data.lat
                    this.lon = data.lon
                }.id.value
        }

    fun insertTestRestroom(
        database: Database,
        cityId: UUID? = null,
        data: TestRestroomData = createTestRestroomData()
    ): UUID =
        transaction(database) {
            RestroomEntity
                .new {
                    this.city = cityId?.let { CityEntity.findById(it) }
                    this.name = data.name
                    this.description = data.description
                    this.address = data.address
                    this.phones = data.phones
                    this.workTime = data.workTime
                    this.feeType = data.feeType
                    this.accessibilityType = data.accessibilityType
                    this.coordinates = GeoPoint(longitude = data.lon, latitude = data.lat)
                    this.dataSource = data.dataSource
                    this.status = data.status
                    this.amenities = data.amenities
                }.id.value
        }

    fun createTestEnvironment(database: Database): TestEnvironment {
        val countryId = insertTestCountry(database)
        val cityId = insertTestCity(database, countryId)
        return TestEnvironment(countryId, cityId)
    }

    data class TestEnvironment(
        val countryId: UUID,
        val cityId: UUID
    )
}
