package integration.api.restrooms

import yayauheny.by.model.enums.RestroomStatus
import integration.base.BaseIntegrationTest
import integration.base.SpringTestApplication
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.toJacksonNode
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import yayauheny.by.helpers.DatabaseTestHelper
import yayauheny.by.helpers.assertHasValidationErrors
import yayauheny.by.helpers.assertJsonContentType
import yayauheny.by.helpers.assertStatusAndJsonContent
import yayauheny.by.helpers.loadImportResourceItems
import yayauheny.by.helpers.parseErrorResponse
import yayauheny.by.helpers.testGet
import yayauheny.by.importing.mapper.RestroomImportMapper
import yayauheny.by.importing.model.ImportAdapterContext
import yayauheny.by.importing.provider.ImportSourceAdapter
import yayauheny.by.importing.provider.twogis.TwoGisImportAdapter
import yayauheny.by.importing.provider.yandex.YandexImportAdapter
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.service.RestroomService
import java.util.UUID

@Tag("integration")
class RestroomApiTest : BaseIntegrationTest() {
    @Autowired
    private lateinit var restroomService: RestroomService

    @Test
    @DisplayName("GIVEN restrooms with different origin ids WHEN GET restrooms with origin filter THEN return only matching restroom")
    fun given_restrooms_with_different_origin_ids_when_get_restrooms_with_origin_filter_then_return_only_matching_restroom() =
        runTest {
            val testEnv = DatabaseTestHelper.createTestEnvironment()
            DatabaseTestHelper.insertTestRestroom(
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(
                    name = "Matched restroom",
                    originProvider = ImportProvider.TWO_GIS,
                    originId = "origin-match-1"
                )
            )
            DatabaseTestHelper.insertTestRestroom(
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(
                    name = "Different restroom",
                    originProvider = ImportProvider.TWO_GIS,
                    originId = "origin-other-2"
                )
            )

            SpringTestApplication.withApp { client ->
                val response =
                    client.testGet(
                        "/api/v1/restrooms",
                        mapOf(
                            "originProvider" to ImportProvider.TWO_GIS.name,
                            "originId" to "origin-match-1"
                        )
                    )

                response.assertStatusAndJsonContent(HttpStatus.OK)
                val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                val content = json["content"]?.jsonArray ?: error("Missing content array")
                assertEquals(1, content.size, "Should return only one restroom for exact origin filter")
                assertEquals("Matched restroom", content.single().jsonObject["name"]?.jsonPrimitive?.content)
            }
        }

    @Test
    @DisplayName("GIVEN existing restrooms WHEN GET nearest with valid coordinates THEN return nearest restrooms with distance")
    fun given_existing_restrooms_when_get_nearest_with_valid_coordinates_then_return_nearest_restrooms() =
        runTest {
            val testEnv = DatabaseTestHelper.createTestEnvironment()
            DatabaseTestHelper.insertTestRestroom(
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(
                    name = "Restroom 1",
                    lat = 55.7558,
                    lon = 37.6176
                )
            )
            DatabaseTestHelper.insertTestRestroom(
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(
                    name = "Restroom 2",
                    lat = 55.7559,
                    lon = 37.6177
                )
            )

            SpringTestApplication.withApp { client ->
                val response =
                    client.testGet(
                        "/api/v1/restrooms/nearest",
                        mapOf("lat" to "55.7558", "lon" to "37.6176", "limit" to "5")
                    )

                response.assertStatusAndJsonContent(HttpStatus.OK)
                val jsonArray = Json.parseToJsonElement(response.bodyAsText()).jsonArray
                assertTrue(jsonArray.isNotEmpty(), "Response should contain at least one restroom")
                val firstRestroom = jsonArray.first().jsonObject
                assertTrue(firstRestroom.containsKey("distanceMeters"), "Response should contain distanceMeters field")
                assertTrue(firstRestroom.containsKey("restroomCoordinates"), "Response should contain restroomCoordinates field")
                val coordinates = firstRestroom["restroomCoordinates"]?.jsonObject
                assertTrue(coordinates != null, "Coordinates should be an object")
                assertTrue(coordinates!!.containsKey("lat"), "Coordinates should have lat field")
                assertTrue(coordinates.containsKey("lon"), "Coordinates should have lon field")
                val names = jsonArray.mapNotNull { it.jsonObject["displayName"]?.jsonPrimitive?.content }
                assertTrue(names.any { it.contains("Restroom 1") || it.contains("Restroom 2") }, "Response should contain test restrooms")
            }
        }

