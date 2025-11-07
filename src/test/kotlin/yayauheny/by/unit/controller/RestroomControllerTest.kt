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
import support.helpers.TestDataHelpers
import yayauheny.by.helpers.assertJsonContentType
import yayauheny.by.helpers.testGet
import yayauheny.by.helpers.testPost
import yayauheny.by.model.PageResponseDto

class RestroomControllerTest : RoutingTestBase() {
    @Nested
    @DisplayName("GET /api/v1/restrooms")
    inner class GetAllRestrooms {
        @Test
        @DisplayName("GIVEN restrooms endpoint WHEN GET /api/v1/restrooms THEN return 200 and call service with default pagination")
        fun restrooms_endpoint_returns_200_and_calls_service_with_default_pagination() =
            runTest {
                coEvery { restroomService.getAllRestrooms(any()) } returns PageResponseDto(emptyList(), 0, 20, 0, 0, true, true)

                withRoutingApp { client ->
                    val response = client.testGet("/api/v1/restrooms")

                    assertEquals(HttpStatusCode.OK, response.status)
                    response.assertJsonContentType()
                }

                coVerify(exactly = 1) { restroomService.getAllRestrooms(any()) }
            }

        @Test
        @DisplayName("GIVEN pagination parameters WHEN GET restrooms THEN pass parameters to service")
        fun restrooms_with_pagination_parameters_passes_to_service() =
            runTest {
                coEvery { restroomService.getAllRestrooms(any()) } returns PageResponseDto(emptyList(), 1, 10, 0, 0, false, true)

                withRoutingApp { client ->
                    val response = client.testGet("/api/v1/restrooms", mapOf("page" to "1", "size" to "10"))

                    assertEquals(HttpStatusCode.OK, response.status)
                }

                coVerify(exactly = 1) {
                    restroomService.getAllRestrooms(any())
                }
            }
    }

    @Nested
    @DisplayName("GET /api/v1/restrooms/{id}")
    inner class GetRestroomById {
        @Test
        @DisplayName("GIVEN valid UUID WHEN GET restroom by ID THEN return 200 and call service")
        fun get_restroom_by_valid_id_returns_200_and_calls_service() =
            runTest {
                val restroomId = UUID.randomUUID()
                coEvery { restroomService.getRestroomById(any()) } returns TestDataHelpers.createRestroomResponseDto(id = restroomId)

                withRoutingApp { client ->
                    val response = client.testGet("/api/v1/restrooms/$restroomId")

                    assertEquals(HttpStatusCode.OK, response.status)
                    response.assertJsonContentType()
                }

                coVerify(exactly = 1) { restroomService.getRestroomById(restroomId) }
            }

        @Test
        @DisplayName("GIVEN restroom not found WHEN GET restroom by ID THEN return 404")
        fun get_restroom_not_found_returns_404() =
            runTest {
                val restroomId = UUID.randomUUID()
                coEvery { restroomService.getRestroomById(any()) } returns null

                withRoutingApp { client ->
                    val response = client.testGet("/api/v1/restrooms/$restroomId")

                    assertEquals(HttpStatusCode.NotFound, response.status)
                }

                coVerify(exactly = 1) { restroomService.getRestroomById(restroomId) }
            }

        @Test
        @DisplayName("GIVEN invalid UUID WHEN GET restroom by ID THEN return 400 and skip service")
        fun get_restroom_by_invalid_uuid_returns_400_and_skips_service() =
            runTest {
                withRoutingApp { client ->
                    val response = client.testGet("/api/v1/restrooms/not-a-uuid")

                    assertEquals(HttpStatusCode.BadRequest, response.status)
                }

                coVerify(exactly = 0) { restroomService.getRestroomById(any()) }
            }
    }

