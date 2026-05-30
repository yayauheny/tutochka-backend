package yayauheny.by.unit.controller

import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import yayauheny.by.common.query.PageResponse
import yayauheny.by.helpers.assertJsonContentType
import yayauheny.by.helpers.testGet
import yayauheny.by.model.country.CountryResponseDto

class CountryControllerTest : RoutingTestBase() {
    @Nested
    @DisplayName("GET /api/v1/countries")
    inner class GetAllCountries {
        @Test
        @DisplayName("GIVEN countries endpoint WHEN GET /api/v1/countries THEN return 200 and call service with default pagination")
        fun countries_endpoint_returns_200_and_calls_service_with_default_pagination() =
            runTest {
                coEvery { countryService.getAllCountries(any(), any()) } returns
                    PageResponse(emptyList<CountryResponseDto>(), 0, 20, 0, 0, true, true)

                withRoutingApp { client ->
                    val response = client.testGet("/api/v1/countries")

                    assertEquals(HttpStatusCode.OK, response.status)
                    response.assertJsonContentType()
                }

                coVerify(exactly = 1) { countryService.getAllCountries(any(), any()) }
            }

        @Test
        @DisplayName("GIVEN too long nameRu filter WHEN GET countries THEN return 400 and skip service")
        fun countries_with_too_long_name_ru_filter_returns_400_and_skips_service() =
            runTest {
                withRoutingApp { client ->
                    val response =
                        client.testGet(
                            "/api/v1/countries",
                            mapOf("nameRu" to "a".repeat(101))
                        )

                    assertEquals(HttpStatusCode.BadRequest, response.status)
                }

                coVerify(exactly = 0) {
                    countryService.getAllCountries(any(), any())
                }
            }
    }
}