    @Test
    @DisplayName("GIVEN no restrooms WHEN GET nearest with valid coordinates THEN return empty array")
    fun given_no_restrooms_when_get_nearest_with_valid_coordinates_then_return_empty_array() =
        runTest {
            SpringTestApplication.withApp { client ->
                val response =
                    client.testGet(
                        "/api/v1/restrooms/nearest",
                        mapOf("lat" to "55.7558", "lon" to "37.6176", "limit" to "5")
                    )

                assertEquals(HttpStatus.OK, response.status)
                response.assertJsonContentType()
                val body = response.bodyAsText()
                assertEquals(body, "[]", "Response should be an empty array when no restrooms exist")
            }
        }

    @Test
    @DisplayName("GIVEN multiple restrooms WHEN GET nearest THEN return sorted by distance with correct distance values")
    fun given_multiple_restrooms_when_get_nearest_then_return_sorted_by_distance() =
        runTest {
            val testEnv = DatabaseTestHelper.createTestEnvironment()
            DatabaseTestHelper.insertTestRestroom(
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(
                    name = "Far Restroom",
                    lat = 55.7568,
                    lon = 37.6176
                )
            )
            DatabaseTestHelper.insertTestRestroom(
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(
                    name = "Close Restroom",
                    lat = 55.7559,
                    lon = 37.6176
                )
            )
            DatabaseTestHelper.insertTestRestroom(
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(
                    name = "Medium Restroom",
                    lat = 55.7563,
                    lon = 37.6176
                )
            )

            SpringTestApplication.withApp { client ->
                val response =
                    client.testGet(
                        "/api/v1/restrooms/nearest",
                        mapOf("lat" to "55.7558", "lon" to "37.6176", "limit" to "10")
                    )

                assertEquals(HttpStatus.OK, response.status)
                response.assertJsonContentType()
                val body = response.bodyAsText()

                val jsonArray = Json.decodeFromString<List<kotlinx.serialization.json.JsonObject>>(body)

                assertTrue(jsonArray.isNotEmpty(), "Should return at least one restroom")

                jsonArray.forEach { restroom ->
                    assertTrue(restroom.containsKey("distanceMeters"), "Each restroom should have distanceMeters field")
                    assertTrue(restroom.containsKey("restroomCoordinates"), "Each restroom should have restroomCoordinates field")
                    val coordinates = restroom["restroomCoordinates"]?.jsonObject
                    assertTrue(coordinates != null, "Coordinates should be an object")
                    assertTrue(coordinates!!.containsKey("lat"), "Coordinates should have lat field")
                    assertTrue(coordinates.containsKey("lon"), "Coordinates should have lon field")
                    assertTrue(restroom.containsKey("displayName"), "Each restroom should have displayName field")

                    val distance = restroom["distanceMeters"]?.jsonPrimitive?.doubleOrNull
                    assertTrue(distance != null && distance >= 0, "Distance should be a valid non-negative number")
                }

                val distances = jsonArray.mapNotNull { it["distanceMeters"]?.jsonPrimitive?.doubleOrNull }
                val sortedDistances = distances.sorted()
                distances.forEachIndexed { index, distance ->
                    if (index > 0) {
                        assertTrue(distance >= sortedDistances[index - 1] - 0.001, "Restrooms should be sorted by distance (nearest first)")
                    }
                }

                val firstRestroom = jsonArray.first()
                val firstName = firstRestroom["displayName"]?.jsonPrimitive?.content
                assertEquals("Close Restroom", firstName, "The closest restroom should be returned first")

                assertTrue(jsonArray.size <= 10, "Response size should not exceed limit")
                assertEquals(3, jsonArray.size, "Should return exactly 3 restrooms")

                val closeRestroom =
                    jsonArray.find {
                        it["displayName"]?.jsonPrimitive?.content == "Close Restroom"
                    }
                assertNotNull(closeRestroom, "Close Restroom should be found in results")
                val closeDistance = closeRestroom["distanceMeters"]?.jsonPrimitive?.doubleOrNull
                assertNotNull(closeDistance, "Close Restroom should have distanceMeters")
                assertTrue(closeDistance in 5.0..20.0, "Close Restroom distance should be approximately 11m (±15m tolerance)")

                val mediumRestroom =
                    jsonArray.find {
                        it["displayName"]?.jsonPrimitive?.content == "Medium Restroom"
                    }
                assertNotNull(mediumRestroom, "Medium Restroom should be found in results")
                val mediumDistance = mediumRestroom["distanceMeters"]?.jsonPrimitive?.doubleOrNull
                assertNotNull(mediumDistance, "Medium Restroom should have distanceMeters")
                assertTrue(mediumDistance in 40.0..70.0, "Medium Restroom distance should be approximately 55m (±15m tolerance)")

                val farRestroom =
                    jsonArray.find {
                        it["displayName"]?.jsonPrimitive?.content == "Far Restroom"
                    }
                assertNotNull(farRestroom, "Far Restroom should be found in results")
                val farDistance = farRestroom["distanceMeters"]?.jsonPrimitive?.doubleOrNull
                assertNotNull(farDistance, "Far Restroom should have distanceMeters")
                assertTrue(farDistance in 95.0..125.0, "Far Restroom distance should be approximately 111m (±15m tolerance)")

                assertTrue(closeDistance < mediumDistance, "Close Restroom should be closer than Medium Restroom")
                assertTrue(mediumDistance < farDistance, "Medium Restroom should be closer than Far Restroom")
            }
        }

