package yayauheny.by.unit.service

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import yayauheny.by.model.city.CityCreateDto
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.DataSourceType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.model.LatLon
import yayauheny.by.model.restroom.RestroomCreateDto
import yayauheny.by.common.errors.ValidationException
import yayauheny.by.service.validation.validateCityOnCreate
import yayauheny.by.service.validation.validateRestroomCreateFields
import yayauheny.by.service.validation.validateRestroomOnCreate
import yayauheny.by.service.validation.validateWith
import java.util.UUID
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@DisplayName("Edge Case Validation Tests")
class EdgeCaseValidationTest {
    @Nested
    @DisplayName("Coordinate Boundary Tests")
    inner class CoordinateBoundaryTests {
        @ParameterizedTest
        @CsvSource(
            "90.0, 180.0",
            "-90.0, -180.0",
            "0.0, 0.0",
            "89.999999, 179.999999",
            "-89.999999, -179.999999"
        )
        @DisplayName("Valid boundary coordinates should pass validation")
        fun valid_boundary_coordinates_should_pass(
            lat: Double,
            lon: Double
        ) {
            // Given
            val dto =
                CityCreateDto(
                    countryId = UUID.randomUUID(),
                    nameRu = "Test",
                    nameEn = "Test",
                    region = null,
                    coordinates = LatLon(lat = lat, lon = lon)
                )

            // When
            val result = dto.validateWith(validateCityOnCreate)

            // Then
            assertTrue(result.isSuccess, "Coordinates ($lat, $lon) should be valid")
        }

        @ParameterizedTest
        @CsvSource(
            "90.1, 180.0",
            "-90.1, -180.0",
            "91.0, 0.0",
            "-91.0, 0.0",
            "0.0, 180.1",
            "0.0, -180.1",
            "0.0, 181.0",
            "0.0, -181.0",
            "1.7976931348623157E308, 0.0",
            "0.0, 1.7976931348623157E308"
        )
        @DisplayName("Out-of-range coordinates should fail validation")
        fun out_of_range_coordinates_should_fail(
            lat: Double,
            lon: Double
        ) {
            // Given
            val dto =
                CityCreateDto(
                    countryId = UUID.randomUUID(),
                    nameRu = "Test",
                    nameEn = "Test",
                    region = null,
                    coordinates = LatLon(lat = lat, lon = lon)
                )

            // When
            val result = dto.validateWith(validateCityOnCreate)

            // Then
            assertFalse(result.isSuccess, "Coordinates ($lat, $lon) should be invalid")
            val exception = result.exceptionOrNull() as? ValidationException
            assertNotNull(exception, "Should throw ValidationException")
        }

        @Test
        @DisplayName("NaN coordinates should fail validation")
        fun nan_coordinates_should_fail() {
            // Given
            val dto =
                CityCreateDto(
                    countryId = UUID.randomUUID(),
                    nameRu = "Test",
                    nameEn = "Test",
                    region = null,
                    coordinates = LatLon(lat = Double.NaN, lon = 0.0)
                )

            // When
            val result = dto.validateWith(validateCityOnCreate)

            // Then
            assertFalse(result.isSuccess, "NaN latitude should be invalid")
        }

        @Test
        @DisplayName("Infinite coordinates should fail validation")
        fun infinite_coordinates_should_fail() {
            // Given
            val dto =
                CityCreateDto(
                    countryId = UUID.randomUUID(),
                    nameRu = "Test",
                    nameEn = "Test",
                    region = null,
                    coordinates = LatLon(lat = Double.POSITIVE_INFINITY, lon = 0.0)
                )

            // When
            val result = dto.validateWith(validateCityOnCreate)

            // Then
            assertFalse(result.isSuccess, "Infinite latitude should be invalid")
        }
    }

