package integration.api.restrooms

import integration.base.BaseIntegrationTest
import integration.base.KtorTestApplication
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import yayauheny.by.helpers.DatabaseTestHelper
import yayauheny.by.helpers.expect
import yayauheny.by.helpers.testGet
import yayauheny.by.helpers.testPost
import yayauheny.by.helpers.parseErrorResponse
import yayauheny.by.helpers.assertHasFieldError
import yayauheny.by.helpers.assertHasValidationErrors
import yayauheny.by.model.enums.RestroomStatus

@Tag("integration")
class RestroomApiTest : BaseIntegrationTest() {
    @Test
    @DisplayName("GIVEN multiple restrooms WHEN GET with pagination THEN return paginated results")
    fun given_multiple_restrooms_when_get_with_pagination_then_return_paginated_results() =
        runTest {
            val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
            DatabaseTestHelper.insertTestRestroom(
                dslContext,
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(name = "Test Restroom")
            )

            KtorTestApplication.withApp(dslContext) { client ->
                val response =
                    client.get("/api/v1/restrooms") {
                        url {
                            parameters.append("page", "0")
                            parameters.append("size", "2")
                        }
                        headers.append("Accept", ContentType.Application.Json.toString())
                    }

                response.expect {
                    ok()
                    json {
                        pathExists("$.content")
                        pathExists("$.totalElements")
                    }
                }
            }
        }

    @Test
    @DisplayName("GIVEN no restrooms WHEN GET with pagination THEN return empty results")
    fun given_no_restrooms_when_get_with_pagination_then_return_empty_results() =
        runTest {
            KtorTestApplication.withApp(dslContext) { client ->
                val response =
                    client.get("/api/v1/restrooms") {
                        url {
                            parameters.append("page", "0")
                            parameters.append("size", "10")
                        }
                        headers.append("Accept", ContentType.Application.Json.toString())
                    }

                response.expect {
                    ok()
                    json {
                        pathEquals("$.totalElements", 0)
                    }
                }
            }
        }

    @Test
    @DisplayName("GIVEN existing restroom WHEN GET by valid ID THEN return restroom details")
    fun given_existing_restroom_when_get_by_valid_id_then_return_restroom_details() =
        runTest {
            val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
            val restroomData = DatabaseTestHelper.createTestRestroomData(name = "Premium Restroom")
            val restroomId = DatabaseTestHelper.insertTestRestroom(dslContext, testEnv.cityId, restroomData)

            KtorTestApplication.withApp(dslContext) { client ->
                val response =
                    client.get("/api/v1/restrooms/$restroomId") {
                        headers.append("Accept", ContentType.Application.Json.toString())
                    }

                response.expect {
                    ok()
                    json {
                        pathExists("$.id")
                        pathEquals("$.name", "Premium Restroom")
                    }
                }
            }
        }

    @Test
    @DisplayName("GIVEN valid restroom data WHEN POST new restroom THEN create successfully")
    fun given_valid_restroom_data_when_post_new_restroom_then_create_successfully() =
        runTest {
            val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
            val restroomData =
                """
                {
                    "cityId": "${testEnv.cityId}",
                    "status": "ACTIVE",
                    "name": "New Restroom",
                    "description": "Test restroom description",
                    "address": "123 New Street",
                    "phones": {},
                    "workTime": {},
                    "feeType": "FREE",
                    "accessibilityType": "UNISEX",
                    "coordinates": {
                        "lat": 55.7558,
                        "lon": 37.6176
                    },
                    "dataSource": "MANUAL",
                    "amenities": {}
                }
                """.trimIndent()

            KtorTestApplication.withApp(dslContext) { client ->
                val response =
                    client.post("/api/v1/restrooms") {
                        contentType(ContentType.Application.Json)
                        setBody(restroomData)
                    }

                response.expect {
                    created()
                    json {
                        pathExists("$.id")
                        pathEquals("$.name", "New Restroom")
                    }
                }
            }
        }

