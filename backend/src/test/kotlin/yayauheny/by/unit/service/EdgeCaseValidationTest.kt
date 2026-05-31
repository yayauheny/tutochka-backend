package yayauheny.by.unit.service

import jakarta.validation.Validation
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import yayauheny.by.helpers.TestDataHelpers
import yayauheny.by.model.city.CityCreateDto
import yayauheny.by.model.dto.Coordinates

@DisplayName("Validation edge cases")
class EdgeCaseValidationTest {
    private val validator = Validation.buildDefaultValidatorFactory().validator

    private fun pathsOf(value: Any): Set<String> =
        validator.validate(value).map { it.propertyPath.toString() }.toSet()

    @Nested
    @DisplayName("RestroomCreateDto lengths")
    inner class RestroomLengthBoundaryTests {
        @Test
        fun maximum_length_fields_should_pass_validation() {
            val dto =
                TestDataHelpers.createRestroomCreateDto(
                    cityId = UUID.randomUUID(),
                    name = "A".repeat(150),
                    address = "B".repeat(250),
                    accessNote = "C".repeat(500),
                    directionGuide = "D".repeat(500)
                )

            assertThat(validator.validate(dto)).isEmpty()
        }

        @Test
        fun values_above_maximum_length_should_fail_validation() {
            val dto =
                TestDataHelpers.createRestroomCreateDto(
                    cityId = UUID.randomUUID(),
                    name = "A".repeat(151),
                    address = "B".repeat(251),
                    accessNote = "C".repeat(501),
                    directionGuide = "D".repeat(501)
                )

            val paths = pathsOf(dto)

            assertThat(paths).anyMatch { it.contains("name") }
            assertThat(paths).anyMatch { it.contains("address") }
            assertThat(paths).anyMatch { it.contains("accessNote") }
            assertThat(paths).anyMatch { it.contains("directionGuide") }
        }
    }

    @Nested
    @DisplayName("CityCreateDto optional fields")
    inner class CityOptionalFieldTests {
        @Test
        fun null_optional_fields_should_pass_validation() {
            val dto =
                CityCreateDto(
                    countryId = UUID.randomUUID(),
                    nameRu = "Минск",
                    nameEn = "Minsk",
                    region = null,
                    coordinates = Coordinates(lat = 53.9, lon = 27.5)
                )

            assertThat(validator.validate(dto)).isEmpty()
        }

        @Test
        fun blank_optional_region_should_fail_validation() {
            val dto =
                CityCreateDto(
                    countryId = UUID.randomUUID(),
                    nameRu = "Минск",
                    nameEn = "Minsk",
                    region = "",
                    coordinates = Coordinates(lat = 53.9, lon = 27.5)
                )

            val paths = pathsOf(dto)

            assertThat(paths).anyMatch { it.contains("region") }
        }
    }
}