    @Test
    @DisplayName("GIVEN many restrooms WHEN GET nearest with small limit THEN return only limited results")
    fun given_many_restrooms_when_get_nearest_with_small_limit_then_return_only_limited_results() =
        runTest {
            val testEnv = DatabaseTestHelper.createTestEnvironment()
            repeat(10) { i ->
                DatabaseTestHelper.insertTestRestroom(
                    testEnv.cityId,
                    DatabaseTestHelper.createTestRestroomData(
                        name = "Restroom $i",
                        lat = 55.7558 + i * 0.001,
                        lon = 37.6176 + i * 0.001
                    )
                )
            }

            SpringTestApplication.withApp { client ->
                val response =
                    client.testGet(
                        "/api/v1/restrooms/nearest",
                        mapOf("lat" to "55.7558", "lon" to "37.6176", "limit" to "3")
                    )

                assertEquals(HttpStatus.OK, response.status)
                response.assertJsonContentType()
                val body = response.bodyAsText()

                val jsonArray = Json.decodeFromString<List<kotlinx.serialization.json.JsonObject>>(body)

                assertEquals(3, jsonArray.size, "Should return exactly 3 restrooms when limit is 3")

                jsonArray.forEach { restroom ->
                    assertTrue(restroom.containsKey("distanceMeters"), "Each restroom should have distanceMeters field")
                    val distance = restroom["distanceMeters"]?.jsonPrimitive?.doubleOrNull
                    assertTrue(distance != null && distance >= 0, "Distance should be a valid non-negative number")
                }

                val distances = jsonArray.mapNotNull { it["distanceMeters"]?.jsonPrimitive?.doubleOrNull }
                val sortedDistances = distances.sorted()
                assertEquals(sortedDistances, distances, "Restrooms should be sorted by distance (nearest first)")
            }
        }

    @Test
    @DisplayName("GIVEN near and far restrooms WHEN GET nearest without distance parameter THEN use default 5km radius")
    fun given_near_and_far_restrooms_when_get_nearest_without_distance_parameter_then_use_default_radius() =
        runTest {
            val testEnv = DatabaseTestHelper.createTestEnvironment()
            DatabaseTestHelper.insertTestRestroom(
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(
                    name = "Near Restroom",
                    lat = 53.9000,
                    lon = 27.5600
                )
            )
            DatabaseTestHelper.insertTestRestroom(
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(
                    name = "Far Restroom",
                    lat = 53.9600,
                    lon = 27.5600
                )
            )

            SpringTestApplication.withApp { client ->
                val response =
                    client.testGet(
                        "/api/v1/restrooms/nearest",
                        mapOf("lat" to "53.9000", "lon" to "27.5600", "limit" to "10")
                    )

                response.assertStatusAndJsonContent(HttpStatus.OK)
                val jsonArray = Json.parseToJsonElement(response.bodyAsText()).jsonArray

                assertEquals(1, jsonArray.size)
                assertEquals("Near Restroom", jsonArray.single().jsonObject["displayName"]?.jsonPrimitive?.content)
            }
        }

