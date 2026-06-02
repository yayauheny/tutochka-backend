package yayauheny.by.unit.controller

import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import yayauheny.by.analytics.model.AnalyticsIdentityHeaders
import yayauheny.by.model.analytics.AnalyticsSource
import yayauheny.by.common.query.PageResponse
import yayauheny.by.controller.RestroomController
import yayauheny.by.helpers.TestDataHelpers
import yayauheny.by.helpers.any
import yayauheny.by.helpers.assertJsonContentType
import yayauheny.by.helpers.anyDouble
import yayauheny.by.helpers.anyInt
import yayauheny.by.helpers.eq
import yayauheny.by.helpers.testGet
import yayauheny.by.helpers.testPost
import yayauheny.by.model.restroom.RestroomResponseDto

@WebMvcTest(RestroomController::class)
class RestroomControllerTest : RoutingTestBase() {
    @Nested
    @DisplayName("GET /api/v1/restrooms")
    inner class GetAllRestrooms {
        @Test
        @DisplayName("GIVEN restrooms endpoint WHEN GET /api/v1/restrooms THEN return 200 and call service with default pagination")
        fun restrooms_endpoint_returns_200_and_calls_service_with_default_pagination() {
            given(restroomService.getAllRestrooms(any(), any())).willReturn(
                PageResponse(emptyList<RestroomResponseDto>(), 0, 20, 0, 0, true, true)
            )

            withRoutingApp {
                val response = testGet("/api/v1/restrooms")

                assertEquals(HttpStatus.OK, response.status)
                response.assertJsonContentType()
            }

            verify(restroomService).getAllRestrooms(any(), any())
        }

        @Test
        @DisplayName("GIVEN pagination parameters WHEN GET restrooms THEN pass parameters to service")
        fun restrooms_with_pagination_parameters_passes_to_service() {
            given(restroomService.getAllRestrooms(any(), any())).willReturn(
                PageResponse(emptyList(), 1, 10, 0, 0, false, true)
            )

            withRoutingApp {
                val response = testGet("/api/v1/restrooms", mapOf("page" to "1", "size" to "10"))

                assertEquals(HttpStatus.OK, response.status)
            }

            verify(restroomService).getAllRestrooms(any(), any())
        }

        @Test
        @DisplayName("GIVEN too long address filter WHEN GET restrooms THEN return 400 and skip service")
        fun restrooms_with_too_long_address_filter_returns_400_and_skips_service() {
            withRoutingApp {
                val response =
                    testGet(
                        "/api/v1/restrooms",
                        mapOf("address" to "a".repeat(256))
                    )

                assertEquals(HttpStatus.BAD_REQUEST, response.status)
            }

            verifyNoInteractions(restroomService)
        }
    }

    @Nested
    @DisplayName("GET /api/v1/restrooms/{id}")
    inner class GetRestroomById {
        @Test
        @DisplayName("GIVEN valid UUID WHEN GET restroom by ID THEN return 200 and call service")
        fun get_restroom_by_valid_id_returns_200_and_calls_service() {
            val restroomId = UUID.randomUUID()
            given(restroomService.getRestroomById(restroomId)).willReturn(TestDataHelpers.createRestroomResponseDto(id = restroomId))

            withRoutingApp {
                val response = testGet("/api/v1/restrooms/$restroomId")

                assertEquals(HttpStatus.OK, response.status)
                response.assertJsonContentType()
            }

            verify(restroomService).getRestroomById(restroomId)
        }

        @Test
        @DisplayName("GIVEN restroom not found WHEN GET restroom by ID THEN return 404")
        fun get_restroom_not_found_returns_404() {
            val restroomId = UUID.randomUUID()
            given(restroomService.getRestroomById(restroomId)).willReturn(null)

            withRoutingApp {
                val response = testGet("/api/v1/restrooms/$restroomId")

                assertEquals(HttpStatus.NOT_FOUND, response.status)
            }

            verify(restroomService).getRestroomById(restroomId)
        }

        @Test
        @DisplayName("GIVEN invalid UUID WHEN GET restroom by ID THEN return 400 and skip service")
        fun get_restroom_by_invalid_uuid_returns_400_and_skips_service() {
            withRoutingApp {
                val response = testGet("/api/v1/restrooms/not-a-uuid")

                assertEquals(HttpStatus.BAD_REQUEST, response.status)
            }

            verifyNoInteractions(restroomService)
        }
    }

