package yayauheny.by.unit.service

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import yayauheny.by.model.city.CityCreateDto
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.service.validation.validateCityOnCreate
import yayauheny.by.service.validation.validateRestroomCreateFields
import yayauheny.by.service.validation.validateRestroomOnCreate
import yayauheny.by.service.validation.validateWith
import yayauheny.by.helpers.TestDataHelpers
import java.util.UUID
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DisplayName("Edge Case Validation Tests")
class EdgeCaseValidationTest {
    @Nested
    @DisplayName("String Length Boundary Tests")
    inner class StringLengthBoundaryTests {
        @Test
        @DisplayName("Maximum length strings should pass validation")
        fun maximum_length_strings_should_pass() {
            val maxName = "A".repeat(255)
            val maxAccessNote = "B".repeat(10000)

            val dto =
                TestDataHelpers.createRestroomCreateDto(
                    cityId = UUID.randomUUID(),
                    name = maxName,
                    address = "Test Address",
                    phones = buildJsonObject {},
                    workTime = buildJsonObject {},
                    lat = 53.9,
                    lon = 27.5,
                    amenities = buildJsonObject {},
                    accessNote = maxAccessNote
                )

            val result = dto.validateWith(validateRestroomOnCreate)

            assertTrue(result.isSuccess, "Maximum length strings should be valid")
        }

        @Test
        @DisplayName("Over maximum length strings should fail validation")
        fun over_maximum_length_strings_should_fail() {
            val tooLongName = "A".repeat(256)
            val tooLongDescription = "B".repeat(10001)

            val dto1 =
                TestDataHelpers.createRestroomCreateDto(
                    cityId = UUID.randomUUID(),
                    name = tooLongName,
                    address = "Test Address",
                    phones = buildJsonObject {},
                    workTime = buildJsonObject {},
                    lat = 53.9,
                    lon = 27.5,
                    amenities = buildJsonObject {},
                    accessNote = "Test"
                )

            val errors1 = validateRestroomCreateFields(dto1)

            assertTrue(errors1.isNotEmpty(), "Name over 255 characters should be invalid")
            assertTrue(errors1.any { it.field == "name" }, "Should have error for 'name' field")

            val dto2 =
                TestDataHelpers.createRestroomCreateDto(
                    cityId = UUID.randomUUID(),
                    name = "Test",
                    address = "Test Address",
                    phones = buildJsonObject {},
                    workTime = buildJsonObject {},
                    lat = 53.9,
                    lon = 27.5,
                    amenities = buildJsonObject {},
                    accessNote = tooLongDescription
                )

            val errors2 = validateRestroomCreateFields(dto2)

            assertTrue(errors2.isNotEmpty(), "AccessNote over 10000 characters should be invalid")
            assertTrue(errors2.any { it.field == "accessNote" }, "Should have error for 'accessNote' field")
        }

        @Test
        @DisplayName("Restroom with null address and valid coordinates should pass validation")
        fun restroom_with_null_address_should_pass() {
            val dto =
                TestDataHelpers.createRestroomCreateDto(
                    cityId = UUID.randomUUID(),
                    name = "Test",
                    address = null,
                    phones = buildJsonObject {},
                    workTime = buildJsonObject {},
                    lat = 53.9,
                    lon = 27.5,
                    amenities = buildJsonObject {},
                    accessNote = null,
                    directionGuide = null
                )

            val result = dto.validateWith(validateRestroomOnCreate)

            assertTrue(result.isSuccess, "Null address is optional and should pass")
        }
    }

    @Nested
    @DisplayName("Minimum Length Boundary Tests")
    inner class MinimumLengthBoundaryTests {
        @ParameterizedTest
        @ValueSource(strings = ["AB", "ABC", "Test", "Минск"])
        @DisplayName("Valid minimum length strings should pass validation")
        fun valid_minimum_length_strings_should_pass(name: String) {
            val dto =
                CityCreateDto(
                    countryId = UUID.randomUUID(),
                    nameRu = name,
                    nameEn = name,
                    region = null,
                    coordinates = Coordinates(lat = 53.9, lon = 27.5)
                )

            val result = dto.validateWith(validateCityOnCreate)

            assertTrue(result.isSuccess, "Name '$name' with length ${name.length} should be valid")
        }

        @ParameterizedTest
        @ValueSource(strings = ["", "A", " "])
        @DisplayName("Below minimum length strings should fail validation")
        fun below_minimum_length_strings_should_fail(name: String) {
            val dto =
                CityCreateDto(
                    countryId = UUID.randomUUID(),
                    nameRu = name,
                    nameEn = name,
                    region = null,
                    coordinates = Coordinates(lat = 53.9, lon = 27.5)
                )

            val result = dto.validateWith(validateCityOnCreate)

            assertFalse(result.isSuccess, "Name '$name' with length ${name.length} should be invalid")
        }
    }

    @Nested
    @DisplayName("Null and Optional Field Tests")
    inner class NullAndOptionalFieldTests {
        @Test
        @DisplayName("Null optional fields should pass validation")
        fun null_optional_fields_should_pass() {
            val dto =
                CityCreateDto(
                    countryId = UUID.randomUUID(),
                    nameRu = "Test",
                    nameEn = "Test",
                    region = null, // Optional field
                    coordinates = Coordinates(lat = 53.9, lon = 27.5)
                )

            val result = dto.validateWith(validateCityOnCreate)

            assertTrue(result.isSuccess, "Null optional region should be valid")
        }
    }

    @Nested
    @DisplayName("JSON Structure Tests")
    inner class JsonStructureTests {
        @Test
        @DisplayName("Valid JSON objects should pass validation")
        fun valid_json_objects_should_pass() {
            val validPhones =
                buildJsonObject {
                    put("mobile", JsonPrimitive("+375291234567"))
                }
            val validWorkTime =
                buildJsonObject {
                    put("monday", JsonPrimitive("09:00-18:00"))
                }

            val dto =
                TestDataHelpers.createRestroomCreateDto(
                    cityId = UUID.randomUUID(),
                    name = "Test",
                    address = "Test Address",
                    phones = validPhones,
                    workTime = validWorkTime,
                    lat = 53.9,
                    lon = 27.5,
                    amenities = buildJsonObject {},
                    accessNote = "Test"
                )

            val result = dto.validateWith(validateRestroomOnCreate)

            assertTrue(result.isSuccess, "Valid JSON objects should pass validation")
        }
    }
}