    @Nested
    @DisplayName("GET /api/v1/restrooms/city/{cityId}")
    inner class GetRestroomsByCityId {
        @Test
        @DisplayName("GIVEN valid city ID WHEN GET restrooms by city THEN return 200 and call service")
        fun get_restrooms_by_valid_city_id_returns_200_and_calls_service() =
            runTest {
                val cityId = UUID.randomUUID()
                coEvery { restroomService.getRestroomsByCity(any(), any()) } returns PageResponseDto(emptyList(), 0, 20, 0, 0, true, true)

                withRoutingApp { client ->
                    val response = client.testGet("/api/v1/restrooms/city/$cityId")

                    assertEquals(HttpStatusCode.OK, response.status)
                    response.assertJsonContentType()
                }

                coVerify(exactly = 1) {
                    restroomService.getRestroomsByCity(cityId, any())
                }
            }

        @Test
        @DisplayName("GIVEN invalid city ID WHEN GET restrooms by city THEN return 400 and skip service")
        fun get_restrooms_by_invalid_city_id_returns_400_and_skips_service() =
            runTest {
                withRoutingApp { client ->
                    val response = client.testGet("/api/v1/restrooms/city/invalid-uuid")

                    assertEquals(HttpStatusCode.BadRequest, response.status)
                }

                coVerify(exactly = 0) { restroomService.getRestroomsByCity(any(), any()) }
            }
    }

    @Nested
    @DisplayName("GET /api/v1/restrooms/nearest")
    inner class GetNearestRestrooms {
        @Test
        @DisplayName("GIVEN valid coordinates WHEN GET nearest restrooms THEN return 200 and call service with parsed values")
        fun get_nearest_restrooms_with_valid_coordinates_returns_200_and_calls_service() =
            runTest {
                coEvery { restroomService.findNearestRestrooms(any(), any(), any()) } returns emptyList()

                withRoutingApp { client ->
                    val response =
                        client.testGet(
                            "/api/v1/restrooms/nearest",
                            mapOf("lat" to "55.7558", "lon" to "37.6176", "limit" to "10")
                        )

                    assertEquals(HttpStatusCode.OK, response.status)
                    response.assertJsonContentType()
                }

                coVerify(exactly = 1) {
                    restroomService.findNearestRestrooms(55.7558, 37.6176, 10)
                }
            }

        @Test
        @DisplayName("GIVEN coordinates without limit WHEN GET nearest restrooms THEN use default limit")
        fun get_nearest_restrooms_without_limit_uses_default() =
            runTest {
                coEvery { restroomService.findNearestRestrooms(any(), any(), any()) } returns emptyList()

                withRoutingApp { client ->
                    val response =
                        client.testGet(
                            "/api/v1/restrooms/nearest",
                            mapOf("lat" to "55.7558", "lon" to "37.6176")
                        )

                    assertEquals(HttpStatusCode.OK, response.status)
                }

                coVerify(exactly = 1) {
                    restroomService.findNearestRestrooms(55.7558, 37.6176, 5)
                }
            }

        @Test
        @DisplayName("GIVEN invalid coordinates WHEN GET nearest restrooms THEN return 400 and skip service")
        fun get_nearest_restrooms_with_invalid_coordinates_returns_400_and_skips_service() =
            runTest {
                withRoutingApp { client ->
                    val response =
                        client.testGet(
                            "/api/v1/restrooms/nearest",
                            mapOf("lat" to "invalid", "lon" to "37.6176")
                        )

                    assertEquals(HttpStatusCode.BadRequest, response.status)
                }

                coVerify(exactly = 0) { restroomService.findNearestRestrooms(any(), any(), any()) }
            }

        @Test
        @DisplayName("GIVEN missing coordinates WHEN GET nearest restrooms THEN return 400 and skip service")
        fun get_nearest_restrooms_with_missing_coordinates_returns_400_and_skips_service() =
            runTest {
                withRoutingApp { client ->
                    val response =
                        client.testGet(
                            "/api/v1/restrooms/nearest",
                            mapOf("lon" to "37.6176")
                        )

                    assertEquals(HttpStatusCode.BadRequest, response.status)
                }

                coVerify(exactly = 0) { restroomService.findNearestRestrooms(any(), any(), any()) }
            }

        @Test
        @DisplayName("GIVEN out of range coordinates WHEN GET nearest restrooms THEN return 400")
        fun get_nearest_restrooms_with_out_of_range_coordinates_returns_400() =
            runTest {
                coEvery { restroomService.findNearestRestrooms(any(), any(), any()) } returns emptyList()

                withRoutingApp { client ->
                    val response =
                        client.testGet(
                            "/api/v1/restrooms/nearest",
                            mapOf("lat" to "91.0", "lon" to "37.6176", "limit" to "5")
                        )

                    assertEquals(HttpStatusCode.BadRequest, response.status)
                    response.assertJsonContentType()
                }

                coVerify(exactly = 0) {
                    restroomService.findNearestRestrooms(any(), any(), any())
                }
            }
    }