    @Nested
    @DisplayName("GET /api/v1/restrooms/city/{cityId}")
    inner class GetRestroomsByCityId {
        @Test
        @DisplayName("GIVEN valid city ID WHEN GET restrooms by city THEN return 200 and call service")
        fun get_restrooms_by_valid_city_id_returns_200_and_calls_service() {
            val cityId = UUID.randomUUID()
            given(restroomService.getRestroomsByCity(any(), any())).willReturn(
                PageResponse(emptyList<RestroomResponseDto>(), 0, 20, 0, 0, true, true)
            )

            withRoutingApp {
                val response = testGet("/api/v1/restrooms/city/$cityId")

                assertEquals(HttpStatus.OK, response.status)
                response.assertJsonContentType()
            }

            verify(restroomService).getRestroomsByCity(eq(cityId), any())
        }

        @Test
        @DisplayName("GIVEN invalid city ID WHEN GET restrooms by city THEN return 400 and skip service")
        fun get_restrooms_by_invalid_city_id_returns_400_and_skips_service() {
            withRoutingApp {
                val response = testGet("/api/v1/restrooms/city/invalid-uuid")

                assertEquals(HttpStatus.BAD_REQUEST, response.status)
            }

            verifyNoInteractions(restroomService)
        }
    }

    @Nested
    @DisplayName("GET /api/v1/restrooms/nearest")
    inner class GetNearestRestrooms {
        @Test
        @DisplayName("GIVEN valid coordinates WHEN GET nearest restrooms THEN return 200 and call service with parsed values")
        fun get_nearest_restrooms_with_valid_coordinates_returns_200_and_calls_service() {
            given(restroomService.findNearestRestrooms(anyDouble(), anyDouble(), anyInt(), anyInt())).willReturn(emptyList())

            withRoutingApp {
                val response =
                    testGet(
                        "/api/v1/restrooms/nearest",
                        mapOf("lat" to "55.7558", "lon" to "37.6176", "limit" to "10")
                    )

                assertEquals(HttpStatus.OK, response.status)
                response.assertJsonContentType()
            }

            verify(restroomService).findNearestRestrooms(eq(55.7558), eq(37.6176), eq(10), anyInt())
        }

        @Test
        @DisplayName("GIVEN coordinates without limit WHEN GET nearest restrooms THEN use default limit")
        fun get_nearest_restrooms_without_limit_uses_default() {
            given(restroomService.findNearestRestrooms(anyDouble(), anyDouble(), anyInt(), anyInt())).willReturn(emptyList())

            withRoutingApp {
                val response =
                    testGet(
                        "/api/v1/restrooms/nearest",
                        mapOf("lat" to "55.7558", "lon" to "37.6176")
                    )

                assertEquals(HttpStatus.OK, response.status)
            }

            verify(restroomService).findNearestRestrooms(eq(55.7558), eq(37.6176), eq(50), anyInt())
        }

        @Test
        @DisplayName("GIVEN no analytics source header and no Telegram identity WHEN GET nearest THEN track analytics as API traffic")
        fun get_nearest_restrooms_without_source_or_telegram_identity_tracks_api_source() {
            given(restroomService.findNearestRestrooms(anyDouble(), anyDouble(), anyInt(), anyInt())).willReturn(emptyList())
            var capturedIdentity: AnalyticsIdentityHeaders? = null
            doAnswer {
                capturedIdentity = it.getArgument(0)
                null
            }.`when`(analyticsService).trackNearestRestroomsSearch(any(), anyInt(), anyDouble(), anyDouble())

            withRoutingApp {
                val response =
                    testGet(
                        "/api/v1/restrooms/nearest",
                        mapOf("lat" to "55.7558", "lon" to "37.6176")
                    )

                assertEquals(HttpStatus.OK, response.status)
            }

            verify(analyticsService).trackNearestRestroomsSearch(any(), eq(0), eq(55.7558), eq(37.6176))
            assertEquals(AnalyticsSource.API, capturedIdentity?.source)
        }

        @Test
        @DisplayName("GIVEN no analytics source header and Telegram identity WHEN GET nearest THEN track analytics as Telegram bot traffic")
        fun get_nearest_restrooms_with_telegram_identity_tracks_telegram_bot_source() {
            given(restroomService.findNearestRestrooms(anyDouble(), anyDouble(), anyInt(), anyInt())).willReturn(emptyList())
            var capturedIdentity: AnalyticsIdentityHeaders? = null
            doAnswer {
                capturedIdentity = it.getArgument(0)
                null
            }.`when`(analyticsService).trackNearestRestroomsSearch(any(), anyInt(), anyDouble(), anyDouble())

            withRoutingApp {
                val response =
                    testGet(
                        "/api/v1/restrooms/nearest",
                        mapOf("lat" to "55.7558", "lon" to "37.6176"),
                        mapOf(
                            "X-Tg-User-Id" to "42",
                            "X-Tg-Chat-Id" to "99",
                            "X-Tg-Username" to "user42"
                        )
                    )

                assertEquals(HttpStatus.OK, response.status)
            }

            verify(analyticsService).trackNearestRestroomsSearch(any(), eq(0), eq(55.7558), eq(37.6176))
            assertEquals(AnalyticsSource.TELEGRAM_BOT, capturedIdentity?.source)
        }

        @Test
        @DisplayName("GIVEN invalid coordinates WHEN GET nearest restrooms THEN return 400 and skip service")
        fun get_nearest_restrooms_with_invalid_coordinates_returns_400_and_skips_service() {
            withRoutingApp {
                val response =
                    testGet(
                        "/api/v1/restrooms/nearest",
                        mapOf("lat" to "invalid", "lon" to "37.6176")
                    )

                assertEquals(HttpStatus.BAD_REQUEST, response.status)
            }

            verifyNoInteractions(restroomService)
        }

        @Test
        @DisplayName("GIVEN missing coordinates WHEN GET nearest restrooms THEN return 400 and skip service")
        fun get_nearest_restrooms_with_missing_coordinates_returns_400_and_skips_service() {
            withRoutingApp {
                val response =
                    testGet(
                        "/api/v1/restrooms/nearest",
                        mapOf("lon" to "37.6176")
                    )

                assertEquals(HttpStatus.BAD_REQUEST, response.status)
            }

            verifyNoInteractions(restroomService)
        }

        @Test
        @DisplayName("GIVEN out of range coordinates WHEN GET nearest restrooms THEN return 400")
        fun get_nearest_restrooms_with_out_of_range_coordinates_returns_400() {
            given(restroomService.findNearestRestrooms(anyDouble(), anyDouble(), anyInt(), anyInt())).willReturn(emptyList())

            withRoutingApp {
                val response =
                    testGet(
                        "/api/v1/restrooms/nearest",
                        mapOf("lat" to "91.0", "lon" to "37.6176", "limit" to "5")
                    )

                assertEquals(HttpStatus.BAD_REQUEST, response.status)
                response.assertJsonContentType()
            }

            verifyNoInteractions(restroomService)
        }
    }

