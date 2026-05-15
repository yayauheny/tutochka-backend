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
import kotlinx.serialization.json.jsonPrimitive
import org.jooq.impl.DSL
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.postgresql.util.PSQLException
import yayauheny.by.common.errors.EntityNotFoundException
import yayauheny.by.helpers.DatabaseTestHelper
import yayauheny.by.importing.dedup.MatchKeyGenerator
import yayauheny.by.helpers.TestDataHelpers
import yayauheny.by.repository.impl.BuildingRepositoryImpl
import yayauheny.by.tables.references.BUILDINGS

@Tag("integration")
@DisplayName("BuildingRepository Error Handling Tests")
class BuildingRepositoryErrorHandlingTest : BaseIntegrationTest() {
    private lateinit var repository: BuildingRepositoryImpl

    @BeforeEach
    override fun openConnectionAndResetData() {
        super.openConnectionAndResetData()
        repository = BuildingRepositoryImpl(dslContext)
    }

    @Nested
    @DisplayName("Database Constraint Violation Tests")
    inner class ConstraintViolationTests {
        @Test
        @DisplayName("GIVEN non-existent cityId WHEN save building THEN throw PSQLException with foreign key violation (23503)")
        fun given_non_existent_city_id_when_save_building_then_throw_foreign_key_violation() =
            runTest {
                val nonExistentCityId = UUID.randomUUID()
                val buildingDto = TestDataHelpers.createBuildingCreateDto(cityId = nonExistentCityId)

                val exception =
                    assertFailsWith<PSQLException> {
                        repository.save(buildingDto)
                    }

                assertEquals(
                    exception.sqlState,
                    "23503",
                    "Expected foreign key violation (23503), got ${exception.sqlState}"
                )
                assertNotNull(exception.message)

                val savedBuildingsCount =
                    dslContext
                        .selectCount()
                        .from(BUILDINGS)
                        .where(BUILDINGS.CITY_ID.eq(nonExistentCityId))
                        .fetchOne()
                        ?.value1() ?: 0

                assertTrue(savedBuildingsCount == 0, "No building should be saved with non-existent cityId")
            }

        @Test
        @DisplayName("GIVEN empty address WHEN save building THEN throw PSQLException with not null violation (23502)")
        fun given_empty_address_when_save_building_then_throw_not_null_violation() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
                val buildingDtoWithEmptyAddress =
                    TestDataHelpers.createBuildingCreateDto(
                        cityId = testEnv.cityId,
                        address = ""
                    )

                val exception =
                    assertFailsWith<PSQLException> {
                        repository.save(buildingDtoWithEmptyAddress)
                    }

                assertTrue(
                    exception.sqlState == "23502",
                    "Expected not null violation (23502), got ${exception.sqlState}"
                )
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
                val updateDto = TestDataHelpers.createBuildingUpdateDto(cityId = testEnv.cityId)

                val exception =
                    assertFailsWith<EntityNotFoundException> {
                        repository.update(nonExistentId, updateDto)
                    }

                assertNotNull(exception.message)
                assertTrue(
                    exception.message?.contains("Здание") == true,
                    "Exception message should mention entity type"
                )
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
    @DisplayName("CRUD Operations Tests")
    inner class CrudOperationsTests {
        @Test
        @DisplayName("GIVEN valid building data WHEN save THEN return saved building")
        fun given_valid_building_data_when_save_then_return_saved_building() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
                val buildingDto = TestDataHelpers.createBuildingCreateDto(cityId = testEnv.cityId)

                val savedBuilding = repository.save(buildingDto)

                assertNotNull(savedBuilding, "Saved building should not be null")
                assertNotNull(savedBuilding.id, "Saved building should have an ID")
                assertTrue(savedBuilding.cityId == testEnv.cityId, "City ID should match")
                assertTrue(savedBuilding.address == buildingDto.address, "Address should match")
                assertTrue(savedBuilding.name == buildingDto.name, "Name should match")
                assertTrue(savedBuilding.buildingType == buildingDto.buildingType, "Building type should match")
            }

        @Test
        @DisplayName("GIVEN existing building WHEN findById THEN return building")
        fun given_existing_building_when_find_by_id_then_return_building() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
                val buildingDto = TestDataHelpers.createBuildingCreateDto(cityId = testEnv.cityId)
                val savedBuilding = repository.save(buildingDto)

                val foundBuilding = repository.findById(savedBuilding.id)

                assertNotNull(foundBuilding, "Building should be found")
                assertTrue(foundBuilding?.id == savedBuilding.id, "Building ID should match")
                assertTrue(foundBuilding?.address == savedBuilding.address, "Address should match")
            }

