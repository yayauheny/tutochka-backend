package integration.api.cities

import integration.base.BaseIntegrationTest
import integration.base.KtorTestApplication
import io.ktor.http.HttpStatusCode
import java.util.UUID
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import yayauheny.by.helpers.DatabaseTestHelper
import yayauheny.by.helpers.assertPagination
import yayauheny.by.helpers.createCityJson
import yayauheny.by.helpers.expect
import yayauheny.by.helpers.testGet
import yayauheny.by.helpers.testPost

@Tag("integration")
class CityApiTest : BaseIntegrationTest() {
    @Nested
    @DisplayName("GET /api/v1/cities")
    inner class GetAllCities {
        @Test
        @DisplayName("GIVEN cities endpoint WHEN GET /api/v1/cities THEN return paginated response")
        fun cities_endpoint_returns_paginated_response() =
            runTest {
                KtorTestApplication.withApp(dslContext) { client ->
                    val response = client.testGet("/api/v1/cities")

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
        @DisplayName("GIVEN pagination parameters WHEN GET cities THEN return correct pagination")
        fun cities_with_pagination_parameters() =
            runTest {
                KtorTestApplication.withApp(dslContext) { client ->
                    val response = client.testGet("/api/v1/cities", mapOf("page" to "0", "size" to "10"))

                    response.assertPagination(page = 0, size = 10)
                }
            }

        @Test
        @DisplayName("GIVEN cities exist WHEN GET cities THEN return cities data")
        fun given_cities_exist_when_get_cities_then_return_cities_data() =
            runTest {
                KtorTestApplication.withApp(dslContext) { client ->
                    val response = client.testGet("/api/v1/cities")

                    response.expect {
                        ok()
                        json {
                            pathExists("$.content")
                            pathExists("$.totalElements")
                        }
                    }
                }
            }
    }

    @Nested
    @DisplayName("GET /api/v1/cities/{id}")
    inner class GetCityById {
        @Test
        @DisplayName("GIVEN valid UUID WHEN GET city by ID THEN return valid response")
        fun get_city_by_valid_id() =
            runTest {
                KtorTestApplication.withApp(dslContext) { client ->
                    val testUuid = "550e8400-e29b-41d4-a716-446655440000"
                    val response = client.testGet("/api/v1/cities/$testUuid")
                    assertTrue(response.status == HttpStatusCode.OK || response.status == HttpStatusCode.NotFound)
                    response.expect { contentTypeJson() }
                }
            }

        @Test
        @DisplayName("GIVEN existing city WHEN GET city by ID THEN return city details")
        fun given_existing_city_when_get_city_by_id_then_return_city_details() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)

                KtorTestApplication.withApp(dslContext) { client ->
                    val response = client.testGet("/api/v1/cities/${testEnv.cityId}")

                    response.expect {
                        ok()
                        json {
                            pathExists("$.id")
                            pathExists("$.nameRu")
                            pathExists("$.nameEn")
                        }
                    }
                }
            }
    }

    @Nested
    @DisplayName("GET /api/v1/cities/country/{countryId}")
    inner class GetCitiesByCountry {
        @Test
        @DisplayName("GIVEN valid country ID WHEN GET cities by country THEN return cities")
        fun get_cities_by_valid_country_id() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)

                KtorTestApplication.withApp(dslContext) { client ->
                    val response = client.testGet("/api/v1/cities/country/${testEnv.countryId}")

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
        @DisplayName("GIVEN invalid country ID WHEN GET cities by country THEN return 400")
        fun get_cities_by_invalid_country_id() =
            runTest {
                KtorTestApplication.withApp(dslContext) { client ->
                    val response = client.testGet("/api/v1/cities/country/invalid-uuid")

                    response.expect { badRequest() }
                }
            }
    }

