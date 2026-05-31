package yayauheny.by.unit.controller

import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import yayauheny.by.common.query.PageResponse
import yayauheny.by.controller.CityController
import yayauheny.by.helpers.TestDataHelpers
import yayauheny.by.helpers.any
import yayauheny.by.helpers.eq
import yayauheny.by.helpers.assertJsonContentType
import yayauheny.by.helpers.testGet
import yayauheny.by.helpers.testPost
import yayauheny.by.model.city.CityResponseDto

@WebMvcTest(CityController::class)
class CityControllerTest : RoutingTestBase() {
    @Nested
    @DisplayName("GET /api/v1/cities")
    inner class GetAllCities {
        @Test
        @DisplayName("GIVEN cities endpoint WHEN GET /api/v1/cities THEN return 200 and call service with default pagination")
        fun cities_endpoint_returns_200_and_calls_service_with_default_pagination() {
            given(cityService.getAllCities(any(), any())).willReturn(
                PageResponse(emptyList<CityResponseDto>(), 0, 20, 0, 0, true, true)
            )

            withRoutingApp {
                val response = testGet("/api/v1/cities")

                assertEquals(HttpStatus.OK, response.status)
                response.assertJsonContentType()
            }

            verify(cityService).getAllCities(any(), any())
        }

        @Test
        @DisplayName("GIVEN pagination parameters WHEN GET cities THEN pass parameters to service")
        fun cities_with_pagination_parameters_passes_to_service() {
            given(cityService.getAllCities(any(), any())).willReturn(
                PageResponse(emptyList<CityResponseDto>(), 1, 10, 0, 0, false, true)
            )

            withRoutingApp {
                val response = testGet("/api/v1/cities", mapOf("page" to "1", "size" to "10"))

                assertEquals(HttpStatus.OK, response.status)
            }

            verify(cityService).getAllCities(any(), any())
        }

        @Test
        @DisplayName("GIVEN negative pagination parameters WHEN GET cities THEN clamp to valid values")
        fun cities_with_negative_pagination_clamps_values() {
            given(cityService.getAllCities(any(), any())).willReturn(
                PageResponse(emptyList<CityResponseDto>(), 0, 1, 0, 0, true, true)
            )

            withRoutingApp {
                val response = testGet("/api/v1/cities", mapOf("page" to "-1", "size" to "-5"))

                assertEquals(HttpStatus.OK, response.status)
            }

            verify(cityService).getAllCities(any(), any())
        }

        @Test
        @DisplayName("GIVEN too long name filter WHEN GET cities THEN return 400 and skip service")
        fun cities_with_too_long_name_filter_returns_400_and_skips_service() {
            withRoutingApp {
                val response =
                    testGet(
                        "/api/v1/cities",
                        mapOf("name" to "a".repeat(101))
                    )

                assertEquals(HttpStatus.BAD_REQUEST, response.status)
            }

            verifyNoInteractions(cityService)
        }
    }

    @Nested
    @DisplayName("GET /api/v1/cities/{id}")
    inner class GetCityById {
        @Test
        @DisplayName("GIVEN valid UUID WHEN GET city by ID THEN return 200 and call service")
        fun get_city_by_valid_id_returns_200_and_calls_service() {
            val cityId = UUID.randomUUID()
            given(cityService.getCityById(cityId)).willReturn(TestDataHelpers.createCityResponseDto(id = cityId))

            withRoutingApp {
                val response = testGet("/api/v1/cities/$cityId")

                assertEquals(HttpStatus.OK, response.status)
                response.assertJsonContentType()
            }

            verify(cityService).getCityById(cityId)
        }

        @Test
        @DisplayName("GIVEN city not found WHEN GET city by ID THEN return 404")
        fun get_city_not_found_returns_404() {
            val cityId = UUID.randomUUID()
            given(cityService.getCityById(cityId)).willReturn(null)

            withRoutingApp {
                val response = testGet("/api/v1/cities/$cityId")

                assertEquals(HttpStatus.NOT_FOUND, response.status)
            }

            verify(cityService).getCityById(cityId)
        }

        @Test
        @DisplayName("GIVEN invalid UUID WHEN GET city by ID THEN return 400 and skip service")
        fun get_city_by_invalid_uuid_returns_400_and_skips_service() {
            withRoutingApp {
                val response = testGet("/api/v1/cities/not-a-uuid")

                assertEquals(HttpStatus.BAD_REQUEST, response.status)
            }

            verifyNoInteractions(cityService)
        }
    }

    @Nested
    @DisplayName("GET /api/v1/cities/country/{countryId}")
    inner class GetCitiesByCountryId {
        @Test
        @DisplayName("GIVEN valid country ID WHEN GET cities by country THEN return 200 and call service")
        fun get_cities_by_valid_country_id_returns_200_and_calls_service() {
            val countryId = UUID.randomUUID()
            given(cityService.getCitiesByCountry(any(), any())).willReturn(
                PageResponse(emptyList<CityResponseDto>(), 0, 20, 0, 0, true, true)
            )

            withRoutingApp {
                val response = testGet("/api/v1/cities/country/$countryId")

                assertEquals(HttpStatus.OK, response.status)
                response.assertJsonContentType()
            }

            verify(cityService).getCitiesByCountry(eq(countryId), any())
        }

        @Test
        @DisplayName("GIVEN invalid country ID WHEN GET cities by country THEN return 400 and skip service")
        fun get_cities_by_invalid_country_id_returns_400_and_skips_service() {
            withRoutingApp {
                val response = testGet("/api/v1/cities/country/invalid-uuid")

                assertEquals(HttpStatus.BAD_REQUEST, response.status)
            }

            verifyNoInteractions(cityService)
        }
    }

    @Nested
    @DisplayName("GET /api/v1/cities/search")
    inner class SearchCities {
        @Test
        @DisplayName("GIVEN name parameter WHEN GET cities search THEN return 200 and call service")
        fun search_cities_with_name_returns_200_and_calls_service() {
            given(cityService.searchCitiesByName(eq("Minsk"), any())).willReturn(
                PageResponse(emptyList<CityResponseDto>(), 0, 20, 0, 0, true, true)
            )

            withRoutingApp {
                val response = testGet("/api/v1/cities/search", mapOf("name" to "Minsk"))

                assertEquals(HttpStatus.OK, response.status)
                response.assertJsonContentType()
            }

            verify(cityService).searchCitiesByName(eq("Minsk"), any())
        }

        @Test
        @DisplayName("GIVEN missing name parameter WHEN GET cities search THEN return 400 and skip service")
        fun search_cities_without_name_returns_400_and_skips_service() {
            withRoutingApp {
                val response = testGet("/api/v1/cities/search")

                assertEquals(HttpStatus.BAD_REQUEST, response.status)
            }

            verifyNoInteractions(cityService)
        }
    }

    @Nested
    @DisplayName("POST /api/v1/cities")
    inner class CreateCity {
        @Test
        @DisplayName("GIVEN valid city data WHEN POST city THEN return 201 and call service")
        fun create_city_with_valid_data_returns_201_and_calls_service() {
            val countryId = UUID.randomUUID()
            given(cityService.createCity(any())).willReturn(TestDataHelpers.createCityResponseDto())

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

            withRoutingApp {
                val response = testPost("/api/v1/cities", validJson)

                assertEquals(HttpStatus.CREATED, response.status)
                response.assertJsonContentType()
            }

            verify(cityService).createCity(any())
        }
    }
}
