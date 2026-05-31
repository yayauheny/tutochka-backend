package yayauheny.by.unit.controller

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import yayauheny.by.common.query.PageResponse
import yayauheny.by.controller.CountryController
import yayauheny.by.helpers.any
import yayauheny.by.helpers.assertJsonContentType
import yayauheny.by.helpers.testGet
import yayauheny.by.model.country.CountryResponseDto

@WebMvcTest(CountryController::class)
class CountryControllerTest : RoutingTestBase() {
    @Nested
    @DisplayName("GET /api/v1/countries")
    inner class GetAllCountries {
        @Test
        @DisplayName("GIVEN countries endpoint WHEN GET /api/v1/countries THEN return 200 and call service with default pagination")
        fun countries_endpoint_returns_200_and_calls_service_with_default_pagination() {
            given(countryService.getAllCountries(any(), any())).willReturn(
                PageResponse(emptyList<CountryResponseDto>(), 0, 20, 0, 0, true, true)
            )

            withRoutingApp {
                val response = testGet("/api/v1/countries")

                assertEquals(HttpStatus.OK, response.status)
                response.assertJsonContentType()
            }

            verify(countryService).getAllCountries(any(), any())
        }

        @Test
        @DisplayName("GIVEN too long nameRu filter WHEN GET countries THEN return 400 and skip service")
        fun countries_with_too_long_name_ru_filter_returns_400_and_skips_service() {
            withRoutingApp {
                val response =
                    testGet(
                        "/api/v1/countries",
                        mapOf("nameRu" to "a".repeat(101))
                    )

                assertEquals(HttpStatus.BAD_REQUEST, response.status)
            }

            verifyNoInteractions(countryService)
        }
    }
}
