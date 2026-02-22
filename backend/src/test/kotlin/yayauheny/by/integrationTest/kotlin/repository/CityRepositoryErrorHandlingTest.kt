package integration.repository

import integration.base.BaseIntegrationTest
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.jooq.impl.DSL
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.postgresql.util.PSQLException
import yayauheny.by.common.errors.EntityNotFoundException
import yayauheny.by.helpers.DatabaseTestHelper
import yayauheny.by.helpers.TestDataHelpers
import yayauheny.by.repository.impl.CityRepositoryImpl
import yayauheny.by.tables.references.CITIES
import yayauheny.by.util.pointExpr
import java.util.UUID

@Tag("integration")
@DisplayName("CityRepository Error Handling Tests")
class CityRepositoryErrorHandlingTest : BaseIntegrationTest() {
    private lateinit var repository: CityRepositoryImpl

    @BeforeEach
    override fun openConnectionAndResetData() {
        super.openConnectionAndResetData()
        repository = CityRepositoryImpl(dslContext)
    }

    @Nested
    @DisplayName("Database Constraint Violation Tests")
    inner class ConstraintViolationTests {
        @Test
        @DisplayName("GIVEN duplicate coordinates WHEN save city THEN throw PSQLException with unique violation (23505)")
        fun given_duplicate_coordinates_when_save_city_then_throw_unique_violation() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
                val duplicateCoordinates =
                    yayauheny.by.model.dto
                        .Coordinates(lat = 55.7558, lon = 37.6176)
                val firstCityDto =
                    TestDataHelpers.createCityCreateDto(
                        countryId = testEnv.countryId,
                        lat = duplicateCoordinates.lat,
                        lon = duplicateCoordinates.lon
                    )

                repository.save(firstCityDto)

                val duplicateCityDto =
                    TestDataHelpers.createCityCreateDto(
                        countryId = testEnv.countryId,
                        lat = duplicateCoordinates.lat,
                        lon = duplicateCoordinates.lon
                    )

                val exception =
                    assertFailsWith<PSQLException> {
                        repository.save(duplicateCityDto)
                    }

                assertTrue(
                    exception.sqlState == "23505",
                    "Expected unique constraint violation (23505), got ${exception.sqlState}"
                )
                assertNotNull(exception.message)

                val citiesWithSameCoordinates =
                    dslContext
                        .selectCount()
                        .from(CITIES)
                        .where(
                            DSL.condition(
                                "ST_Equals({0}, {1})",
                                CITIES.COORDINATES,
                                pointExpr(duplicateCoordinates.lon, duplicateCoordinates.lat, CITIES.COORDINATES)
                            )
                        ).fetchOne()
                        ?.value1() ?: 0

                assertTrue(citiesWithSameCoordinates == 1, "Only one city with these coordinates should exist")
            }

        @Test
        @DisplayName("GIVEN duplicate nameRu for same country WHEN save city THEN throw PSQLException with unique violation (23505)")
        fun given_duplicate_name_ru_for_same_country_when_save_city_then_throw_unique_violation() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
                val duplicateNameRu = "Дубликат"
                val firstCityDto =
                    TestDataHelpers.createCityCreateDto(
                        countryId = testEnv.countryId,
                        nameRu = duplicateNameRu
                    )

                repository.save(firstCityDto)

                val duplicateCityDto =
                    TestDataHelpers.createCityCreateDto(
                        countryId = testEnv.countryId,
                        nameRu = duplicateNameRu
                    )

                val exception =
                    assertFailsWith<PSQLException> {
                        repository.save(duplicateCityDto)
                    }

                assertTrue(
                    exception.sqlState == "23505",
                    "Expected unique constraint violation (23505), got ${exception.sqlState}"
                )
                assertNotNull(exception.message)

                val citiesWithSameNameRu =
                    dslContext
                        .selectCount()
                        .from(CITIES)
                        .where(
                            CITIES.COUNTRY_ID
                                .eq(testEnv.countryId)
                                .and(CITIES.NAME_RU.eq(duplicateNameRu))
                        ).fetchOne()
                        ?.value1() ?: 0