        @Test
        @DisplayName("GIVEN existing building WHEN update THEN return updated building")
        fun given_existing_building_when_update_then_return_updated_building() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
                val buildingDto = TestDataHelpers.createBuildingCreateDto(cityId = testEnv.cityId)
                val savedBuilding = repository.save(buildingDto)

                val updateDto =
                    TestDataHelpers.createBuildingUpdateDto(
                        cityId = testEnv.cityId,
                        name = "Updated Name",
                        address = "Updated Address"
                    )
                val updatedBuilding = repository.update(savedBuilding.id, updateDto)

                assertNotNull(updatedBuilding, "Updated building should not be null")
                assertTrue(updatedBuilding.name == updateDto.name, "Name should be updated")
                assertTrue(updatedBuilding.address == updateDto.address, "Address should be updated")
            }

        @Test
        @DisplayName("GIVEN existing building WHEN deleteById THEN return true and building marked as deleted")
        fun given_existing_building_when_delete_by_id_then_return_true_and_marked_deleted() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
                val buildingDto = TestDataHelpers.createBuildingCreateDto(cityId = testEnv.cityId)
                val savedBuilding = repository.save(buildingDto)

                val deleteResult = repository.deleteById(savedBuilding.id)

                assertTrue(deleteResult, "deleteById should return true")
                val foundBuilding = repository.findById(savedBuilding.id)
                assertNull(foundBuilding, "Deleted building should not be found")
            }

        @Test
        @DisplayName("GIVEN Yandex building match key WHEN upsertImportedBuildingInTx THEN return saved building")
        fun given_yandex_building_match_key_when_upsert_imported_building_then_return_saved_building() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
                val address = "123 Test Street"
                val lat = 53.9006
                val lon = 27.5590
                val matchKey = MatchKeyGenerator.buildingMatchKey(testEnv.cityId, address, lat, lon)
                val buildingDto =
                    TestDataHelpers.createBuildingCreateDto(
                        cityId = testEnv.cityId,
                        address = address,
                        lat = lat,
                        lon = lon,
                        externalIds =
                            buildJsonObject {
                                put("yandex", JsonPrimitive("yandex-building-123"))
                            }
                    )

                val result =
                    dslContext.transactionResult { configuration ->
                        val txCtx = DSL.using(configuration)
                        repository.upsertImportedBuildingInTx(
                            txCtx = txCtx,
                            provider = "yandex",
                            externalId = "yandex-building-123",
                            createDto = buildingDto,
                            matchKey = matchKey
                        )
                    }

                assertTrue(result.created)
                assertNotNull(result.building.id)
                assertEquals(
                    "yandex-building-123",
                    result.building.externalIds
                        ?.get("yandex")
                        ?.jsonPrimitive
                        ?.content
                )
            }
    }

    @Nested
    @DisplayName("findByExternalId Tests")
    inner class FindByExternalIdTests {
        @Test
        @DisplayName("GIVEN building with external IDs WHEN findByExternalId THEN return building")
        fun given_building_with_external_ids_when_find_by_external_id_then_return_building() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
                val externalIds =
                    buildJsonObject {
                        put("yandex", JsonPrimitive("12345"))
                        put("google", JsonPrimitive("67890"))
                    }
                val buildingDto =
                    TestDataHelpers.createBuildingCreateDto(
                        cityId = testEnv.cityId,
                        externalIds = externalIds
                    )
                val savedBuilding = repository.save(buildingDto)

                val foundBuilding = repository.findByExternalId("yandex", "12345")

                assertNotNull(foundBuilding, "Building should be found by external ID")
                assertTrue(foundBuilding?.id == savedBuilding.id, "Building ID should match")
            }

        @Test
        @DisplayName("GIVEN non-existent external ID WHEN findByExternalId THEN return null")
        fun given_non_existent_external_id_when_find_by_external_id_then_return_null() =
            runTest {
                val result = repository.findByExternalId("yandex", "nonexistent")
                assertNull(result, "findByExternalId should return null for non-existent external ID")
            }
    }
}
