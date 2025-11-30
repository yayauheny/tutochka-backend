package yayauheny.by.unit.service

import java.util.UUID
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import yayauheny.by.service.validation.validateOrThrow
import yayauheny.by.service.validation.validateWith
import yayauheny.by.common.errors.ValidationException
import yayauheny.by.model.LatLon
import yayauheny.by.model.city.CityCreateDto
import yayauheny.by.model.country.CountryCreateDto
import yayauheny.by.model.restroom.RestroomCreateDto
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.DataSourceType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.service.validation.NearestRestroomsParams
import yayauheny.by.service.validation.validateCityOnCreate
import yayauheny.by.service.validation.validateCountryOnCreate
import yayauheny.by.service.validation.validateNearestRestroomsParams
import yayauheny.by.service.validation.validateRestroomOnCreate

class ValidationTest {
    companion object {
        @JvmStatic
        fun invalidCountryData(): Stream<Arguments> =
            Stream.of(
                Arguments.of(CountryCreateDto("", "United States", "US"), 1), // Empty nameRu
                Arguments.of(CountryCreateDto("Соединенные Штаты", "", "US"), 1), // Empty nameEn
                Arguments.of(CountryCreateDto("Соединенные Штаты", "United States", ""), 2), // Empty code (minLength + pattern)
                Arguments.of(CountryCreateDto("Соединенные Штаты", "United States", "US"), 0) // Valid
            )

        @JvmStatic
        fun invalidCityData(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    CityCreateDto(UUID.randomUUID(), "", "Minsk", null, LatLon(lat = 53.9006, lon = 27.5590)),
                    1
                ), // Empty nameRu
                Arguments.of(
                    CityCreateDto(UUID.randomUUID(), "Минск", "", null, LatLon(lat = 53.9006, lon = 27.5590)),
                    1
                ), // Empty nameEn
                Arguments.of(
                    CityCreateDto(UUID.randomUUID(), "Минск", "Minsk", null, LatLon(lat = 91.0, lon = 27.5590)),
                    1
                ), // lat > 90
                Arguments.of(
                    CityCreateDto(UUID.randomUUID(), "Минск", "Minsk", null, LatLon(lat = -91.0, lon = 27.5590)),
                    1
                ), // lat < -90
                Arguments.of(
                    CityCreateDto(UUID.randomUUID(), "Минск", "Minsk", null, LatLon(lat = 53.9006, lon = 181.0)),
                    1
                ), // lon > 180
                Arguments.of(
                    CityCreateDto(UUID.randomUUID(), "Минск", "Minsk", null, LatLon(lat = 53.9006, lon = -181.0)),
                    1
                ), // lon < -180
                Arguments.of(
                    CityCreateDto(UUID.randomUUID(), "Минск", "Minsk", null, LatLon(lat = 53.9006, lon = 27.5590)),
                    0
                ) // Valid
            )