                assertTrue(citiesWithSameNameRu == 1, "Only one city with this nameRu for this country should exist")
            }

        @Test
        @DisplayName("GIVEN duplicate nameEn for same country WHEN save city THEN throw PSQLException with unique violation (23505)")
        fun given_duplicate_name_en_for_same_country_when_save_city_then_throw_unique_violation() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
                val duplicateNameEn = "Duplicate"
                val firstCityDto =
                    TestDataHelpers.createCityCreateDto(
                        countryId = testEnv.countryId,
                        nameEn = duplicateNameEn
                    )

                repository.save(firstCityDto)

                val duplicateCityDto =
                    TestDataHelpers.createCityCreateDto(
                        countryId = testEnv.countryId,
                        nameEn = duplicateNameEn
                    )

                val exception =
                    assertFailsWith<PSQLException> {
                        repository.save(duplicateCityDto)
                    }

                assertTrue(
                    exception.sqlState == "23505",
                    "Expected unique constraint violation (23505), got ${exception.sqlState}"
                )
                assertNotNull(exception.message)

                val citiesWithSameNameEn =
                    dslContext
                        .selectCount()
                        .from(CITIES)
                        .where(
                            CITIES.COUNTRY_ID
                                .eq(testEnv.countryId)
                                .and(CITIES.NAME_EN.eq(duplicateNameEn))
                        ).fetchOne()
                        ?.value1() ?: 0

                assertTrue(citiesWithSameNameEn == 1, "Only one city with this nameEn for this country should exist")
            }

        @Test
        @DisplayName("GIVEN non-existent countryId WHEN save city THEN throw PSQLException with foreign key violation (23503)")
        fun given_non_existent_country_id_when_save_city_then_throw_foreign_key_violation() =
            runTest {
                val nonExistentCountryId = UUID.randomUUID()
                val cityDto = TestDataHelpers.createCityCreateDto(countryId = nonExistentCountryId)

                val exception =
                    assertFailsWith<PSQLException> {
                        repository.save(cityDto)
                    }

                assertTrue(
                    exception.sqlState == "23503",
                    "Expected foreign key violation (23503), got ${exception.sqlState}"
                )
                assertNotNull(exception.message)

                val savedCitiesCount =
                    dslContext
                        .selectCount()
                        .from(CITIES)
                        .where(CITIES.COUNTRY_ID.eq(nonExistentCountryId))
                        .fetchOne()
                        ?.value1() ?: 0

                assertTrue(savedCitiesCount == 0, "No city should be saved with non-existent countryId")
            }
    }

    @Nested
    @DisplayName("Entity Not Found Tests")
    inner class EntityNotFoundTests {
        @Test
        @DisplayName("GIVEN non-existent ID WHEN findById THEN return null")
        fun given_non_existent_id_when_find_by_id_then_return_null() =
            runTest {
                val nonExistentId = UUID.randomUUID()
                val result = repository.findById(nonExistentId)
                assertNull(result, "findById should return null for non-existent ID")
            }

        @Test
        @DisplayName("GIVEN non-existent ID WHEN update THEN throw EntityNotFoundException")
        fun given_non_existent_id_when_update_then_throw_entity_not_found() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
                val nonExistentId = UUID.randomUUID()
                val updateDto = TestDataHelpers.createCityUpdateDto(countryId = testEnv.countryId)

                val exception =
                    assertFailsWith<EntityNotFoundException> {
                        repository.update(nonExistentId, updateDto)
                    }

                assertNotNull(exception.message)
                assertTrue(
                    exception.message?.contains("Город") == true,
                    "Exception message should mention entity type"
                )

                val citiesWithNonExistentId =
                    dslContext
                        .selectCount()
                        .from(CITIES)
                        .where(CITIES.ID.eq(nonExistentId))
                        .fetchOne()
                        ?.value1() ?: 0

                assertTrue(citiesWithNonExistentId == 0, "No city should exist with non-existent ID")
            }

        @Test
        @DisplayName("GIVEN non-existent ID WHEN deleteById THEN return false")
        fun given_non_existent_id_when_delete_by_id_then_return_false() =
            runTest {
                val nonExistentId = UUID.randomUUID()
                val deleteResult = repository.deleteById(nonExistentId)
                assertTrue(!deleteResult, "deleteById should return false for non-existent ID")
            }
    }

    @Nested
    @DisplayName("Transaction Rollback Tests")
    inner class TransactionRollbackTests {
        @Test
        @DisplayName("GIVEN save throws exception WHEN transaction rollback THEN no data persisted")
        fun given_save_throws_exception_when_transaction_rollback_then_no_data_persisted() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
                val duplicateCoordinates =
                    yayauheny.by.model.dto
                        .Coordinates(lat = 55.7558, lon = 37.6176)
                val firstCityDto =
                    TestDataHelpers.createCityCreateDto(
                        countryId = testEnv.countryId,
                        lat = duplicateCoordinates.lat,
                        lon = duplicateCoordinates.lon
                    )

                repository.save(firstCityDto)

                val duplicateCityDto =
                    TestDataHelpers.createCityCreateDto(
                        countryId = testEnv.countryId,
                        lat = duplicateCoordinates.lat,
                        lon = duplicateCoordinates.lon
                    )

                val exception =
                    assertFailsWith<PSQLException> {
                        repository.save(duplicateCityDto)
                    }

                assertTrue(exception.sqlState == "23505", "Expected unique constraint violation")

                val citiesWithDuplicateCoordinates =
                    dslContext
                        .selectCount()
                        .from(CITIES)
                        .where(
                            DSL.condition(
                                "ST_Equals({0}, {1})",
                                CITIES.COORDINATES,
                                pointExpr(duplicateCoordinates.lon, duplicateCoordinates.lat, CITIES.COORDINATES)
                            )
                        ).fetchOne()
                        ?.value1() ?: 0

                assertTrue(citiesWithDuplicateCoordinates == 1, "Only one city with duplicate coordinates should exist after rollback")
            }

        @Test
        @DisplayName("GIVEN update throws exception WHEN transaction rollback THEN no changes persisted")
        fun given_update_throws_exception_when_transaction_rollback_then_no_changes_persisted() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
                val cityDto = TestDataHelpers.createCityCreateDto(countryId = testEnv.countryId)
                val savedCity = repository.save(cityDto)
                val originalNameRu = savedCity.nameRu

                val nonExistentId = UUID.randomUUID()
                val updateDto = TestDataHelpers.createCityUpdateDto(nameRu = "Updated Name")

                assertFailsWith<EntityNotFoundException> {
                    repository.update(nonExistentId, updateDto)
                }

                val unchangedCity = repository.findById(savedCity.id)
                assertNotNull(unchangedCity, "Original city should still exist")
                assertTrue(
                    unchangedCity?.nameRu == originalNameRu,
                    "Original city nameRu should not be changed"
                )
            }

        @Test
        @DisplayName("GIVEN successful save WHEN transaction commit THEN data persisted")
        fun given_successful_save_when_transaction_commit_then_data_persisted() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
                val cityDto = TestDataHelpers.createCityCreateDto(countryId = testEnv.countryId)

                val savedCity = repository.save(cityDto)

                assertNotNull(savedCity, "Saved city should not be null")
                assertNotNull(savedCity.id, "Saved city should have an ID")

                val foundCity = repository.findById(savedCity.id)
                assertNotNull(foundCity, "City should be found in database")
                assertTrue(
                    foundCity?.nameRu == cityDto.nameRu,
                    "City nameRu should match"
                )
                assertTrue(
                    foundCity?.nameEn == cityDto.nameEn,
                    "City nameEn should match"
                )
            }
    }
}