    @Test
    @DisplayName("GIVEN near and far restrooms WHEN GET nearest with explicit larger radius THEN include both")
    fun given_near_and_far_restrooms_when_get_nearest_with_explicit_larger_radius_then_include_both() =
        runTest {
            val testEnv = DatabaseTestHelper.createTestEnvironment()
            DatabaseTestHelper.insertTestRestroom(
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(
                    name = "Near Restroom",
                    lat = 53.9000,
                    lon = 27.5600
                )
            )
            DatabaseTestHelper.insertTestRestroom(
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(
                    name = "Far Restroom",
                    lat = 53.9600,
                    lon = 27.5600
                )
            )

            SpringTestApplication.withApp { client ->
                val response =
                    client.testGet(
                        "/api/v1/restrooms/nearest",
                        mapOf("lat" to "53.9000", "lon" to "27.5600", "limit" to "10", "distanceMeters" to "10000")
                    )

                response.assertStatusAndJsonContent(HttpStatus.OK)
                val jsonArray = Json.parseToJsonElement(response.bodyAsText()).jsonArray

                assertEquals(2, jsonArray.size)
                assertEquals("Near Restroom", jsonArray[0].jsonObject["displayName"]?.jsonPrimitive?.content)
                assertEquals("Far Restroom", jsonArray[1].jsonObject["displayName"]?.jsonPrimitive?.content)
            }
        }

    @Test
    @DisplayName("GIVEN active and inactive restrooms WHEN GET nearest THEN return only active restrooms")
    fun given_active_and_inactive_restrooms_when_get_nearest_then_return_only_active_restrooms() =
        runTest {
            val testEnv = DatabaseTestHelper.createTestEnvironment()

            DatabaseTestHelper.insertTestRestroom(
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(
                    name = "Active Restroom",
                    lat = 55.7559,
                    lon = 37.6176
                )
            )

            DatabaseTestHelper.insertTestRestroom(
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(
                    name = "Inactive Restroom",
                    lat = 55.7558,
                    lon = 37.6176,
                    status = RestroomStatus.INACTIVE
                )
            )

            SpringTestApplication.withApp { client ->
                val response =
                    client.testGet(
                        "/api/v1/restrooms/nearest",
                        mapOf("lat" to "55.7558", "lon" to "37.6176", "limit" to "10")
                    )

                assertEquals(HttpStatus.OK, response.status)
                response.assertJsonContentType()
                val body = response.bodyAsText()

                val jsonArray = Json.decodeFromString<List<kotlinx.serialization.json.JsonObject>>(body)

                assertEquals(1, jsonArray.size, "Should return only 1 restroom (the active one)")

                val returnedRestroom = jsonArray.first()
                val name = returnedRestroom["displayName"]?.jsonPrimitive?.content
                assertEquals("Active Restroom", name, "Should return the active restroom, not the inactive one")

                val distanceValue = returnedRestroom["distanceMeters"]?.jsonPrimitive
                assertNotNull(distanceValue, "Active restroom should have distanceMeters")
                val distance = distanceValue.doubleOrNull
                assertNotNull(distance, "Distance should be a valid number")
                assertTrue(distance >= 0.0, "Distance should be non-negative")
            }
        }

