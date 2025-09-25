package unit.service

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
import service.validation.Validated
import service.validation.validateOrThrow
import service.validation.validateWith
import yayauheny.by.common.errors.ValidationException
import yayauheny.by.model.CityCreateDto
import yayauheny.by.model.CountryCreateDto
import yayauheny.by.model.RestroomCreateDto
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.DataSourceType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.service.validation.NearestRestroomsParams
import yayauheny.by.service.validation.cityCreateValidator
import yayauheny.by.service.validation.countryCreateValidator
import yayauheny.by.service.validation.nearestRestroomsParamsValidator
import yayauheny.by.service.validation.restroomCreateValidator

class ValidationTest {
    @Nested
    @DisplayName("CountryCreateDto validation")
    inner class CountryValidationTest {
        @Test
        @DisplayName("Valid data should pass validation")
        fun validDataShouldPassValidation() {
            val validDto =
                CountryCreateDto(
                    nameRu = "Соединенные Штаты",
                    nameEn = "United States",
                    code = "US"
                )

            val result = validDto.validateWith(countryCreateValidator)
            assertTrue(result is Validated.Ok)
            assertEquals(validDto, result.value)
        }

        @ParameterizedTest
        @MethodSource("unit.service.ValidationTest#invalidCountryData")
        @DisplayName("Invalid data should fail validation")
        fun invalidDataShouldFailValidation(
            dto: CountryCreateDto,
            expectedErrorCount: Int
        ) {
            val result = dto.validateWith(countryCreateValidator)
            assertTrue(result is Validated.Fail)
            assertEquals(expectedErrorCount, result.errors.size)
        }
    }

    @Nested
    @DisplayName("CityCreateDto validation")
    inner class CityValidationTest {
        @Test
        @DisplayName("Valid data should pass validation")
        fun validDataShouldPassValidation() {
            val validDto =
                CityCreateDto(
                    countryId = UUID.randomUUID(),
                    nameRu = "Минск",
                    nameEn = "Minsk",
                    region = "Минская область",
                    lat = 53.9006,
                    lon = 27.5590
                )

            val result = validDto.validateWith(cityCreateValidator)
            assertTrue(result is Validated.Ok)
            assertEquals(validDto, result.value)
        }

        @Test
        @DisplayName("Valid boundary values should pass validation")
        fun validBoundaryValuesShouldPassValidation() {
            val validDto =
                CityCreateDto(
                    countryId = UUID.randomUUID(),
                    nameRu = "Минск",
                    nameEn = "Minsk",
                    region = null,
                    lat = 90.0, // Maximum valid latitude
                    lon = 180.0 // Maximum valid longitude
                )

            val result = validDto.validateWith(cityCreateValidator)
            assertTrue(result is Validated.Ok)
        }

        @ParameterizedTest
        @MethodSource("unit.service.ValidationTest#invalidCityData")
        @DisplayName("Invalid data should fail validation")
        fun invalidDataShouldFailValidation(
            dto: CityCreateDto,
            expectedErrorCount: Int
        ) {
            val result = dto.validateWith(cityCreateValidator)
            assertTrue(result is Validated.Fail)
            assertEquals(expectedErrorCount, result.errors.size)
        }
    }

    @Nested
    @DisplayName("RestroomCreateDto validation")
    inner class RestroomValidationTest {
        @Test
        @DisplayName("Valid data should pass validation")
        fun validDataShouldPassValidation() {
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
                    lat = 55.7558,
                    lon = 37.6176,
                    dataSource = DataSourceType.MANUAL,
                    amenities = buildJsonObject { put("wifi", true) },
                    parentPlaceName = null,
                    parentPlaceType = null,
                    inheritParentSchedule = false
                )

            val result = validDto.validateWith(restroomCreateValidator)
            assertTrue(result is Validated.Ok)
            assertEquals(validDto, result.value)
        }

