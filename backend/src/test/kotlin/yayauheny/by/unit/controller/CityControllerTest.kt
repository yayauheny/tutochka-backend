package yayauheny.by.unit.controller

import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.coVerify
import java.util.UUID
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import yayauheny.by.helpers.TestDataHelpers
import yayauheny.by.helpers.assertJsonContentType
import yayauheny.by.helpers.testGet
import yayauheny.by.helpers.testPost
import yayauheny.by.common.query.PageResponse
import yayauheny.by.model.city.CityResponseDto

class CityControllerTest : RoutingTestBase() {
    @Nested
    @DisplayName("GET /api/v1/cities")
    inner class GetAllCities {
        @Test
        @DisplayName("GIVEN cities endpoint WHEN GET /api/v1/cities THEN return 200 and call service with default pagination")
        fun cities_endpoint_returns_200_and_calls_service_with_default_pagination() =
            runTest {
                // Given
                coEvery { cityService.getAllCities(any()) } returns PageResponse(emptyList<CityResponseDto>(), 0, 20, 0, 0, true, true)

                // When
                withRoutingApp { client ->
                    val response = client.testGet("/api/v1/cities")

                    // Then
                    assertEquals(HttpStatusCode.OK, response.status)
                    response.assertJsonContentType()
                }

                coVerify(exactly = 1) {
                    cityService.getAllCities(any())
                }
            }

        @Test
        @DisplayName("GIVEN pagination parameters WHEN GET cities THEN pass parameters to service")
        fun cities_with_pagination_parameters_passes_to_service() =
            runTest {
                // Given
                coEvery { cityService.getAllCities(any()) } returns PageResponse(emptyList<CityResponseDto>(), 1, 10, 0, 0, false, true)

                // When
                withRoutingApp { client ->
                    val response = client.testGet("/api/v1/cities", mapOf("page" to "1", "size" to "10"))

                    // Then
                    assertEquals(HttpStatusCode.OK, response.status)
                }

                coVerify(exactly = 1) {
                    cityService.getAllCities(any())
                }
            }

        @Test
        @DisplayName("GIVEN negative pagination parameters WHEN GET cities THEN clamp to valid values")
        fun cities_with_negative_pagination_clamps_values() =
            runTest {
                // Given
                coEvery { cityService.getAllCities(any()) } returns PageResponse(emptyList<CityResponseDto>(), 0, 1, 0, 0, true, true)

                // When
                withRoutingApp { client ->
                    val response = client.testGet("/api/v1/cities", mapOf("page" to "-1", "size" to "-5"))

                    // Then
                    assertEquals(HttpStatusCode.OK, response.status)
                }

                coVerify(exactly = 1) {
                    cityService.getAllCities(any())
                }
            }
    }

    @Nested
    @DisplayName("GET /api/v1/cities/{id}")
    inner class GetCityById {
        @Test
        @DisplayName("GIVEN valid UUID WHEN GET city by ID THEN return 200 and call service")
        fun get_city_by_valid_id_returns_200_and_calls_service() =
            runTest {
                // Given
                val cityId = UUID.randomUUID()
                coEvery { cityService.getCityById(any()) } returns TestDataHelpers.createCityResponseDto(id = cityId)

                // When
                withRoutingApp { client ->
                    val response = client.testGet("/api/v1/cities/$cityId")

                    // Then
                    assertEquals(HttpStatusCode.OK, response.status)
                    response.assertJsonContentType()
                }

                coVerify(exactly = 1) { cityService.getCityById(cityId) }
            }

        @Test
        @DisplayName("GIVEN city not found WHEN GET city by ID THEN return 404")
        fun get_city_not_found_returns_404() =
            runTest {
                // Given
                val cityId = UUID.randomUUID()
                coEvery { cityService.getCityById(any()) } returns null

                // When
                withRoutingApp { client ->
                    val response = client.testGet("/api/v1/cities/$cityId")

                    // Then
                    assertEquals(HttpStatusCode.NotFound, response.status)
                }

                coVerify(exactly = 1) { cityService.getCityById(cityId) }
            }

        @Test
        @DisplayName("GIVEN invalid UUID WHEN GET city by ID THEN return 400 and skip service")
        fun get_city_by_invalid_uuid_returns_400_and_skips_service() =
            runTest {
                // Given
                // (invalid UUID format)

                // When
                withRoutingApp { client ->
                    val response = client.testGet("/api/v1/cities/not-a-uuid")

                    // Then
                    assertEquals(HttpStatusCode.BadRequest, response.status)
                }

                coVerify(exactly = 0) { cityService.getCityById(any()) }
            }
    }