    @Test
    @DisplayName("GIVEN real 2GIS import fixtures WHEN GET nearest THEN return imported toilets ordered by actual distance")
    fun given_real_twogis_import_fixtures_when_get_nearest_then_return_imported_toilets_ordered_by_distance() =
        runTest {
            val testEnv = DatabaseTestHelper.createTestEnvironment()
            importFixtureRestroom(
                adapter = TwoGisImportAdapter(),
                resourcePath = "import/2gis/2gis_scraped_places.json",
                idField = "id",
                resourceId = "70000001042444951",
                payloadType = ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON,
                cityId = testEnv.cityId
            )
            importFixtureRestroom(
                adapter = TwoGisImportAdapter(),
                resourcePath = "import/2gis/2gis_scraped_places.json",
                idField = "id",
                resourceId = "70000001057290977",
                payloadType = ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON,
                cityId = testEnv.cityId
            )

            SpringTestApplication.withApp { client ->
                val response =
                    client.testGet(
                        "/api/v1/restrooms/nearest",
                        mapOf("lat" to "53.933332", "lon" to "27.651295", "limit" to "10", "distanceMeters" to "15000")
                    )

                response.assertStatusAndJsonContent(HttpStatus.OK)
                val jsonArray = Json.parseToJsonElement(response.bodyAsText()).jsonArray

                assertEquals(2, jsonArray.size)
                assertEquals(
                    "Dana Mall, торгово-развлекательный комплекс",
                    jsonArray[0].jsonObject["displayName"]?.jsonPrimitive?.content
                )
                assertEquals(
                    "Платный туалет",
                    jsonArray[1].jsonObject["displayName"]?.jsonPrimitive?.content
                )
                assertEquals(53.933332, jsonArray[0].jsonObject["restroomCoordinates"]!!.jsonObject["lat"]!!.jsonPrimitive.content.toDouble())
                assertEquals(27.651295, jsonArray[0].jsonObject["restroomCoordinates"]!!.jsonObject["lon"]!!.jsonPrimitive.content.toDouble())

                val distances = jsonArray.map { it.jsonObject["distanceMeters"]!!.jsonPrimitive.content.toDouble() }
                assertTrue(distances[0] in 0.0..0.01, "Expected the exact queried 2GIS restroom to be effectively at zero distance")
                assertEquals(
                    haversineMeters(53.933332, 27.651295, 53.834559, 27.628516),
                    distances[1],
                    0.01
                )
                assertTrue(distances[0] < distances[1], "Expected nearest results to stay sorted by distance")
            }
        }

    @Test
    @DisplayName("GIVEN real Yandex import fixtures WHEN GET nearest THEN exclude inactive imported toilet and return active match")
    fun given_real_yandex_import_fixtures_when_get_nearest_then_exclude_inactive_imported_toilet_and_return_active_match() =
        runTest {
            val testEnv = DatabaseTestHelper.createTestEnvironment()
            importFixtureRestroom(
                adapter = YandexImportAdapter(),
                resourcePath = "import/yandex/yandex_scraped_places.json",
                idField = "placeId",
                resourceId = "137246000290",
                payloadType = ImportPayloadType.YANDEX_MAPS_SCRAPED_PLACE_JSON,
                cityId = testEnv.cityId
            )
            importFixtureRestroom(
                adapter = YandexImportAdapter(),
                resourcePath = "import/yandex/yandex_scraped_places.json",
                idField = "placeId",
                resourceId = "239919493009",
                payloadType = ImportPayloadType.YANDEX_MAPS_SCRAPED_PLACE_JSON,
                cityId = testEnv.cityId
            )

            SpringTestApplication.withApp { client ->
                val response =
                    client.testGet(
                        "/api/v1/restrooms/nearest",
                        mapOf("lat" to "53.909435", "lon" to "27.573736", "limit" to "10")
                    )

                response.assertStatusAndJsonContent(HttpStatus.OK)
                val jsonArray = Json.parseToJsonElement(response.bodyAsText()).jsonArray

                assertEquals(1, jsonArray.size, "Expected only the active Yandex restroom to be searchable")
                val returned = jsonArray.single().jsonObject
                assertEquals("Туалет", returned["displayName"]?.jsonPrimitive?.content)
                assertEquals(53.890334, returned["restroomCoordinates"]!!.jsonObject["lat"]!!.jsonPrimitive.content.toDouble())
                assertEquals(27.553889, returned["restroomCoordinates"]!!.jsonObject["lon"]!!.jsonPrimitive.content.toDouble())
                assertEquals(
                    haversineMeters(53.909435, 27.573736, 53.890334, 27.553889),
                    returned["distanceMeters"]!!.jsonPrimitive.content.toDouble(),
                    0.01
                )
            }
        }