        @ParameterizedTest
        @MethodSource("unit.service.ValidationTest#invalidRestroomData")
        @DisplayName("Invalid data should fail validation")
        fun invalidDataShouldFailValidation(
            dto: RestroomCreateDto,
            expectedErrorCount: Int
        ) {
            val result = dto.validateWith(restroomCreateValidator)
            assertTrue(result is Validated.Fail)
            assertEquals(expectedErrorCount, result.errors.size)
        }
    }

    @Nested
    @DisplayName("ValidationException throwing")
    inner class ValidationExceptionTest {
        @Test
        @DisplayName("validateOrThrow should throw ValidationException for invalid data")
        fun validateOrThrowShouldThrowForInvalidData() =
            runTest {
                val invalidDto = CountryCreateDto("", "United States", "US")

                val exception =
                    assertFailsWith<ValidationException> {
                        invalidDto.validateOrThrow(countryCreateValidator)
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
                val validDto = CountryCreateDto("США", "United States", "US")

                val result = validDto.validateOrThrow(countryCreateValidator)
                assertEquals(validDto, result)
            }
    }

    @Nested
    @DisplayName("Multiple validation errors")
    inner class MultipleErrorsTest {
        @Test
        @DisplayName("Country with multiple issues should return all validation errors")
        fun countryWithMultipleIssuesShouldReturnAllErrors() {
            val invalidDto = CountryCreateDto("", "", "U") // Multiple issues

            val result = invalidDto.validateWith(countryCreateValidator)
            assertTrue(result is Validated.Fail)
            assertEquals(3, result.errors.size) // nameRu, nameEn, code

            val fields = result.errors.map { it.field }
            // Check that we have errors for the expected fields (field names might be different)
            assertTrue(fields.any { it.contains("nameRu") || it.contains("name_ru") })
            assertTrue(fields.any { it.contains("nameEn") || it.contains("name_en") })
            assertTrue(fields.any { it.contains("code") })
        }

        @Test
        @DisplayName("City with multiple issues should return all validation errors")
        fun cityWithMultipleIssuesShouldReturnAllErrors() {
            val invalidDto =
                CityCreateDto(
                    UUID.randomUUID(),
                    "",
                    "",
                    null,
                    91.0,
                    181.0
                ) // Multiple issues

            val result = invalidDto.validateWith(cityCreateValidator)
            assertTrue(result is Validated.Fail)
            assertEquals(4, result.errors.size) // nameRu, nameEn, lat, lon

            val fields = result.errors.map { it.field }
            // Check that we have errors for the expected fields (field names might be different)
            assertTrue(fields.any { it.contains("nameRu") || it.contains("name_ru") })
            assertTrue(fields.any { it.contains("nameEn") || it.contains("name_en") })
            assertTrue(fields.any { it.contains("lat") })
            assertTrue(fields.any { it.contains("lon") })
        }

        @Test
        @DisplayName("Restroom with multiple issues should return all validation errors")
        fun restroomWithMultipleIssuesShouldReturnAllErrors() {
            val invalidDto =
                RestroomCreateDto(
                    UUID.randomUUID(),
                    "Test",
                    "Test",
                    "", // Empty address
                    buildJsonObject {},
                    buildJsonObject {},
                    FeeType.FREE,
                    AccessibilityType.UNISEX,
                    91.0, // Invalid lat
                    181.0, // Invalid lon
                    DataSourceType.MANUAL,
                    buildJsonObject {},
                    null,
                    null,
                    false
                ) // Multiple issues

            val result = invalidDto.validateWith(restroomCreateValidator)
            assertTrue(result is Validated.Fail)
            assertEquals(3, result.errors.size) // address, lat, lon

            val fields = result.errors.map { it.field }
            assertTrue(fields.any { it.contains("address") })
            assertTrue(fields.any { it.contains("lat") })
            assertTrue(fields.any { it.contains("lon") })
        }
    }

    companion object {
        @JvmStatic
        fun invalidCountryData(): Stream<Arguments> =
            Stream.of(
                Arguments.of(CountryCreateDto("", "United States", "US"), 1), // Empty nameRu
                Arguments.of(CountryCreateDto("США", "", "US"), 1), // Empty nameEn
                Arguments.of(CountryCreateDto("США", "United States", ""), 2), // Empty code (minLength + maxLength)
                Arguments.of(CountryCreateDto("США", "United States", "U"), 1), // Too short code
                Arguments.of(CountryCreateDto("США", "United States", "TOOLONGCODE"), 1), // Too long code
                Arguments.of(CountryCreateDto("США", "United States", "US@"), 1), // Invalid characters
                Arguments.of(CountryCreateDto("", "", "U"), 3), // Multiple issues
            )

        @JvmStatic
        fun invalidCityData(): Stream<Arguments> =
            Stream.of(
                Arguments.of(CityCreateDto(UUID.randomUUID(), "", "Minsk", null, 53.9006, 27.5590), 1), // Empty nameRu
                Arguments.of(CityCreateDto(UUID.randomUUID(), "Минск", "", null, 53.9006, 27.5590), 1), // Empty nameEn
                Arguments.of(CityCreateDto(UUID.randomUUID(), "Минск", "Minsk", null, 91.0, 27.5590), 1), // Invalid lat > 90
                Arguments.of(CityCreateDto(UUID.randomUUID(), "Минск", "Minsk", null, -91.0, 27.5590), 1), // Invalid lat < -90
                Arguments.of(CityCreateDto(UUID.randomUUID(), "Минск", "Minsk", null, 53.9006, 181.0), 1), // Invalid lon > 180
                Arguments.of(CityCreateDto(UUID.randomUUID(), "Минск", "Minsk", null, 53.9006, -181.0), 1), // Invalid lon < -180
                Arguments.of(CityCreateDto(UUID.randomUUID(), "", "", null, 91.0, 181.0), 4), // Multiple issues
            )

        @JvmStatic
        fun invalidRestroomData(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    RestroomCreateDto(
                        UUID.randomUUID(),
                        "Test",
                        "Test",
                        "", // Empty address
                        buildJsonObject {},
                        buildJsonObject {},
                        FeeType.FREE,
                        AccessibilityType.UNISEX,
                        55.7558,
                        37.6176,
                        DataSourceType.MANUAL,
                        buildJsonObject {},
                        null,
                        null,
                        false
                    ),
                    1
                ),
                Arguments.of(
                    RestroomCreateDto(
                        UUID.randomUUID(),
                        "Test",
                        "Test",
                        "123 Main St",
                        buildJsonObject {},
                        buildJsonObject {},
                        FeeType.FREE,
                        AccessibilityType.UNISEX,
                        91.0,
                        37.6176,
                        DataSourceType.MANUAL,
                        buildJsonObject {}, // Invalid lat > 90
                        null,
                        null,
                        false
                    ),
                    1
                ),
                Arguments.of(
                    RestroomCreateDto(
                        UUID.randomUUID(),
                        "Test",
                        "Test",
                        "123 Main St",
                        buildJsonObject {},
                        buildJsonObject {},
                        FeeType.FREE,
                        AccessibilityType.UNISEX,
                        -91.0,
                        37.6176,
                        DataSourceType.MANUAL,
                        buildJsonObject {}, // Invalid lat < -90
                        null,
                        null,
                        false
                    ),
                    1
                ),
                Arguments.of(
                    RestroomCreateDto(
                        UUID.randomUUID(),
                        "Test",
                        "Test",
                        "123 Main St",
                        buildJsonObject {},
                        buildJsonObject {},
                        FeeType.FREE,
                        AccessibilityType.UNISEX,
                        55.7558,
                        181.0,
                        DataSourceType.MANUAL,
                        buildJsonObject {}, // Invalid lon > 180
                        null,
                        null,
                        false
                    ),
                    1
                ),
                Arguments.of(
                    RestroomCreateDto(
                        UUID.randomUUID(),
                        "Test",
                        "Test",
                        "123 Main St",
                        buildJsonObject {},
                        buildJsonObject {},
                        FeeType.FREE,
                        AccessibilityType.UNISEX,
                        55.7558,
                        -181.0,
                        DataSourceType.MANUAL,
                        buildJsonObject {}, // Invalid lon < -180
                        null,
                        null,
                        false
                    ),
                    1
                ),
            )

        @JvmStatic
        fun invalidNearestRestroomsParamsData(): Stream<Arguments> =
            Stream.of(
                Arguments.of(NearestRestroomsParams(91.0, 37.6176, 10), 1), // lat > 90
                Arguments.of(NearestRestroomsParams(-91.0, 37.6176, 10), 1), // lat < -90
                Arguments.of(NearestRestroomsParams(55.7558, 181.0, 10), 1), // lon > 180
                Arguments.of(NearestRestroomsParams(55.7558, -181.0, 10), 1), // lon < -180
                Arguments.of(NearestRestroomsParams(55.7558, 37.6176, 0), 1), // limit = 0
                Arguments.of(NearestRestroomsParams(55.7558, 37.6176, -1), 1), // limit < 0
                Arguments.of(NearestRestroomsParams(55.7558, 37.6176, 101), 1), // limit > 100
                Arguments.of(NearestRestroomsParams(91.0, 181.0, 101), 3), // Multiple issues
            )
    }

    @Nested
    @DisplayName("NearestRestroomsParams validation")
    inner class NearestRestroomsParamsValidationTest {
        @Test
        @DisplayName("Valid parameters should pass validation")
        fun valid_params_should_pass() {
            val params = NearestRestroomsParams(55.7558, 37.6176, 10)
            val result = params.validateWith(nearestRestroomsParamsValidator)
            assertTrue(result is Validated.Ok)
            assertEquals(params, result.value)
        }

        @Test
        @DisplayName("Valid parameters should pass with validateOrThrow")
        fun valid_params_should_pass_with_validateOrThrow() =
            runTest {
                val params = NearestRestroomsParams(55.7558, 37.6176, 10)
                val result = params.validateOrThrow(nearestRestroomsParamsValidator)
                assertEquals(params, result)
            }

        @ParameterizedTest
        @MethodSource("unit.service.ValidationTest#invalidNearestRestroomsParamsData")
        @DisplayName("Invalid parameters should fail validation")
        fun invalid_params_should_fail(
            params: NearestRestroomsParams,
            expectedErrorCount: Int
        ) {
            val result = params.validateWith(nearestRestroomsParamsValidator)
            assertTrue(result is Validated.Fail)
            assertEquals(expectedErrorCount, result.errors.size)
        }

        @ParameterizedTest
        @MethodSource("unit.service.ValidationTest#invalidNearestRestroomsParamsData")
        @DisplayName("Invalid parameters should throw ValidationException")
        fun invalid_params_should_throw(
            params: NearestRestroomsParams,
            expectedErrorCount: Int
        ) = runTest {
            assertFailsWith<ValidationException> {
                params.validateOrThrow(nearestRestroomsParamsValidator)
            }
        }
    }
}
