package integration.repository

import integration.base.BaseIntegrationTest
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.postgresql.util.PSQLException
import yayauheny.by.helpers.DatabaseTestHelper
import yayauheny.by.helpers.TestDataHelpers
import yayauheny.by.repository.impl.SubwayRepositoryImpl

@Tag("integration")
@DisplayName("SubwayRepository Tests")
class SubwayRepositoryTest : BaseIntegrationTest() {
    private lateinit var repository: SubwayRepositoryImpl

    @BeforeEach
    override fun openConnectionAndResetData() {
        super.openConnectionAndResetData()
        repository = SubwayRepositoryImpl(dslContext)
    }

    @Nested
    @DisplayName("Subway Line CRUD Tests")
    inner class SubwayLineCrudTests {
        @Test
        @DisplayName("GIVEN valid line data WHEN createLine THEN return saved line")
        fun given_valid_line_data_when_create_line_then_return_saved_line() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
                val lineDto = TestDataHelpers.createSubwayLineCreateDto(cityId = testEnv.cityId)

                val savedLine = repository.createLine(lineDto)

                assertNotNull(savedLine, "Saved line should not be null")
                assertNotNull(savedLine.id, "Saved line should have an ID")
                assertTrue(savedLine.cityId == testEnv.cityId, "City ID should match")
                assertTrue(savedLine.nameRu == lineDto.nameRu, "Russian name should match")
                assertTrue(savedLine.nameEn == lineDto.nameEn, "English name should match")
                assertTrue(savedLine.hexColor == lineDto.hexColor, "Hex color should match")
            }

        @Test
        @DisplayName("GIVEN existing line WHEN findLineById THEN return line")
        fun given_existing_line_when_find_line_by_id_then_return_line() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
                val lineDto = TestDataHelpers.createSubwayLineCreateDto(cityId = testEnv.cityId)
                val savedLine = repository.createLine(lineDto)

                val foundLine = repository.findLineById(savedLine.id)