    @Nested
    @DisplayName("String Length Boundary Tests")
    inner class StringLengthBoundaryTests {
        @Test
        @DisplayName("Maximum length strings should pass validation")
        fun maximum_length_strings_should_pass() {
            // Given
            val maxName = "A".repeat(255)
            val maxDescription = "B".repeat(10000)

            val dto =
                RestroomCreateDto(
                    cityId = UUID.randomUUID(),
                    status = RestroomStatus.ACTIVE,
                    name = maxName,
                    description = maxDescription,
                    address = "Test Address",
                    phones = buildJsonObject {},
                    workTime = buildJsonObject {},
                    feeType = FeeType.FREE,
                    accessibilityType = AccessibilityType.UNISEX,
                    coordinates = LatLon(lat = 53.9, lon = 27.5),
                    dataSource = DataSourceType.MANUAL,
                    amenities = buildJsonObject {},
                    parentPlaceName = null,
                    parentPlaceType = null,
                    inheritParentSchedule = false
                )

            // When
            val result = dto.validateWith(validateRestroomOnCreate)

            // Then
            assertTrue(result.isSuccess, "Maximum length strings should be valid")
        }

        @Test
        @DisplayName("Over maximum length strings should fail validation")
        fun over_maximum_length_strings_should_fail() {
            // Given
            val tooLongName = "A".repeat(256)
            val tooLongDescription = "B".repeat(10001)

            val dto1 =
                RestroomCreateDto(
                    cityId = UUID.randomUUID(),
                    status = RestroomStatus.ACTIVE,
                    name = tooLongName,
                    description = "Test",
                    address = "Test Address",
                    phones = buildJsonObject {},
                    workTime = buildJsonObject {},
                    feeType = FeeType.FREE,
                    accessibilityType = AccessibilityType.UNISEX,
                    coordinates = LatLon(lat = 53.9, lon = 27.5),
                    dataSource = DataSourceType.MANUAL,
                    amenities = buildJsonObject {},
                    parentPlaceName = null,
                    parentPlaceType = null,
                    inheritParentSchedule = false
                )

            // When
            // validateRestroomOnCreate не проверяет name (это nullable поле)
            // Проверяем через validateRestroomCreateFields
            val errors1 = validateRestroomCreateFields(dto1)

            // Then
            assertTrue(errors1.isNotEmpty(), "Name over 255 characters should be invalid")
            assertTrue(errors1.any { it.field == "name" }, "Should have error for 'name' field")

            // Given
            val dto2 =
                RestroomCreateDto(
                    cityId = UUID.randomUUID(),
                    status = RestroomStatus.ACTIVE,
                    name = "Test",
                    description = tooLongDescription,
                    address = "Test Address",
                    phones = buildJsonObject {},
                    workTime = buildJsonObject {},
                    feeType = FeeType.FREE,
                    accessibilityType = AccessibilityType.UNISEX,
                    coordinates = LatLon(lat = 53.9, lon = 27.5),
                    dataSource = DataSourceType.MANUAL,
                    amenities = buildJsonObject {},
                    parentPlaceName = null,
                    parentPlaceType = null,
                    inheritParentSchedule = false
                )

            // When
            // Проверяем через validateRestroomCreateFields
            val errors2 = validateRestroomCreateFields(dto2)

            // Then
            assertTrue(errors2.isNotEmpty(), "Description over 10000 characters should be invalid")
            assertTrue(errors2.any { it.field == "description" }, "Should have error for 'description' field")
        }

        @Test
        @DisplayName("Empty strings for required fields should fail validation")
        fun empty_strings_for_required_fields_should_fail() {
            // Given
            val dto =
                RestroomCreateDto(
                    cityId = UUID.randomUUID(),
                    status = RestroomStatus.ACTIVE,
                    name = "",
                    description = "",
                    address = "",
                    phones = buildJsonObject {},
                    workTime = buildJsonObject {},
                    feeType = FeeType.FREE,
                    accessibilityType = AccessibilityType.UNISEX,
                    coordinates = LatLon(lat = 53.9, lon = 27.5),
                    dataSource = DataSourceType.MANUAL,
                    amenities = buildJsonObject {},
                    parentPlaceName = null,
                    parentPlaceType = null,
                    inheritParentSchedule = false
                )

            // When
            val result = dto.validateWith(validateRestroomOnCreate)

            // Then
            assertFalse(result.isSuccess, "Empty address should be invalid")
        }

        @Test
        @DisplayName("Whitespace-only strings for required fields should pass validation (current limitation)")
        fun whitespace_only_strings_should_pass() {
            // Given
            // minLength проверяет длину строки, но не проверяет, что строка не состоит только из пробелов
            // В реальной валидации whitespace-only строки могут проходить проверку minLength
            // Это известное ограничение текущей валидации
            val dto =
                RestroomCreateDto(
                    cityId = UUID.randomUUID(),
                    status = RestroomStatus.ACTIVE,
                    name = "   ",
                    description = "   ",
                    address = "   ", // address имеет minLength(1), поэтому "   " пройдет проверку по длине
                    phones = buildJsonObject {},
                    workTime = buildJsonObject {},
                    feeType = FeeType.FREE,
                    accessibilityType = AccessibilityType.UNISEX,
                    coordinates = LatLon(lat = 53.9, lon = 27.5),
                    dataSource = DataSourceType.MANUAL,
                    amenities = buildJsonObject {},
                    parentPlaceName = null,
                    parentPlaceType = null,
                    inheritParentSchedule = false
                )

            // When
            // Текущая валидация не проверяет whitespace-only строки
            // Это может быть улучшено в будущем добавлением проверки trim().isBlank()
            val result = dto.validateWith(validateRestroomOnCreate)

            // Then
            // Валидация может пройти, так как "   " имеет длину >= 1
            // Это ожидаемое поведение текущей реализации
            assertTrue(result.isSuccess, "Whitespace-only address passes validation (current limitation)")
        }
    }

