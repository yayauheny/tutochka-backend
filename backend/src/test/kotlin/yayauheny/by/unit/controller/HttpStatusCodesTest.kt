package yayauheny.by.unit.controller

import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import yayauheny.by.common.errors.BadRequestException
import yayauheny.by.common.errors.ConflictException
import yayauheny.by.common.errors.FieldError
import yayauheny.by.common.errors.ServiceUnavailableException
import yayauheny.by.common.errors.ValidationException
import yayauheny.by.controller.CityController
import yayauheny.by.helpers.TestDataHelpers
import yayauheny.by.helpers.any
import yayauheny.by.helpers.assertHasValidationErrors
import yayauheny.by.helpers.createCityJson
import yayauheny.by.helpers.createCityUpdateJson
import yayauheny.by.helpers.parseErrorResponse
import yayauheny.by.helpers.testDelete
import yayauheny.by.helpers.testGet
import yayauheny.by.helpers.testPost
import yayauheny.by.helpers.testPut

@WebMvcTest(CityController::class)
@DisplayName("HTTP Status Codes Tests")
class HttpStatusCodesTest : RoutingTestBase() {
    @Nested
    @DisplayName("City Controller Status Codes")
    inner class CityControllerStatusCodes {
        @Test
        @DisplayName("GIVEN valid request WHEN GET city THEN return 200 OK")
        fun get_city_returns_200() {
            given(cityService.getCityById(any())).willReturn(TestDataHelpers.createCityResponseDto())

            withRoutingApp {
                val response = testGet("/api/v1/cities/${UUID.randomUUID()}")

                assertEquals(HttpStatus.OK, response.status)
            }
        }

        @Test
        @DisplayName("GIVEN non-existent city WHEN GET city THEN return 404 Not Found")
        fun get_non_existent_city_returns_404() {
            given(cityService.getCityById(any())).willReturn(null)

            withRoutingApp {
                val response = testGet("/api/v1/cities/${UUID.randomUUID()}")

                assertEquals(HttpStatus.NOT_FOUND, response.status)
            }
        }

        @Test
        @DisplayName("GIVEN invalid UUID WHEN GET city THEN return 400 Bad Request")
        fun get_city_with_invalid_uuid_returns_400() {
            withRoutingApp {
                val response = testGet("/api/v1/cities/invalid-uuid")

                assertEquals(HttpStatus.BAD_REQUEST, response.status)
            }
        }

        @Test
        @DisplayName("GIVEN valid city data WHEN POST city THEN return 201 Created")
        fun post_city_returns_201() {
            given(cityService.createCity(any())).willReturn(TestDataHelpers.createCityResponseDto())

            withRoutingApp {
                val createDto = TestDataHelpers.createCityCreateDto()
                val response = testPost("/api/v1/cities", createCityJson(createDto))

                assertEquals(HttpStatus.CREATED, response.status)
            }
        }

        @Test
        @DisplayName("GIVEN invalid city data WHEN POST city THEN return 400 Bad Request")
        fun post_invalid_city_returns_400() {
            doAnswer { throw ValidationException(listOf(FieldError("nameRu", "Название обязательно"))) }
                .`when`(cityService)
                .createCity(any())

            withRoutingApp {
                val response = testPost("/api/v1/cities", """{"invalid": "data"}""")

                assertEquals(HttpStatus.BAD_REQUEST, response.status)
            }
        }

        @Test
        @DisplayName("GIVEN duplicate city WHEN POST city THEN return 409 Conflict")
        fun post_duplicate_city_returns_409() {
            doAnswer { throw ConflictException("City already exists") }
                .`when`(cityService)
                .createCity(any())

            withRoutingApp {
                val createDto = TestDataHelpers.createCityCreateDto()
                val response = testPost("/api/v1/cities", createCityJson(createDto))

                assertEquals(HttpStatus.CONFLICT, response.status)
            }
        }

        @Test
        @DisplayName("GIVEN non-existent country WHEN POST city THEN return 400 Bad Request")
        fun post_city_with_non_existent_country_returns_400() {
            doAnswer { throw BadRequestException("Country not found") }
                .`when`(cityService)
                .createCity(any())

            withRoutingApp {
                val createDto = TestDataHelpers.createCityCreateDto()
                val response = testPost("/api/v1/cities", createCityJson(createDto))

                assertEquals(HttpStatus.BAD_REQUEST, response.status)
            }
        }

        @Test
        @DisplayName("GIVEN valid update data WHEN PUT city THEN return 200 OK")
        fun put_city_returns_200() {
            val id = UUID.randomUUID()
            given(cityService.updateCity(any(), any())).willReturn(TestDataHelpers.createCityResponseDto())

            withRoutingApp {
                val updateDto = TestDataHelpers.createCityUpdateDto()
                val response = testPut("/api/v1/cities/$id", createCityUpdateJson(updateDto))

                assertEquals(HttpStatus.OK, response.status)
            }
        }

        @Test
        @DisplayName("GIVEN existing city WHEN DELETE city THEN return 200 OK")
        fun delete_existing_city_returns_200() {
            val id = UUID.randomUUID()
            given(cityService.deleteCity(id)).willReturn(true)

            withRoutingApp {
                val response = testDelete("/api/v1/cities/$id")

                assertEquals(HttpStatus.OK, response.status)
            }
        }

        @Test
        @DisplayName("GIVEN non-existent city WHEN DELETE city THEN return 404 Not Found")
        fun delete_non_existent_city_returns_404() {
            val id = UUID.randomUUID()
            given(cityService.deleteCity(id)).willReturn(false)

            withRoutingApp {
                val response = testDelete("/api/v1/cities/$id")

                assertEquals(HttpStatus.NOT_FOUND, response.status)
            }
        }
    }

    @Nested
    @DisplayName("Error Response Structure Tests")
    inner class ErrorResponseStructureTests {
        @Test
        @DisplayName("GIVEN validation error WHEN request fails THEN response contains errors array")
        fun validation_error_contains_errors_array() {
            doAnswer {
                throw ValidationException(
                    listOf(
                        FieldError("nameRu", "Название обязательно"),
                        FieldError("nameEn", "Name is required")
                    )
                )
            }
                .`when`(cityService)
                .createCity(any())

            val invalidDto = TestDataHelpers.createCityCreateDto().copy(nameRu = "")
            withRoutingApp {
                val response = testPost("/api/v1/cities", createCityJson(invalidDto))

                assertEquals(HttpStatus.BAD_REQUEST, response.status)
                response.parseErrorResponse().assertHasValidationErrors()
            }
        }

        @Test
        @DisplayName("GIVEN service unavailable WHEN request fails THEN return 503")
        fun service_unavailable_returns_503() {
            doAnswer { throw ServiceUnavailableException("Service temporarily unavailable", retryAfter = 60) }
                .`when`(cityService)
                .getAllCities(any(), any())

            withRoutingApp {
                val response = testGet("/api/v1/cities")

                assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.status)
                assertEquals("60", response.header("Retry-After"))
            }
        }
    }
}