    @Nested
    @DisplayName("GET /api/v1/cities/search")
    inner class SearchCities {
        @Test
        @DisplayName("GIVEN valid name parameter WHEN search cities THEN return results")
        fun search_cities_with_valid_name() =
            runTest {
                KtorTestApplication.withApp(dslContext) { client ->
                    val response = client.testGet("/api/v1/cities/search", mapOf("name" to "Test"))

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
        @DisplayName("GIVEN missing name parameter WHEN search cities THEN return 400")
        fun search_cities_without_name_parameter() =
            runTest {
                KtorTestApplication.withApp(dslContext) { client ->
                    val response = client.testGet("/api/v1/cities/search")

                    response.expect {
                        expectError(HttpStatusCode.BadRequest)
                    }
                }
            }
    }

    @Nested
    @DisplayName("POST /api/v1/cities")
    inner class CreateCity {
        @Test
        @DisplayName("GIVEN valid city data WHEN POST city THEN return created city")
        fun create_city_with_valid_data() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
                val validJson =
                    createCityJson(
                        countryId = testEnv.countryId,
                        nameRu = "Тестовый город",
                        nameEn = "Test City",
                        region = "Test Region"
                    )

                KtorTestApplication.withApp(dslContext) { client ->
                    val response = client.testPost("/api/v1/cities", validJson)

                    response.expect {
                        created()
                        json {
                            pathExists("$.id")
                            pathContains("$.nameRu", "Тестовый город")
                            pathContains("$.nameEn", "Test City")
                        }
                    }
                }
            }

        @Test
        @DisplayName("GIVEN minimal city data WHEN POST city THEN create successfully")
        fun create_city_with_minimal_data() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
                val minimalJson =
                    createCityJson(
                        countryId = testEnv.countryId,
                        nameRu = "Минимальный город",
                        nameEn = "Minimal City"
                    )

                KtorTestApplication.withApp(dslContext) { client ->
                    val response = client.testPost("/api/v1/cities", minimalJson)

                    response.expect {
                        created()
                        json {
                            pathExists("$.id")
                            pathContains("$.nameRu", "Минимальный город")
                        }
                    }
                }
            }

        @Test
        @DisplayName("GIVEN invalid city data WHEN POST city THEN return error")
        fun create_city_with_invalid_data() =
            runTest {
                val invalidJson = """{"invalid": "data"}"""

                KtorTestApplication.withApp(dslContext) { client ->
                    val response = client.testPost("/api/v1/cities", invalidJson)

                    response.expect {
                        expectError(HttpStatusCode.BadRequest)
                    }
                }
            }

        @Test
        @DisplayName("GIVEN missing required fields WHEN POST city THEN return validation error")
        fun create_city_with_missing_required_fields() =
            runTest {
                val incompleteJson = """{"nameRu": "Неполный город"}"""

                KtorTestApplication.withApp(dslContext) { client ->
                    val response = client.testPost("/api/v1/cities", incompleteJson)

                    response.expect {
                        expectError(HttpStatusCode.BadRequest)
                    }
                }
            }
    }

    @Nested
    @DisplayName("Negative Test Cases")
    inner class NegativeTestCases {
        @Test
        @DisplayName("GET /api/v1/cities/{id} with invalid UUID returns 400")
        fun get_city_invalid_uuid() =
            runTest {
                KtorTestApplication.withApp(dslContext) { client ->
                    val response = client.testGet("/api/v1/cities/invalid-uuid")
                    response.expect { badRequest() }
                }
            }

        @Test
        @DisplayName("GET /api/v1/cities/{id} with non-existing UUID returns 404")
        fun get_city_non_existing_uuid() =
            runTest {
                KtorTestApplication.withApp(dslContext) { client ->
                    val nonExistentId = "550e8400-e29b-41d4-a716-446655440000"
                    val response = client.testGet("/api/v1/cities/$nonExistentId")
                    response.expect {
                        expectError(HttpStatusCode.NotFound, messageContains = "not found")
                    }
                }
            }

        @Test
        @DisplayName("GET /api/v1/cities/country/{countryId} with non-existing UUID returns empty results")
        fun get_cities_by_non_existing_country() =
            runTest {
                KtorTestApplication.withApp(dslContext) { client ->
                    val nonExistentCountryId = "550e8400-e29b-41d4-a716-446655440000"
                    val response = client.testGet("/api/v1/cities/country/$nonExistentCountryId")
                    response.expect {
                        ok()
                        json {
                            pathEquals("$.totalElements", 0)
                        }
                    }
                }
            }

        @Test
        @DisplayName("POST /api/v1/cities with invalid countryId UUID returns 400")
        fun create_city_invalid_country_id() =
            runTest {
                KtorTestApplication.withApp(dslContext) { client ->
                    val invalidCountryIdJson =
                        """{"countryId": "invalid-uuid", "nameRu": "Тестовый город", "nameEn": "Test City", "coordinates": {"lat": 55.7558, "lon": 37.6176}}"""
                    val response = client.testPost("/api/v1/cities", invalidCountryIdJson)
                    response.expect { badRequest() }
                }
            }

        @Test
        @DisplayName("POST /api/v1/cities with non-existing countryId returns 400")
        fun create_city_non_existing_country_id() =
            runTest {
                KtorTestApplication.withApp(dslContext) { client ->
                    val nonExistentCountryIdJson =
                        createCityJson(
                            countryId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
                            nameRu = "Тестовый город",
                            nameEn = "Test City"
                        )
                    val response = client.testPost("/api/v1/cities", nonExistentCountryIdJson)
                    response.expect { badRequest() }
                }
            }

        @Test
        @DisplayName("POST /api/v1/cities with invalid lat type returns 400")
        fun create_city_invalid_lat_type() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
                KtorTestApplication.withApp(dslContext) { client ->
                    val invalidLatJson =
                        """
                        {
                            "countryId": "${testEnv.countryId}",
                            "nameRu": "Тестовый город",
                            "nameEn": "Test City",
                            "coordinates": {
                                "lat": "north",
                                "lon": 37.6176
                            }
                        }
                        """.trimIndent()
                    val response = client.testPost("/api/v1/cities", invalidLatJson)
                    response.expect { badRequest() }
                }
            }
    }
}