                assertNotNull(foundLine, "Line should be found")
                assertTrue(foundLine?.id == savedLine.id, "Line ID should match")
            }

        @Test
        @DisplayName("GIVEN non-existent line ID WHEN findLineById THEN return null")
        fun given_non_existent_line_id_when_find_line_by_id_then_return_null() =
            runTest {
                val nonExistentId = UUID.randomUUID()
                val result = repository.findLineById(nonExistentId)
                assertNull(result, "findLineById should return null for non-existent ID")
            }

        @Test
        @DisplayName("GIVEN city with lines WHEN findAllLinesByCityId THEN return all lines")
        fun given_city_with_lines_when_find_all_lines_by_city_id_then_return_all_lines() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
                val line1 =
                    repository.createLine(
                        TestDataHelpers.createSubwayLineCreateDto(
                            cityId = testEnv.cityId,
                            nameRu = "Линия 1",
                            hexColor = "#FF0000"
                        )
                    )
                val line2 =
                    repository.createLine(
                        TestDataHelpers.createSubwayLineCreateDto(
                            cityId = testEnv.cityId,
                            nameRu = "Линия 2",
                            hexColor = "#00FF00"
                        )
                    )

                val lines = repository.findAllLinesByCityId(testEnv.cityId)

                assertTrue(lines.size == 2, "Should return 2 lines")
                assertTrue(lines.any { it.id == line1.id }, "Should contain line 1")
                assertTrue(lines.any { it.id == line2.id }, "Should contain line 2")
            }
    }

    @Nested
    @DisplayName("Subway Station CRUD Tests")
    inner class SubwayStationCrudTests {
        @Test
        @DisplayName("GIVEN valid station data WHEN createStation THEN return saved station")
        fun given_valid_station_data_when_create_station_then_return_saved_station() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
                val line =
                    repository.createLine(
                        TestDataHelpers.createSubwayLineCreateDto(cityId = testEnv.cityId)
                    )
                val stationDto =
                    TestDataHelpers.createSubwayStationCreateDto(
                        subwayLineId = line.id
                    )

                val savedStation = repository.createStation(stationDto)

                assertNotNull(savedStation, "Saved station should not be null")
                assertNotNull(savedStation.id, "Saved station should have an ID")
                assertTrue(savedStation.subwayLineId == line.id, "Line ID should match")
                assertTrue(savedStation.nameRu == stationDto.nameRu, "Russian name should match")
            }

        @Test
        @DisplayName("GIVEN existing station WHEN findStationById THEN return station")
        fun given_existing_station_when_find_station_by_id_then_return_station() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
                val line =
                    repository.createLine(
                        TestDataHelpers.createSubwayLineCreateDto(cityId = testEnv.cityId)
                    )
                val stationDto =
                    TestDataHelpers.createSubwayStationCreateDto(
                        subwayLineId = line.id
                    )
                val savedStation = repository.createStation(stationDto)

                val foundStation = repository.findStationById(savedStation.id)

                assertNotNull(foundStation, "Station should be found")
                assertTrue(foundStation?.id == savedStation.id, "Station ID should match")
            }

        @Test
        @DisplayName("GIVEN line with stations WHEN findStationsByLineId THEN return all stations")
        fun given_line_with_stations_when_find_stations_by_line_id_then_return_all_stations() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
                val line =
                    repository.createLine(
                        TestDataHelpers.createSubwayLineCreateDto(cityId = testEnv.cityId)
                    )
                val station1 =
                    repository.createStation(
                        TestDataHelpers.createSubwayStationCreateDto(
                            subwayLineId = line.id,
                            nameRu = "Станция 1"
                        )
                    )
                val station2 =
                    repository.createStation(
                        TestDataHelpers.createSubwayStationCreateDto(
                            subwayLineId = line.id,
                            nameRu = "Станция 2"
                        )
                    )

                val stations = repository.findStationsByLineId(line.id)

                assertTrue(stations.size == 2, "Should return 2 stations")
                assertTrue(stations.any { it.id == station1.id }, "Should contain station 1")
                assertTrue(stations.any { it.id == station2.id }, "Should contain station 2")
            }
    }

    @Nested
    @DisplayName("Geo Search Tests")
    inner class GeoSearchTests {
        @Test
        @DisplayName("GIVEN stations WHEN findNearestStation THEN return nearest station")
        fun given_stations_when_find_nearest_station_then_return_nearest_station() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
                val line =
                    repository.createLine(
                        TestDataHelpers.createSubwayLineCreateDto(cityId = testEnv.cityId)
                    )

                repository.createStation(
                    TestDataHelpers.createSubwayStationCreateDto(
                        subwayLineId = line.id,
                        nameRu = "Дальняя станция",
                        lat = 53.9100,
                        lon = 27.5700
                    )
                )

                val nearestStation =
                    repository.createStation(
                        TestDataHelpers.createSubwayStationCreateDto(
                            subwayLineId = line.id,
                            nameRu = "Ближайшая станция",
                            lat = 53.9006,
                            lon = 27.5590
                        )
                    )

                val foundNearest = repository.findNearestStation(53.9006, 27.5590)

                assertNotNull(foundNearest, "Nearest station should be found")
                assertEquals(
                    foundNearest.id,
                    nearestStation.id,
                    "Should return the nearest station"
                )
            }

        @Test
        @DisplayName("GIVEN no stations WHEN findNearestStation THEN return null")
        fun given_no_stations_when_find_nearest_station_then_return_null() =
            runTest {
                val result = repository.findNearestStation(53.9006, 27.5590)
                assertNull(result, "findNearestStation should return null when no stations exist")
            }
    }

    @Nested
    @DisplayName("setNearestStationForRestroom Tests")
    inner class SetNearestStationForRestroomTests {
        @Test
        @DisplayName("GIVEN restroom and stations WHEN setNearestStationForRestroom THEN update restroom")
        fun given_restroom_and_stations_when_set_nearest_station_then_update_restroom() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
                val line =
                    repository.createLine(
                        TestDataHelpers.createSubwayLineCreateDto(cityId = testEnv.cityId)
                    )
                val station =
                    repository.createStation(
                        TestDataHelpers.createSubwayStationCreateDto(
                            subwayLineId = line.id,
                            lat = 53.9006,
                            lon = 27.5590
                        )
                    )

                val restroomDto =
                    TestDataHelpers.createRestroomCreateDto(
                        cityId = testEnv.cityId,
                        lat = 53.9006,
                        lon = 27.5590
                    )
                val restroomRepository =
                    yayauheny.by.repository.impl
                        .RestroomRepositoryImpl(dslContext)
                val savedRestroom = restroomRepository.save(restroomDto)

                val result =
                    repository.setNearestStationForRestroom(
                        savedRestroom.id,
                        53.9006,
                        27.5590
                    )

                assertTrue(result, "setNearestStationForRestroom should return true")

                val updatedRestroom = restroomRepository.findById(savedRestroom.id)
                assertNotNull(updatedRestroom, "Restroom should still exist")
                assertEquals(
                    updatedRestroom.subwayStationId,
                    station.id,
                    "Restroom should have nearest station set"
                )
            }
    }

    @Nested
    @DisplayName("Database Constraint Violation Tests")
    inner class ConstraintViolationTests {
        @Test
        @DisplayName("GIVEN non-existent cityId WHEN createLine THEN throw PSQLException with foreign key violation (23503)")
        fun given_non_existent_city_id_when_create_line_then_throw_foreign_key_violation() =
            runTest {
                val nonExistentCityId = UUID.randomUUID()
                val lineDto = TestDataHelpers.createSubwayLineCreateDto(cityId = nonExistentCityId)

                val exception =
                    assertFailsWith<PSQLException> {
                        repository.createLine(lineDto)
                    }

                assertEquals(
                    exception.sqlState,
                    "23503",
                    "Expected foreign key violation (23503), got ${exception.sqlState}"
                )
            }

        @Test
        @DisplayName("GIVEN non-existent lineId WHEN createStation THEN throw PSQLException with foreign key violation (23503)")
        fun given_non_existent_line_id_when_create_station_then_throw_foreign_key_violation() =
            runTest {
                val nonExistentLineId = UUID.randomUUID()
                val stationDto =
                    TestDataHelpers.createSubwayStationCreateDto(
                        subwayLineId = nonExistentLineId
                    )

                val exception =
                    assertFailsWith<PSQLException> {
                        repository.createStation(stationDto)
                    }

                assertTrue(
                    exception.sqlState == "23503",
                    "Expected foreign key violation (23503), got ${exception.sqlState}"
                )
            }
    }

    @Nested
    @DisplayName("Subway Line Local Names Tests")
    inner class SubwayLineLocalNamesTests {
        @Test
        @DisplayName("GIVEN line without local name WHEN createLine THEN save with null local fields")
        fun given_line_without_local_name_when_create_line_then_save_with_null_local_fields() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
                val lineDto =
                    TestDataHelpers.createSubwayLineCreateDto(
                        cityId = testEnv.cityId,
                        shortCode = null
                    )

                val savedLine = repository.createLine(lineDto)

                assertNotNull(savedLine, "Saved line should not be null")
                assertNull(savedLine.shortCode, "Short code should be null")
            }
    }

    @Nested
    @DisplayName("Subway Station Local Names and Transfer Tests")
    inner class SubwayStationLocalNamesAndTransferTests {
        @Test
        @DisplayName("GIVEN transfer station WHEN createStation THEN save isTransfer flag")
        fun given_transfer_station_when_create_station_then_save_is_transfer_flag() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
                val line =
                    repository.createLine(
                        TestDataHelpers.createSubwayLineCreateDto(cityId = testEnv.cityId)
                    )
                val stationDto =
                    TestDataHelpers.createSubwayStationCreateDto(
                        subwayLineId = line.id,
                        isTransfer = true
                    )

                val savedStation = repository.createStation(stationDto)

                assertNotNull(savedStation, "Saved station should not be null")
                assertTrue(savedStation.isTransfer, "isTransfer should be true")
            }

        @Test
        @DisplayName("GIVEN station with external IDs WHEN createStation THEN save external IDs")
        fun given_station_with_external_ids_when_create_station_then_save_external_ids() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
                val line =
                    repository.createLine(
                        TestDataHelpers.createSubwayLineCreateDto(cityId = testEnv.cityId)
                    )
                val externalIds =
                    buildJsonObject {
                        put("2gis", JsonPrimitive("123456"))
                        put("yandex", JsonPrimitive("789012"))
                    }
                val stationDto =
                    TestDataHelpers.createSubwayStationCreateDto(
                        subwayLineId = line.id,
                        externalIds = externalIds
                    )

                val savedStation = repository.createStation(stationDto)

                assertNotNull(savedStation, "Saved station should not be null")
                // External IDs are stored but not returned in response DTO currently
                // This test verifies the field is saved without error
            }
    }

    @Nested
    @DisplayName("SubwayStationResponseDto displayName Tests")
    inner class DisplayNameTests {
        @Test
        @DisplayName("GIVEN station WHEN displayName default THEN return Russian name")
        fun given_station_when_display_name_with_ru_then_return_russian_name() {
            val station =
                TestDataHelpers.createSubwayStationResponseDto(
                    nameRu = "Площадь Победы",
                    nameEn = "Victory Square"
                )

            val displayName = station.displayName()

            assertTrue(displayName == "Площадь Победы", "Should return Russian name")
        }

        @Test
        @DisplayName("GIVEN station WHEN displayName with 'en' THEN return English name")
        fun given_station_when_display_name_with_en_then_return_english_name() {
            val station =
                TestDataHelpers.createSubwayStationResponseDto(
                    nameRu = "Площадь Победы",
                    nameEn = "Victory Square"
                )

            val displayName = station.displayName("en")

            assertTrue(displayName == "Victory Square", "Should return English name")
        }
    }
}