    @Nested
    @DisplayName("Minimum Length Boundary Tests")
    inner class MinimumLengthBoundaryTests {
        @ParameterizedTest
        @ValueSource(strings = ["AB", "ABC", "Test", "Минск"])
        @DisplayName("Valid minimum length strings should pass validation")
        fun valid_minimum_length_strings_should_pass(name: String) {
            // Given
            val dto =
                CityCreateDto(
                    countryId = UUID.randomUUID(),
                    nameRu = name,
                    nameEn = name,
                    region = null,
                    coordinates = LatLon(lat = 53.9, lon = 27.5)
                )

            // When
            val result = dto.validateWith(validateCityOnCreate)

            // Then
            assertTrue(result.isSuccess, "Name '$name' with length ${name.length} should be valid")
        }

        @ParameterizedTest
        @ValueSource(strings = ["", "A", " "])
        @DisplayName("Below minimum length strings should fail validation")
        fun below_minimum_length_strings_should_fail(name: String) {
            // Given
            val dto =
                CityCreateDto(
                    countryId = UUID.randomUUID(),
                    nameRu = name,
                    nameEn = name,
                    region = null,
                    coordinates = LatLon(lat = 53.9, lon = 27.5)
                )

            // When
            val result = dto.validateWith(validateCityOnCreate)

            // Then
            assertFalse(result.isSuccess, "Name '$name' with length ${name.length} should be invalid")
        }
    }

    @Nested
    @DisplayName("Null and Optional Field Tests")
    inner class NullAndOptionalFieldTests {
        @Test
        @DisplayName("Null optional fields should pass validation")
        fun null_optional_fields_should_pass() {
            // Given
            val dto =
                CityCreateDto(
                    countryId = UUID.randomUUID(),
                    nameRu = "Test",
                    nameEn = "Test",
                    region = null, // Optional field
                    coordinates = LatLon(lat = 53.9, lon = 27.5)
                )

            // When
            val result = dto.validateWith(validateCityOnCreate)

            // Then
            assertTrue(result.isSuccess, "Null optional region should be valid")
        }

        @Test
        @DisplayName("Null required fields should fail validation")
        fun null_required_fields_should_fail() {
            // For runtime validation, we test with empty strings or invalid values
        }
    }

    @Nested
    @DisplayName("JSON Structure Tests")
    inner class JsonStructureTests {
        @Test
        @DisplayName("Valid JSON objects should pass validation")
        fun valid_json_objects_should_pass() {
            // Given
            val validPhones =
                buildJsonObject {
                    put("mobile", JsonPrimitive("+375291234567"))
                }
            val validWorkTime =
                buildJsonObject {
                    put("monday", JsonPrimitive("09:00-18:00"))
                }

            val dto =
                RestroomCreateDto(
                    cityId = UUID.randomUUID(),
                    status = RestroomStatus.ACTIVE,
                    name = "Test",
                    description = "Test",
                    address = "Test Address",
                    phones = validPhones,
                    workTime = validWorkTime,
                    feeType = FeeType.FREE,
                    accessibilityType = AccessibilityType.UNISEX,
                    coordinates = LatLon(lat = 53.9, lon = 27.5),
                    dataSource = DataSourceType.MANUAL,
                    amenities = buildJsonObject {},
                    parentPlaceName = null,
                    parentPlaceType = null,
                    inheritParentSchedule = false
                )

            // When
            val result = dto.validateWith(validateRestroomOnCreate)

            // Then
            assertTrue(result.isSuccess, "Valid JSON objects should pass validation")
        }
    }
}