    @Nested
    @DisplayName("POST /api/v1/restrooms")
    inner class CreateRestroom {
        @Test
        @DisplayName("GIVEN valid restroom data WHEN POST restroom THEN return 201 and call service")
        fun create_restroom_with_valid_data_returns_201_and_calls_service() =
            runTest {
                val cityId = UUID.randomUUID()
                coEvery { restroomService.createRestroom(any()) } returns TestDataHelpers.createRestroomResponseDto()

                val validJson =
                    """
                    {
                        "cityId": "$cityId",
                        "name": "Test Restroom",
                        "description": "Test Description",
                        "address": "Test Address 123",
                        "phones": {},
                        "workTime": {},
                        "feeType": "FREE",
                        "accessibilityType": "UNISEX",
                        "lat": 55.7558,
                        "lon": 37.6176,
                        "dataSource": "MANUAL",
                        "amenities": {},
                        "parentPlaceName": "Test Mall",
                        "parentPlaceType": "SHOPPING_MALL",
                        "inheritParentSchedule": true
                    }
                    """.trimIndent()

                withRoutingApp { client ->
                    val response = client.testPost("/api/v1/restrooms", validJson)

                    assertEquals(HttpStatusCode.Created, response.status)
                    response.assertJsonContentType()
                }

                coVerify(exactly = 1) {
                    restroomService.createRestroom(
                        any()
                    )
                }
            }

        @Test
        @DisplayName("GIVEN invalid JSON WHEN POST restroom THEN return 400 and skip service")
        fun create_restroom_with_invalid_json_returns_400_and_skips_service() =
            runTest {
                withRoutingApp { client ->
                    val response = client.testPost("/api/v1/restrooms", "{ invalid json }")

                    assertEquals(HttpStatusCode.BadRequest, response.status)
                }

                coVerify(exactly = 0) { restroomService.createRestroom(any()) }
            }

        @Test
        @DisplayName("GIVEN malformed JSON WHEN POST restroom THEN return 400 and skip service")
        fun create_restroom_with_malformed_json_returns_400_and_skips_service() =
            runTest {
                withRoutingApp { client ->
                    val response = client.testPost("/api/v1/restrooms", """{"name": "Test"}""") // Missing required fields

                    assertEquals(HttpStatusCode.BadRequest, response.status)
                }

                coVerify(exactly = 0) { restroomService.createRestroom(any()) }
            }

        @Test
        @DisplayName("GIVEN empty request body WHEN POST restroom THEN return 415 and skip service")
        fun create_restroom_with_empty_body_returns_415_and_skips_service() =
            runTest {
                withRoutingApp { client ->
                    val response = client.testPost("/api/v1/restrooms", "")

                    assertEquals(HttpStatusCode.UnsupportedMediaType, response.status)
                }

                coVerify(exactly = 0) { restroomService.createRestroom(any()) }
            }
    }
}
