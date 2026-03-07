package yayauheny.by.unit.controller

import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import java.util.UUID
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import yayauheny.by.common.errors.BadRequestException
import yayauheny.by.common.errors.ConflictException
import yayauheny.by.common.errors.FieldError
import yayauheny.by.common.errors.ServiceUnavailableException
import yayauheny.by.common.errors.ValidationException
import yayauheny.by.helpers.TestDataHelpers
import yayauheny.by.helpers.assertHasValidationErrors
import yayauheny.by.helpers.createCityJson
import yayauheny.by.helpers.createCityUpdateJson
import yayauheny.by.helpers.parseErrorResponse
import yayauheny.by.helpers.testDelete
import yayauheny.by.helpers.testGet
import yayauheny.by.helpers.testPost
import yayauheny.by.helpers.testPut

@DisplayName("HTTP Status Codes Tests")
class HttpStatusCodesTest : RoutingTestBase() {
    @Nested
    @DisplayName("City Controller Status Codes")
    inner class CityControllerStatusCodes {
        @Test
        @DisplayName("GIVEN valid request WHEN GET city THEN return 200 OK")
        fun get_city_returns_200() =
            runTest {
                coEvery { cityService.getCityById(any()) } returns TestDataHelpers.createCityResponseDto()

                withRoutingApp { client ->
                    val response = client.testGet("/api/v1/cities/${UUID.randomUUID()}")

                    assertEquals(HttpStatusCode.OK, response.status)
                }
            }

        @Test
        @DisplayName("GIVEN non-existent city WHEN GET city THEN return 404 Not Found")
        fun get_non_existent_city_returns_404() =
            runTest {
                coEvery { cityService.getCityById(any()) } returns null

                withRoutingApp { client ->
                    val response = client.testGet("/api/v1/cities/${UUID.randomUUID()}")

                    assertEquals(HttpStatusCode.NotFound, response.status)
                }
            }

        @Test
        @DisplayName("GIVEN invalid UUID WHEN GET city THEN return 400 Bad Request")
        fun get_city_with_invalid_uuid_returns_400() =
            runTest {
                withRoutingApp { client ->
                    val response = client.testGet("/api/v1/cities/invalid-uuid")

                    assertEquals(HttpStatusCode.BadRequest, response.status)
                }
            }

        @Test
        @DisplayName("GIVEN valid city data WHEN POST city THEN return 201 Created")
        fun post_city_returns_201() =
            runTest {
                val createDto = TestDataHelpers.createCityCreateDto()
                coEvery { cityService.createCity(any()) } returns TestDataHelpers.createCityResponseDto()

                withRoutingApp { client ->
                    val response = client.testPost("/api/v1/cities", createCityJson(createDto))

                    assertEquals(HttpStatusCode.Created, response.status)
                }
            }

        @Test
        @DisplayName("GIVEN invalid city data WHEN POST city THEN return 400 Bad Request")
        fun post_invalid_city_returns_400() =
            runTest {
                coEvery { cityService.createCity(any()) } throws
                    ValidationException(
                        listOf(FieldError("nameRu", "Название обязательно"))
                    )

                withRoutingApp { client ->
                    val response = client.testPost("/api/v1/cities", """{"invalid": "data"}""")

                    assertEquals(HttpStatusCode.BadRequest, response.status)
                }
            }

        @Test
        @DisplayName("GIVEN duplicate city WHEN POST city THEN return 409 Conflict")
        fun post_duplicate_city_returns_409() =
            runTest {
                coEvery { cityService.createCity(any()) } throws ConflictException("City already exists")

                withRoutingApp { client ->
                    val createDto = TestDataHelpers.createCityCreateDto()
                    val response = client.testPost("/api/v1/cities", createCityJson(createDto))

                    assertEquals(HttpStatusCode.Conflict, response.status)
                }
            }

        @Test
        @DisplayName("GIVEN non-existent country WHEN POST city THEN return 400 Bad Request")
        fun post_city_with_non_existent_country_returns_400() =
            runTest {
                coEvery { cityService.createCity(any()) } throws BadRequestException("Country not found")

                withRoutingApp { client ->
                    val createDto = TestDataHelpers.createCityCreateDto()
                    val response = client.testPost("/api/v1/cities", createCityJson(createDto))

                    assertEquals(HttpStatusCode.BadRequest, response.status)
                }
            }

        @Test
        @DisplayName("GIVEN valid update data WHEN PUT city THEN return 200 OK")
        fun put_city_returns_200() =
            runTest {
                val id = UUID.randomUUID()
                coEvery { cityService.updateCity(any(), any()) } returns TestDataHelpers.createCityResponseDto()

                withRoutingApp { client ->
                    val updateDto = TestDataHelpers.createCityUpdateDto()
                    val response = client.testPut("/api/v1/cities/$id", createCityUpdateJson(updateDto))

                    assertEquals(HttpStatusCode.OK, response.status)
                }
            }

        @Test
        @DisplayName("GIVEN existing city WHEN DELETE city THEN return 200 OK")
        fun delete_existing_city_returns_200() =
            runTest {
                val id = UUID.randomUUID()
                coEvery { cityService.deleteCity(id) } returns true

                withRoutingApp { client ->
                    val response = client.testDelete("/api/v1/cities/$id")

                    assertEquals(HttpStatusCode.OK, response.status)
                }
            }

        @Test
        @DisplayName("GIVEN non-existent city WHEN DELETE city THEN return 404 Not Found")
        fun delete_non_existent_city_returns_404() =
            runTest {
                val id = UUID.randomUUID()
                coEvery { cityService.deleteCity(id) } returns false

                withRoutingApp { client ->
                    val response = client.testDelete("/api/v1/cities/$id")

                    assertEquals(HttpStatusCode.NotFound, response.status)
                }
            }
    }

    @Nested
    @DisplayName("Error Response Structure Tests")
    inner class ErrorResponseStructureTests {
        @Test
        @DisplayName("GIVEN validation error WHEN request fails THEN response contains errors array")
        fun validation_error_contains_errors_array() =
            runTest {
                coEvery { cityService.createCity(any()) } throws
                    ValidationException(
                        listOf(
                            FieldError("nameRu", "Название обязательно"),
                            FieldError("nameEn", "Name is required")
                        )
                    )

                val invalidDto = TestDataHelpers.createCityCreateDto().copy(nameRu = "")
                withRoutingApp { client ->
                    val response = client.testPost("/api/v1/cities", createCityJson(invalidDto))

                    assertEquals(HttpStatusCode.BadRequest, response.status)
                    response.parseErrorResponse().assertHasValidationErrors()
                }
            }

        @Test
        @DisplayName("GIVEN service unavailable WHEN request fails THEN return 503")
        fun service_unavailable_returns_503() =
            runTest {
                coEvery { cityService.getAllCities(any()) } throws
                    ServiceUnavailableException("Service temporarily unavailable", retryAfter = 60)

                withRoutingApp { client ->
                    val response = client.testGet("/api/v1/cities")

                    assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
                }
            }
    }
}
