package integration.repository

import integration.base.BaseIntegrationTest
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.postgresql.util.PSQLException
import yayauheny.by.common.errors.EntityNotFoundException
import yayauheny.by.helpers.DatabaseTestHelper
import yayauheny.by.helpers.TestDataHelpers
import yayauheny.by.repository.impl.CountryRepositoryImpl
import yayauheny.by.tables.references.COUNTRIES
import java.util.UUID

@Tag("integration")
@DisplayName("CountryRepository Error Handling Tests")
class CountryRepositoryErrorHandlingTest : BaseIntegrationTest() {
    private val repository = CountryRepositoryImpl(dslContext)

    @Nested
    @DisplayName("Database Constraint Violation Tests")
    inner class ConstraintViolationTests {
        @Test
        @DisplayName("GIVEN duplicate code WHEN save country THEN throw PSQLException with unique violation (23505)")
        fun given_duplicate_code_when_save_country_then_throw_unique_violation() =
            runTest {
                val duplicateCode = "DUPLICATE"
                val firstCountryDto = TestDataHelpers.createCountryCreateDto(code = duplicateCode)

                repository.save(firstCountryDto)

                val duplicateCountryDto = TestDataHelpers.createCountryCreateDto(code = duplicateCode)

                val exception = assertFailsWith<PSQLException> {
                    repository.save(duplicateCountryDto)
                }

                assertTrue(
                    exception.sqlState == "23505",
                    "Expected unique constraint violation (23505), got ${exception.sqlState}"
                )
                assertNotNull(exception.message)

                val countriesWithSameCode = dslContext
                    .selectCount()
                    .from(COUNTRIES)
                    .where(COUNTRIES.CODE.eq(duplicateCode))
                    .fetchOne()?.value1() ?: 0

                assertTrue(countriesWithSameCode == 1, "Only one country with this code should exist")
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
                val nonExistentId = UUID.randomUUID()
                val updateDto = TestDataHelpers.createCountryUpdateDto()

                val exception = assertFailsWith<EntityNotFoundException> {
                    repository.update(nonExistentId, updateDto)
                }

                assertNotNull(exception.message)
                assertTrue(
                    exception.message?.contains("Страна") == true,
                    "Exception message should mention entity type"
                )

                val countriesWithNonExistentId = dslContext
                    .selectCount()
                    .from(COUNTRIES)
                    .where(COUNTRIES.ID.eq(nonExistentId))
                    .fetchOne()?.value1() ?: 0

                assertTrue(countriesWithNonExistentId == 0, "No country should exist with non-existent ID")
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
                val duplicateCode = "ROLLBACK"
                val firstCountryDto = TestDataHelpers.createCountryCreateDto(code = duplicateCode)

                repository.save(firstCountryDto)

                val duplicateCountryDto = TestDataHelpers.createCountryCreateDto(code = duplicateCode)

                val exception = assertFailsWith<PSQLException> {
                    repository.save(duplicateCountryDto)
                }

                assertTrue(exception.sqlState == "23505", "Expected unique constraint violation")

                val totalCountriesCount = dslContext
                    .selectCount()
                    .from(COUNTRIES)
                    .where(COUNTRIES.CODE.eq(duplicateCode))
                    .fetchOne()?.value1() ?: 0

                assertTrue(totalCountriesCount == 1, "Only the first country should exist after rollback")
            }

        @Test
        @DisplayName("GIVEN update throws exception WHEN transaction rollback THEN no changes persisted")
        fun given_update_throws_exception_when_transaction_rollback_then_no_changes_persisted() =
            runTest {
                val countryDto = TestDataHelpers.createCountryCreateDto()
                val savedCountry = repository.save(countryDto)
                val originalNameRu = savedCountry.nameRu

                val nonExistentId = UUID.randomUUID()
                val updateDto = TestDataHelpers.createCountryUpdateDto(nameRu = "Updated Name")

                assertFailsWith<EntityNotFoundException> {
                    repository.update(nonExistentId, updateDto)
                }

                val unchangedCountry = repository.findById(savedCountry.id)
                assertNotNull(unchangedCountry, "Original country should still exist")
                assertTrue(
                    unchangedCountry?.nameRu == originalNameRu,
                    "Original country nameRu should not be changed"
                )
            }

        @Test
        @DisplayName("GIVEN successful save WHEN transaction commit THEN data persisted")
        fun given_successful_save_when_transaction_commit_then_data_persisted() =
            runTest {
                val countryDto = TestDataHelpers.createCountryCreateDto()

                val savedCountry = repository.save(countryDto)

                assertNotNull(savedCountry, "Saved country should not be null")
                assertNotNull(savedCountry.id, "Saved country should have an ID")

                val foundCountry = repository.findById(savedCountry.id)
                assertNotNull(foundCountry, "Country should be found in database")
                assertTrue(
                    foundCountry?.nameRu == countryDto.nameRu,
                    "Country nameRu should match"
                )
                assertTrue(
                    foundCountry?.nameEn == countryDto.nameEn,
                    "Country nameEn should match"
                )
                assertTrue(
                    foundCountry?.code == countryDto.code,
                    "Country code should match"
                )
            }
    }
}
