package yayauheny.by.unit.service

import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import yayauheny.by.helpers.TestDataHelpers
import yayauheny.by.model.city.CityCreateDto
import yayauheny.by.model.country.CountryCreateDto
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.DataSourceType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.GenderType
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.model.restroom.RestroomCreateDto

@DisplayName("DTO validation tests")
class ValidationTest {
    private val validator = Validation.buildDefaultValidatorFactory().validator

    private fun <T> violations(value: T): Set<ConstraintViolation<T>> = validator.validate(value)

    private fun pathsOf(value: Any): Set<String> =
        validator.validate(value).map { it.propertyPath.toString() }.toSet()

    @Nested
    @DisplayName("CountryCreateDto")
    inner class CountryValidationTest {
        @Test
        fun valid_country_should_pass_validation() {
            val dto = CountryCreateDto("Соединенные Штаты", "United States", "US")

            val result = violations(dto)

            assertThat(result).isEmpty()
        }

        @Test
        fun blank_country_fields_should_fail_validation() {
            val dto = CountryCreateDto("", "", "")

            val paths = pathsOf(dto)

            assertThat(paths).anyMatch { it.contains("nameRu") }
            assertThat(paths).anyMatch { it.contains("nameEn") }
            assertThat(paths).anyMatch { it.contains("code") }
        }
    }

    @Nested
    @DisplayName("CityCreateDto")
    inner class CityValidationTest {
        @Test
        fun valid_city_should_pass_validation() {
            val dto =
                CityCreateDto(
                    countryId = UUID.randomUUID(),
                    nameRu = "Минск",
                    nameEn = "Minsk",
                    region = "Минская область",
                    coordinates = Coordinates(lat = 53.9006, lon = 27.5590)
                )

            assertThat(violations(dto)).isEmpty()
        }

        @Test
        fun boundary_coordinates_should_pass_validation() {
            val dto =
                CityCreateDto(
                    countryId = UUID.randomUUID(),
                    nameRu = "Минск",
                    nameEn = "Minsk",
                    region = null,
                    coordinates = Coordinates(lat = 90.0, lon = 180.0)
                )

            assertThat(violations(dto)).isEmpty()
        }

        @Test
        fun invalid_city_coordinates_should_fail_validation() {
            val dto =
                CityCreateDto(
                    countryId = UUID.randomUUID(),
                    nameRu = "Минск",
                    nameEn = "Minsk",
                    region = null,
                    coordinates = Coordinates(lat = 91.0, lon = 181.0)
                )

            val paths = pathsOf(dto)

            assertThat(paths).anyMatch { it.contains("coordinates") && it.contains("lat") }
            assertThat(paths).anyMatch { it.contains("coordinates") && it.contains("lon") }
        }

        @Test
        fun blank_city_names_should_fail_validation() {
            val dto =
                CityCreateDto(
                    countryId = UUID.randomUUID(),
                    nameRu = "",
                    nameEn = "",
                    region = null,
                    coordinates = Coordinates(lat = 53.9006, lon = 27.5590)
                )

            val paths = pathsOf(dto)

            assertThat(paths).anyMatch { it.contains("nameRu") }
            assertThat(paths).anyMatch { it.contains("nameEn") }
        }
    }

    @Nested
    @DisplayName("RestroomCreateDto")
    inner class RestroomValidationTest {
        @Test
        fun valid_restroom_should_pass_validation() {
            val dto = TestDataHelpers.createRestroomCreateDto()

            assertThat(violations(dto)).isEmpty()
        }

        @Test
        fun restroom_with_null_address_should_pass_validation() {
            val dto =
                TestDataHelpers.createRestroomCreateDto(
                    address = null,
                    accessNote = null,
                    directionGuide = null
                )

            assertThat(violations(dto)).isEmpty()
        }

        @Test
        fun restroom_with_invalid_coordinates_should_fail_validation() {
            val dto =
                RestroomCreateDto(
                    cityId = UUID.randomUUID(),
                    status = RestroomStatus.ACTIVE,
                    name = "Test",
                    address = "Test Address",
                    phones = emptyMap(),
                    workTime = emptyMap(),
                    feeType = FeeType.FREE,
                    genderType = GenderType.UNISEX,
                    accessibilityType = AccessibilityType.WHEELCHAIR,
                    placeType = PlaceType.OTHER,
                    coordinates = Coordinates(lat = 91.0, lon = 181.0),
                    dataSource = DataSourceType.MANUAL,
                    amenities = emptyMap()
                )

            val paths = pathsOf(dto)

            assertThat(paths).anyMatch { it.contains("coordinates") && it.contains("lat") }
            assertThat(paths).anyMatch { it.contains("coordinates") && it.contains("lon") }
        }
    }
}