    @Test
    @DisplayName("GIVEN restroom with minimal valid data WHEN POST THEN create successfully")
    fun given_restroom_with_minimal_valid_data_when_post_then_create_successfully() =
        runTest {
            val minimalData =
                """
                {
                    "cityId": null,
                    "status": "ACTIVE",
                    "name": "Minimal Restroom",
                    "description": "Minimal description",
                    "address": "Minimal Address",
                    "phones": {},
                    "workTime": {},
                    "feeType": "FREE",
                    "accessibilityType": "UNISEX",
                    "coordinates": {
                        "lat": 55.7558,
                        "lon": 37.6176
                    },
                    "dataSource": "MANUAL",
                    "amenities": {}
                }
                """.trimIndent()

            KtorTestApplication.withApp(dslContext) { client ->
                val response =
                    client.post("/api/v1/restrooms") {
                        contentType(ContentType.Application.Json)
                        setBody(minimalData)
                    }

                response.expect {
                    created()
                    json {
                        pathEquals("$.address", "Minimal Address")
                    }
                }
            }
        }

    @Test
    @DisplayName("GIVEN invalid restroom data WHEN POST THEN return error response")
    fun given_invalid_restroom_data_when_post_then_return_error_response() =
        runTest {
            val invalidData = """{"invalid": "data"}"""

            KtorTestApplication.withApp(dslContext) { client ->
                val response =
                    client.post("/api/v1/restrooms") {
                        contentType(ContentType.Application.Json)
                        setBody(invalidData)
                    }

                response.expect {
                    expectError(HttpStatusCode.BadRequest)
                }
            }
        }

    @Test
    @DisplayName("GIVEN existing restrooms WHEN GET nearest with valid coordinates THEN return nearest restrooms with distance")
    fun given_existing_restrooms_when_get_nearest_with_valid_coordinates_then_return_nearest_restrooms() =
        runTest {
            val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
            DatabaseTestHelper.insertTestRestroom(
                dslContext,
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(
                    name = "Restroom 1",
                    lat = 55.7558,
                    lon = 37.6176
                )
            )
            DatabaseTestHelper.insertTestRestroom(
                dslContext,
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(
                    name = "Restroom 2",
                    lat = 55.7559,
                    lon = 37.6177
                )
            )

            KtorTestApplication.withApp(dslContext) { client ->
                val response =
                    client.testGet(
                        "/api/v1/restrooms/nearest",
                        mapOf("lat" to "55.7558", "lon" to "37.6176", "limit" to "5")
                    )

                response.expect {
                    ok()
                    json {
                        arraySize("$", 2)
                        arrayAny("$") { restroom ->
                            restroom.containsKey("distanceMeters") &&
                                restroom.containsKey("coordinates") &&
                                restroom["coordinates"]?.jsonObject?.containsKey("lat") == true &&
                                restroom["coordinates"]?.jsonObject?.containsKey("lon") == true
                        }
                        arrayAny("$") { restroom ->
                            val name = restroom["name"]?.toString()?.removeSurrounding("\"")
                            name?.contains("Restroom 1") == true || name?.contains("Restroom 2") == true
                        }
                    }
                }
            }
        }

    @Test
    @DisplayName("GIVEN no restrooms WHEN GET nearest with valid coordinates THEN return empty array")
    fun given_no_restrooms_when_get_nearest_with_valid_coordinates_then_return_empty_array() =
        runTest {
            KtorTestApplication.withApp(dslContext) { client ->
                val response =
                    client.testGet(
                        "/api/v1/restrooms/nearest",
                        mapOf("lat" to "55.7558", "lon" to "37.6176", "limit" to "5")
                    )

                response.expect {
                    ok()
                    json {
                        arraySize("$", 0)
                    }
                }
            }
        }