    @Nested
    @DisplayName("POST /api/v1/restrooms")
    inner class CreateRestroom {
        @Test
        @DisplayName("GIVEN valid restroom data WHEN POST restroom THEN return 201 and call service")
        fun create_restroom_with_valid_data_returns_201_and_calls_service() {
            val cityId = UUID.randomUUID()
            given(restroomService.createRestroom(any())).willReturn(TestDataHelpers.createRestroomResponseDto())

            val validJson =
                """
                {
                    "cityId": "$cityId",
                    "status": "ACTIVE",
                    "name": "Test Restroom",
                    "description": "Test Description",
                    "address": "Test Address 123",
                    "phones": {},
                    "workTime": {},
                    "feeType": "FREE",
                    "genderType": "UNISEX",
                    "accessibilityType": "WHEELCHAIR",
                    "coordinates": {
                        "lat": 55.7558,
                        "lon": 37.6176
                    },
                    "dataSource": "MANUAL",
                    "amenities": {},
                    "buildingId": "${UUID.randomUUID()}",
                    "inheritBuildingSchedule": true
                }
                """.trimIndent()

            withRoutingApp {
                val response = testPost("/api/v1/restrooms", validJson)

                assertEquals(HttpStatus.CREATED, response.status)
                response.assertJsonContentType()
            }

            verify(restroomService).createRestroom(any())
        }

        @Test
        @DisplayName("GIVEN invalid JSON WHEN POST restroom THEN return 400 and skip service")
        fun create_restroom_with_invalid_json_returns_400_and_skips_service() {
            withRoutingApp {
                val response = testPost("/api/v1/restrooms", "{ invalid json }")

                assertEquals(HttpStatus.BAD_REQUEST, response.status)
            }

            verifyNoInteractions(restroomService)
        }

        @Test
        @DisplayName("GIVEN malformed JSON WHEN POST restroom THEN return 400 and skip service")
        fun create_restroom_with_malformed_json_returns_400_and_skips_service() {
            withRoutingApp {
                val response = testPost("/api/v1/restrooms", """{"name": "Test"}""")

                assertEquals(HttpStatus.BAD_REQUEST, response.status)
            }

            verifyNoInteractions(restroomService)
        }

        @Test
        @DisplayName("GIVEN empty request body WHEN POST restroom THEN return 400 and skip service")
        fun create_restroom_with_empty_body_returns_400_and_skips_service() {
            withRoutingApp {
                val response = testPost("/api/v1/restrooms", "")

                assertEquals(HttpStatus.BAD_REQUEST, response.status)
            }

            verifyNoInteractions(restroomService)
        }
    }
}