    @Nested
    @DisplayName("GET /api/v1/cities/country/{countryId}")
    inner class GetCitiesByCountryId {
        @Test
        @DisplayName("GIVEN valid country ID WHEN GET cities by country THEN return 200 and call service")
        fun get_cities_by_valid_country_id_returns_200_and_calls_service() =
            runTest {
                // Given
                val countryId = UUID.randomUUID()
                coEvery { cityService.getCitiesByCountry(any(), any()) } returns
                    PageResponse(emptyList<CityResponseDto>(), 0, 20, 0, 0, true, true)

                // When
                withRoutingApp { client ->
                    val response = client.testGet("/api/v1/cities/country/$countryId")

                    // Then
                    assertEquals(HttpStatusCode.OK, response.status)
                    response.assertJsonContentType()
                }

                coVerify(exactly = 1) {
                    cityService.getCitiesByCountry(countryId, any())
                }
            }

        @Test
        @DisplayName("GIVEN invalid country ID WHEN GET cities by country THEN return 400 and skip service")
        fun get_cities_by_invalid_country_id_returns_400_and_skips_service() =
            runTest {
                // Given
                // (invalid UUID format)

                // When
                withRoutingApp { client ->
                    val response = client.testGet("/api/v1/cities/country/invalid-uuid")

                    // Then
                    assertEquals(HttpStatusCode.BadRequest, response.status)
                }

                coVerify(exactly = 0) { cityService.getCitiesByCountry(any(), any()) }
            }
    }

    @Nested
    @DisplayName("GET /api/v1/cities/search")
    inner class SearchCities {
        @Test
        @DisplayName("GIVEN name parameter WHEN GET cities search THEN return 200 and call service")
        fun search_cities_with_name_returns_200_and_calls_service() =
            runTest {
                // Given
                coEvery { cityService.searchCitiesByName(any(), any()) } returns
                    PageResponse(emptyList<CityResponseDto>(), 0, 20, 0, 0, true, true)

                // When
                withRoutingApp { client ->
                    val response = client.testGet("/api/v1/cities/search", mapOf("name" to "Minsk"))

                    // Then
                    assertEquals(HttpStatusCode.OK, response.status)
                    response.assertJsonContentType()
                }

                coVerify(exactly = 1) {
                    cityService.searchCitiesByName("Minsk", any())
                }
            }

        @Test
        @DisplayName("GIVEN missing name parameter WHEN GET cities search THEN return 400 and skip service")
        fun search_cities_without_name_returns_400_and_skips_service() =
            runTest {
                // Given
                // (missing name parameter)

                // When
                withRoutingApp { client ->
                    val response = client.testGet("/api/v1/cities/search")

                    // Then
                    assertEquals(HttpStatusCode.BadRequest, response.status)
                }

                coVerify(exactly = 0) { cityService.searchCitiesByName(any(), any()) }
            }
    }

    @Nested
    @DisplayName("POST /api/v1/cities")
    inner class CreateCity {
        @Test
        @DisplayName("GIVEN valid city data WHEN POST city THEN return 201 and call service")
        fun create_city_with_valid_data_returns_201_and_calls_service() =
            runTest {
                // Given
                val countryId = UUID.randomUUID()
                coEvery { cityService.createCity(any()) } returns TestDataHelpers.createCityResponseDto()

                val validJson =
                    """
                    {
                        "countryId": "$countryId",
                        "nameRu": "Минск",
                        "nameEn": "Minsk",
                        "coordinates": {
                            "lat": 53.9006,
                            "lon": 27.5590
                        }
                    }
                    """.trimIndent()

                // When
                withRoutingApp { client ->
                    val response = client.testPost("/api/v1/cities", validJson)

                    // Then
                    assertEquals(HttpStatusCode.Created, response.status)
                    response.assertJsonContentType()
                }

                coVerify(exactly = 1) {
                    cityService.createCity(
                        any()
                    )
                }
            }

        @Test
        @DisplayName("GIVEN invalid JSON WHEN POST city THEN return 400 and skip service")
        fun create_city_with_invalid_json_returns_400_and_skips_service() =
            runTest {
                // Given
                // (invalid JSON syntax)

                // When
                withRoutingApp { client ->
                    val response = client.testPost("/api/v1/cities", "{ invalid json }")

                    // Then
                    assertEquals(HttpStatusCode.BadRequest, response.status)
                }

                coVerify(exactly = 0) { cityService.createCity(any()) }
            }

        @Test
        @DisplayName("GIVEN malformed JSON WHEN POST city THEN return 400 and skip service")
        fun create_city_with_malformed_json_returns_400_and_skips_service() =
            runTest {
                // Given
                // (missing required fields)

                // When
                withRoutingApp { client ->
                    val response = client.testPost("/api/v1/cities", """{"nameRu": "Test"}""") // Missing required fields

                    // Then
                    assertEquals(HttpStatusCode.BadRequest, response.status)
                }

                coVerify(exactly = 0) { cityService.createCity(any()) }
            }

        @Test
        @DisplayName("GIVEN empty request body WHEN POST city THEN return 415 and skip service")
        fun create_city_with_empty_body_returns_415_and_skips_service() =
            runTest {
                // Given
                // (empty body)

                // When
                withRoutingApp { client ->
                    val response = client.testPost("/api/v1/cities", "")

                    // Then
                    assertEquals(HttpStatusCode.UnsupportedMediaType, response.status)
                }

                coVerify(exactly = 0) { cityService.createCity(any()) }
            }
    }
}