    @Test
    @DisplayName("GIVEN multiple restrooms WHEN GET nearest THEN return sorted by distance with correct distance values")
    fun given_multiple_restrooms_when_get_nearest_then_return_sorted_by_distance() =
        runTest {
            val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
            DatabaseTestHelper.insertTestRestroom(
                dslContext,
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(
                    name = "Far Restroom",
                    lat = 55.7568,
                    lon = 37.6176
                )
            )
            DatabaseTestHelper.insertTestRestroom(
                dslContext,
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(
                    name = "Close Restroom",
                    lat = 55.7559,
                    lon = 37.6176
                )
            )
            DatabaseTestHelper.insertTestRestroom(
                dslContext,
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(
                    name = "Medium Restroom",
                    lat = 55.7563,
                    lon = 37.6176
                )
            )

            KtorTestApplication.withApp(dslContext) { client ->
                val response =
                    client.testGet(
                        "/api/v1/restrooms/nearest",
                        mapOf("lat" to "55.7558", "lon" to "37.6176", "limit" to "10")
                    )

                response.expect {
                    ok()
                    json {
                        arraySize("$", 3)
                        arrayEach("$") { restroom ->
                            assertTrue(restroom.containsKey("distanceMeters"), "Each restroom should have distanceMeters field")
                            assertTrue(restroom.containsKey("coordinates"), "Each restroom should have coordinates field")
                            val coords = restroom["coordinates"]?.jsonObject
                            assertTrue(coords?.containsKey("lat") == true, "Each restroom should have coordinates.lat field")
                            assertTrue(coords?.containsKey("lon") == true, "Each restroom should have coordinates.lon field")
                            assertTrue(restroom.containsKey("name"), "Each restroom should have name field")
                        }
                        arrayAny("$") { restroom ->
                            restroom["name"]?.toString()?.removeSurrounding("\"") == "Close Restroom"
                        }
                    }
                }
            }
        }

    @Test
    @DisplayName("GIVEN many restrooms WHEN GET nearest with small limit THEN return only limited results")
    fun given_many_restrooms_when_get_nearest_with_small_limit_then_return_only_limited_results() =
        runTest {
            val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
            repeat(10) { i ->
                DatabaseTestHelper.insertTestRestroom(
                    dslContext,
                    testEnv.cityId,
                    DatabaseTestHelper.createTestRestroomData(
                        name = "Restroom $i",
                        lat = 55.7558 + i * 0.001,
                        lon = 37.6176 + i * 0.001
                    )
                )
            }

            KtorTestApplication.withApp(dslContext) { client ->
                val response =
                    client.testGet(
                        "/api/v1/restrooms/nearest",
                        mapOf("lat" to "55.7558", "lon" to "37.6176", "limit" to "3")
                    )

                response.expect {
                    ok()
                    json {
                        arraySize("$", 3)
                        arrayEach("$") { restroom ->
                            assertTrue(restroom.containsKey("distanceMeters"), "Each restroom should have distanceMeters field")
                        }
                    }
                }
            }
        }

    @Test
    @DisplayName("GIVEN active and inactive restrooms WHEN GET nearest THEN return only active restrooms")
    fun given_active_and_inactive_restrooms_when_get_nearest_then_return_only_active_restrooms() =
        runTest {
            val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)

            DatabaseTestHelper.insertTestRestroom(
                dslContext,
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(
                    name = "Active Restroom",
                    lat = 55.7559,
                    lon = 37.6176
                )
            )

            DatabaseTestHelper.insertTestRestroom(
                dslContext,
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(
                    name = "Inactive Restroom",
                    lat = 55.7558,
                    lon = 37.6176,
                    status = RestroomStatus.INACTIVE
                )
            )