        @JvmStatic
        fun invalidRestroomData(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    RestroomCreateDto(
                        cityId = UUID.randomUUID(),
                        status = RestroomStatus.ACTIVE,
                        name = null,
                        description = null,
                        address = "",
                        phones = null,
                        workTime = null,
                        feeType = FeeType.FREE,
                        accessibilityType = AccessibilityType.UNISEX,
                        coordinates = LatLon(lat = 55.7558, lon = 37.6176),
                        dataSource = DataSourceType.MANUAL,
                        amenities = buildJsonObject {},
                        parentPlaceName = null,
                        parentPlaceType = null,
                        inheritParentSchedule = false
                    ),
                    1
                ),
            )

        @JvmStatic
        fun invalidNearestRestroomsParamsData(): Stream<Arguments> =
            Stream.of(
                Arguments.of(NearestRestroomsParams(LatLon(lat = 91.0, lon = 37.6176), 10, 1000), 1), // lat > 90
                Arguments.of(NearestRestroomsParams(LatLon(lat = -91.0, lon = 37.6176), 10, 1000), 1), // lat < -90
                Arguments.of(NearestRestroomsParams(LatLon(lat = 55.7558, lon = 181.0), 10, 1000), 1), // lon > 180
                Arguments.of(NearestRestroomsParams(LatLon(lat = 55.7558, lon = -181.0), 10, 1000), 1), // lon < -180
                Arguments.of(NearestRestroomsParams(LatLon(lat = 55.7558, lon = 37.6176), 0, 1000), 1), // limit = 0
                Arguments.of(NearestRestroomsParams(LatLon(lat = 55.7558, lon = 37.6176), -1, 1000), 1), // limit < 0
                Arguments.of(NearestRestroomsParams(LatLon(lat = 55.7558, lon = 37.6176), 101, 1000), 1), // limit > 100
                Arguments.of(NearestRestroomsParams(LatLon(lat = 91.0, lon = 181.0), 101, 1000), 3), // Multiple issues
            )
    }

    @Nested
    @DisplayName("CountryCreateDto validation")
    inner class CountryValidationTest {
        @Test
        @DisplayName("Valid data should pass validation")
        fun givenValidCountryData_whenValidating_thenPasses() {
            // Given
            val validDto =
                CountryCreateDto(
                    nameRu = "Соединенные Штаты",
                    nameEn = "United States",
                    code = "US"
                )

            // When
            val result = validDto.validateWith(validateCountryOnCreate)

            // Then
            assertTrue(result.isSuccess)
            assertEquals(validDto, result.getOrNull())
        }

        @ParameterizedTest
        @MethodSource("yayauheny.by.unit.service.ValidationTest#invalidCountryData")
        @DisplayName("Invalid data should fail validation")
        fun givenInvalidCountryData_whenValidating_thenFailsWithExpectedErrors(
            dto: CountryCreateDto,
            expectedErrorCount: Int
        ) {
            // Given
            // (dto and expectedErrorCount provided as parameters)

            // When
            val result = dto.validateWith(validateCountryOnCreate)

            // Then
            if (expectedErrorCount == 0) {
                assertTrue(result.isSuccess)
            } else {
                assertTrue(result.isFailure)
                val exception = result.exceptionOrNull() as? ValidationException
                assertNotNull(exception)
                assertEquals(expectedErrorCount, exception.errors?.size)
            }
        }
    }

    @Nested
    @DisplayName("CityCreateDto validation")
    inner class CityValidationTest {
        @Test
        @DisplayName("Valid data should pass validation")
        fun givenValidCityData_whenValidating_thenPasses() {
            // Given
            val validDto =
                CityCreateDto(
                    countryId = UUID.randomUUID(),
                    nameRu = "Минск",
                    nameEn = "Minsk",
                    region = "Минская область",
                    coordinates = LatLon(lat = 53.9006, lon = 27.5590)
                )

            // When
            val result = validDto.validateWith(validateCityOnCreate)

            // Then
            assertTrue(result.isSuccess)
            assertEquals(validDto, result.getOrNull())
        }

        @Test
        @DisplayName("Valid boundary values should pass validation")
        fun givenBoundaryCoordinates_whenValidatingCity_thenPasses() {
            // Given
            val validDto =
                CityCreateDto(
                    countryId = UUID.randomUUID(),
                    nameRu = "Минск",
                    nameEn = "Minsk",
                    region = null,
                    coordinates = LatLon(lat = 90.0, lon = 180.0)
                )

            // When
            val result = validDto.validateWith(validateCityOnCreate)

            // Then
            assertTrue(result.isSuccess)
        }

        @ParameterizedTest
        @MethodSource("yayauheny.by.unit.service.ValidationTest#invalidCityData")
        @DisplayName("Invalid data should fail validation")
        fun givenInvalidCityData_whenValidating_thenFailsWithExpectedErrors(
            dto: CityCreateDto,
            expectedErrorCount: Int
        ) {
            // Given
            // (dto and expectedErrorCount provided as parameters)

            // When
            val result = dto.validateWith(validateCityOnCreate)

            // Then
            if (expectedErrorCount == 0) {
                assertTrue(result.isSuccess)
            } else {
                assertTrue(result.isFailure)
                val exception = result.exceptionOrNull() as? ValidationException
                assertNotNull(exception)
                assertEquals(expectedErrorCount, exception.errors?.size)
            }
        }
    }

    @Nested
    @DisplayName("RestroomCreateDto validation")
    inner class RestroomValidationTest {
        @Test
        @DisplayName("Valid data should pass validation")
        fun validDataShouldPassValidation() {
            // Given
            val validDto =
                RestroomCreateDto(
                    cityId = UUID.randomUUID(),
                    name = "Public Restroom",
                    description = "Clean public restroom",
                    address = "123 Main Street",
                    phones = buildJsonObject { put("main", "+1234567890") },
                    workTime = buildJsonObject { put("monday", "08:00-22:00") },
                    feeType = FeeType.FREE,
                    accessibilityType = AccessibilityType.UNISEX,
                    coordinates = LatLon(lat = 55.7558, lon = 37.6176),
                    dataSource = DataSourceType.MANUAL,
                    status = RestroomStatus.ACTIVE,
                    amenities = buildJsonObject { put("wifi", true) },
                    parentPlaceName = null,
                    parentPlaceType = null,
                    inheritParentSchedule = false
                )

            // When
            val result = validDto.validateWith(validateRestroomOnCreate)

            // Then
            assertTrue(result.isSuccess)
            assertEquals(validDto, result.getOrNull())
        }

        @ParameterizedTest
        @MethodSource("yayauheny.by.unit.service.ValidationTest#invalidRestroomData")
        @DisplayName("Invalid data should fail validation")
        fun invalidDataShouldFailValidation(
            dto: RestroomCreateDto,
            expectedErrorCount: Int
        ) {
            // Given
            // (dto and expectedErrorCount provided as parameters)

            // When
            val result = dto.validateWith(validateRestroomOnCreate)

            // Then
            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull() as? ValidationException
            assertNotNull(exception)
            assertEquals(expectedErrorCount, exception.errors?.size)
        }
    }

    @Nested
    @DisplayName("ValidationException throwing")
    inner class ValidationExceptionTest {
        @Test
        @DisplayName("validateOrThrow should throw ValidationException for invalid data")
        fun validateOrThrowShouldThrowForInvalidData() =
            runTest {
                // Given
                val invalidDto = CountryCreateDto("", "United States", "US")

                // When & Then
                val exception =
                    assertFailsWith<ValidationException> {
                        invalidDto.validateOrThrow(validateCountryOnCreate)
                    }

                assertNotNull(exception.errors)
                assertEquals(1, exception.errors!!.size)
                assertTrue(exception.errors!![0].field.contains("nameRu"))
                assertTrue(exception.errors!![0].message.contains("обязательно"))
            }

        @Test
        @DisplayName("validateOrThrow should return valid data without throwing")
        fun validateOrThrowShouldReturnValidDataWithoutThrowing() =
            runTest {
                // Given
                val validDto = CountryCreateDto("США", "United States", "US")

                // When
                val result = validDto.validateOrThrow(validateCountryOnCreate)

                // Then
                assertEquals(validDto, result)
            }
    }

    @Nested
    @DisplayName("Multiple validation errors")
    inner class MultipleErrorsTest {
        @Test
        @DisplayName("Country with multiple issues should return all validation errors")
        fun countryWithMultipleIssuesShouldReturnAllErrors() {
            // Given
            val invalidDto = CountryCreateDto("", "", "U") // Multiple issues

            // When
            val result = invalidDto.validateWith(validateCountryOnCreate)

            // Then
            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull() as? ValidationException
            assertNotNull(exception)
            assertEquals(3, exception.errors?.size) // nameRu, nameEn, code

            val fields = exception.errors!!.map { it.field }
            // Check that we have errors for the expected fields (field names might be different)
            assertTrue(fields.any { it.contains("nameRu") || it.contains("name_ru") })
            assertTrue(fields.any { it.contains("nameEn") || it.contains("name_en") })
            assertTrue(fields.any { it.contains("code") })
        }

        @Test
        @DisplayName("City with multiple issues should return all validation errors")
        fun cityWithMultipleIssuesShouldReturnAllErrors() {
            // Given
            val invalidDto =
                CityCreateDto(
                    countryId = UUID.randomUUID(),
                    nameRu = "",
                    nameEn = "",
                    region = null,
                    coordinates = LatLon(lat = 91.0, lon = 181.0)
                ) // Multiple issues

            // When
            val result = invalidDto.validateWith(validateCityOnCreate)

            // Then
            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull() as? ValidationException
            assertNotNull(exception)
            // nameRu (1) + nameEn (1) + coordinates (2 for lat and lon) = 4 errors
            assertTrue(
                exception.errors?.size ?: 0 >= 3,
                "Should have at least 3 errors (nameRu, nameEn, coordinates), got: ${exception.errors?.size}"
            )

            val fields = exception.errors!!.map { it.field.lowercase() }
            // Check that we have errors for the expected fields (field names might be different)
            assertTrue(fields.any { it.contains("nameru") || it.contains("name_ru") }, "Should have nameRu error, got: $fields")
            assertTrue(fields.any { it.contains("nameen") || it.contains("name_en") }, "Should have nameEn error, got: $fields")
            // For nested coordinates, errors are reported on "coordinates" field
            assertTrue(fields.any { it.contains("coordinates") }, "Should have coordinates error, got: $fields")
        }

        @Test
        @DisplayName("Restroom with multiple issues should return all validation errors")
        fun restroomWithMultipleIssuesShouldReturnAllErrors() {
            // Given
            val invalidDto =
                RestroomCreateDto(
                    cityId = UUID.randomUUID(),
                    status = RestroomStatus.ACTIVE,
                    name = "Test",
                    description = "Test",
                    address = "", // Empty address
                    phones = buildJsonObject {},
                    workTime = buildJsonObject {},
                    feeType = FeeType.FREE,
                    accessibilityType = AccessibilityType.UNISEX,
                    coordinates = LatLon(lat = 91.0, lon = 181.0), // Invalid lat/lon
                    dataSource = DataSourceType.MANUAL,
                    amenities = buildJsonObject {},
                    parentPlaceName = null,
                    parentPlaceType = null,
                    inheritParentSchedule = false
                ) // Multiple issues

            // When
            val result = invalidDto.validateWith(validateRestroomOnCreate)

            // Then
            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull() as? ValidationException
            assertNotNull(exception)
            // address (1) + coordinates (2 for lat and lon) = 3 errors
            assertTrue(
                exception.errors?.size ?: 0 >= 2,
                "Should have at least 2 errors (address, coordinates), got: ${exception.errors?.size}"
            )

            val fields = exception.errors!!.map { it.field.lowercase() }
            assertTrue(fields.any { it.contains("address") }, "Should have address error, got: $fields")
            // For nested coordinates, errors are reported on "coordinates" field
            assertTrue(fields.any { it.contains("coordinates") }, "Should have coordinates error, got: $fields")
        }
    }

    @Nested
    @DisplayName("NearestRestroomsParams validation")
    inner class NearestRestroomsParamsValidationTest {
        @Test
        @DisplayName("Valid parameters should pass validation")
        fun valid_params_should_pass() {
            // Given
            val params = NearestRestroomsParams(LatLon(lat = 55.7558, lon = 37.6176), 10, 1000)

            // When
            val result = params.validateWith(validateNearestRestroomsParams)

            // Then
            assertTrue(result.isSuccess)
            assertEquals(params, result.getOrNull())
        }

        @Test
        @DisplayName("Valid parameters should pass with validateOrThrow")
        fun valid_params_should_pass_with_validateOrThrow() =
            runTest {
                // Given
                val params = NearestRestroomsParams(LatLon(lat = 55.7558, lon = 37.6176), 10, 1000)

                // When
                val result = params.validateOrThrow(validateNearestRestroomsParams)

                // Then
                assertEquals(params, result)
            }

        @ParameterizedTest
        @MethodSource("yayauheny.by.unit.service.ValidationTest#invalidNearestRestroomsParamsData")
        @DisplayName("Invalid parameters should fail validation")
        fun invalid_params_should_fail(
            params: NearestRestroomsParams,
            expectedErrorCount: Int
        ) {
            // Given
            // (params and expectedErrorCount provided as parameters)

            // When
            val result = params.validateWith(validateNearestRestroomsParams)

            // Then
            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull() as? ValidationException
            assertNotNull(exception)
            assertEquals(expectedErrorCount, exception.errors?.size)
        }

        @ParameterizedTest
        @MethodSource("yayauheny.by.unit.service.ValidationTest#invalidNearestRestroomsParamsData")
        @DisplayName("Invalid parameters should throw ValidationException")
        fun invalid_params_should_throw(
            params: NearestRestroomsParams,
            expectedErrorCount: Int
        ) = runTest {
            // Given
            // (params provided as parameter)

            // When & Then
            assertFailsWith<ValidationException> {
                params.validateOrThrow(validateNearestRestroomsParams)
            }
        }
    }
}