    @Test
    @DisplayName("GIVEN mixed imported provider fixtures WHEN GET nearest THEN return all providers in actual distance order")
    fun given_mixed_imported_provider_fixtures_when_get_nearest_then_return_all_providers_in_actual_distance_order() =
        runTest {
            val testEnv = DatabaseTestHelper.createTestEnvironment()
            importFixtureRestroom(
                adapter = TwoGisImportAdapter(),
                resourcePath = "import/2gis/2gis_scraped_places.json",
                idField = "id",
                resourceId = "70000001042444951",
                payloadType = ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON,
                cityId = testEnv.cityId
            )
            importFixtureRestroom(
                adapter = YandexImportAdapter(),
                resourcePath = "import/yandex/yandex_scraped_places.json",
                idField = "placeId",
                resourceId = "239919493009",
                payloadType = ImportPayloadType.YANDEX_MAPS_SCRAPED_PLACE_JSON,
                cityId = testEnv.cityId
            )

            SpringTestApplication.withApp { client ->
                val response =
                    client.testGet(
                        "/api/v1/restrooms/nearest",
                        mapOf("lat" to "53.9000", "lon" to "27.5600", "limit" to "10", "distanceMeters" to "10000")
                    )

                response.assertStatusAndJsonContent(HttpStatus.OK)
                val jsonArray = Json.parseToJsonElement(response.bodyAsText()).jsonArray

                assertEquals(2, jsonArray.size)
                val firstDistance = jsonArray[0].jsonObject["distanceMeters"]!!.jsonPrimitive.content.toDouble()
                val secondDistance = jsonArray[1].jsonObject["distanceMeters"]!!.jsonPrimitive.content.toDouble()
                assertEquals("Туалет", jsonArray[0].jsonObject["displayName"]?.jsonPrimitive?.content)
                assertEquals("Dana Mall, торгово-развлекательный комплекс", jsonArray[1].jsonObject["displayName"]?.jsonPrimitive?.content)
                assertEquals(
                    haversineMeters(53.9000, 27.5600, 53.890334, 27.553889),
                    firstDistance,
                    0.01
                )
                assertEquals(
                    haversineMeters(53.9000, 27.5600, 53.933332, 27.651295),
                    secondDistance,
                    0.01
                )
                assertTrue(firstDistance < secondDistance)
            }
        }

    @Nested
    @DisplayName("E2E Negative Test Cases for /nearest endpoint")
    inner class NearestNegativeTestCases {
        @Test
        @DisplayName("GET /api/v1/restrooms/nearest with invalid lat type returns 400")
        fun nearest_invalid_lat_type() =
            runTest {
                SpringTestApplication.withApp { client ->
                    val response = client.testGet("/api/v1/restrooms/nearest", mapOf("lat" to "abc", "lon" to "37.6"))
                    response.assertStatusAndJsonContent(HttpStatus.BAD_REQUEST)
                }
            }

        @Test
        @DisplayName("GET /api/v1/restrooms/nearest with out-of-range coordinates returns 400 with validation errors")
        fun nearest_out_of_range_coordinates() =
            runTest {
                SpringTestApplication.withApp { client ->
                    val response = client.testGet("/api/v1/restrooms/nearest", mapOf("lat" to "91.0", "lon" to "37.6"))
                    response.assertStatusAndJsonContent(HttpStatus.BAD_REQUEST)

                    val errorResponse = response.parseErrorResponse()
                    errorResponse.assertHasValidationErrors()
                    assertEquals(
                        errorResponse.errors?.any {
                            it.field == "lat" && it.message.contains("не более 90 градусов")
                        },
                        true,
                        "Error response should contain field error for 'lat' with message about 90 degrees, but got: ${errorResponse.errors}"
                    )
                }
            }
    }

    private fun importFixtureRestroom(
        adapter: ImportSourceAdapter,
        resourcePath: String,
        idField: String,
        resourceId: String,
        payloadType: ImportPayloadType,
        cityId: UUID
    ) {
        val payload = loadImportResourceItems(resourcePath).firstFixture(idField, resourceId).toJacksonNode()
        val envelope =
            adapter.parseEnvelope(
                payload,
                ImportAdapterContext(
                    payloadType = payloadType,
                    cityId = cityId
                )
            )

        restroomService.createRestroom(RestroomImportMapper.toCreateDto(envelope.command))
    }

    private fun List<JsonObject>.firstFixture(
        idField: String,
        resourceId: String
    ): JsonObject =
        first { item -> item[idField]?.jsonPrimitive?.content == resourceId }

    private fun haversineMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadius = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a =
            kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return earthRadius * c
    }
}