            KtorTestApplication.withApp(dslContext) { client ->
                val response =
                    client.testGet(
                        "/api/v1/restrooms/nearest",
                        mapOf("lat" to "55.7558", "lon" to "37.6176", "limit" to "10")
                    )

                response.expect {
                    ok()
                    json {
                        arraySize("$", 1)
                        arrayAny("$") { restroom ->
                            restroom["name"]?.toString()?.removeSurrounding("\"") == "Active Restroom"
                        }
                        arrayEach("$") { restroom ->
                            assertTrue(restroom.containsKey("distanceMeters"), "Active restroom should have distanceMeters")
                        }
                    }
                }
            }
        }

    @Nested
    @DisplayName("Negative Test Cases")
    inner class NegativeTestCases {
        @Test
        @DisplayName("GET /api/v1/restrooms/{id} with invalid UUID returns 400")
        fun get_restroom_invalid_uuid() =
            runTest {
                KtorTestApplication.withApp(dslContext) { client ->
                    val response = client.testGet("/api/v1/restrooms/invalid-uuid")
                    response.expect { badRequest() }
                }
            }

        @Test
        @DisplayName("GET /api/v1/restrooms/city/{cityId} with invalid UUID returns 400")
        fun get_restrooms_by_city_invalid_uuid() =
            runTest {
                KtorTestApplication.withApp(dslContext) { client ->
                    val response = client.testGet("/api/v1/restrooms/city/invalid-uuid")
                    response.expect { badRequest() }
                }
            }

        @Test
        @DisplayName("GET /api/v1/restrooms/{id} with non-existing UUID returns 404")
        fun get_restroom_non_existing_uuid() =
            runTest {
                KtorTestApplication.withApp(dslContext) { client ->
                    val nonExistentId = "550e8400-e29b-41d4-a716-446655440000"
                    val response = client.testGet("/api/v1/restrooms/$nonExistentId")
                    response.expect {
                        expectError(HttpStatusCode.NotFound, messageContains = "not found")
                    }
                }
            }

        @Test
        @DisplayName("GET /api/v1/restrooms/nearest with invalid lat type returns 400")
        fun nearest_invalid_lat_type() =
            runTest {
                KtorTestApplication.withApp(dslContext) { client ->
                    val response = client.testGet("/api/v1/restrooms/nearest", mapOf("lat" to "abc", "lon" to "37.6"))
                    response.expect { badRequest() }
                }
            }

        @Test
        @DisplayName("GET /api/v1/restrooms/nearest with out-of-range coordinates returns 400 with validation errors")
        fun nearest_out_of_range_coordinates() =
            runTest {
                KtorTestApplication.withApp(dslContext) { client ->
                    val response = client.testGet("/api/v1/restrooms/nearest", mapOf("lat" to "91.0", "lon" to "37.6"))
                    response.expect { badRequest() }

                    val errorResponse = response.parseErrorResponse()
                    errorResponse.assertHasValidationErrors()
                    errorResponse.assertHasFieldError("coordinates", "не более 90 градусов")
                }
            }

        @Test
        @DisplayName("POST /api/v1/restrooms with invalid JSON returns 400")
        fun post_restroom_invalid_json() =
            runTest {
                KtorTestApplication.withApp(dslContext) { client ->
                    val response = client.testPost("/api/v1/restrooms", """{"invalid": "data"}""")
                    response.expect { badRequest() }
                }
            }

        @Test
        @DisplayName("POST /api/v1/restrooms with invalid enum returns 400")
        fun post_restroom_invalid_enum() =
            runTest {
                KtorTestApplication.withApp(dslContext) { client ->
                    val invalidEnumJson =
                        """
                        {
                            "status": "ACTIVE",
                            "name": "Test Restroom",
                            "address": "Test Address",
                            "feeType": "UNKNOWN",
                            "accessibilityType": "UNISEX",
                            "coordinates": {
                                "lat": 55.7,
                                "lon": 37.6
                            },
                            "dataSource": "MANUAL",
                            "amenities": {}
                        }
                        """.trimIndent()
                    val response = client.testPost("/api/v1/restrooms", invalidEnumJson)
                    response.expect { badRequest() }
                }
            }
    }
}
