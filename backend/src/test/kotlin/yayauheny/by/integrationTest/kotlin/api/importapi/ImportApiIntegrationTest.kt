package yayauheny.by.integrationTest.kotlin.api.importapi

import integration.base.BaseIntegrationTest
import integration.base.KtorTestApplication
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import yayauheny.by.helpers.DatabaseTestHelper
import yayauheny.by.helpers.assertStatusAndJsonContent
import yayauheny.by.helpers.loadImportResourceItems
import yayauheny.by.helpers.testPost
import yayauheny.by.tables.references.BUILDINGS
import yayauheny.by.tables.references.RESTROOMS
import yayauheny.by.tables.references.RESTROOM_IMPORTS
import yayauheny.by.util.HEADER_IMPORT_CITY_ID
import yayauheny.by.util.toJsonObject
import yayauheny.by.util.HEADER_IMPORT_PAYLOAD_TYPE
import yayauheny.by.util.HEADER_IMPORT_PROVIDER

@Tag("integration")
class ImportApiIntegrationTest : BaseIntegrationTest() {
    private val sampleRestrooms: List<JsonObject> by lazy {
        val items =
            javaClass.classLoader
                .getResourceAsStream("import/restrooms_2gis_sample.json")
                ?.reader(Charsets.UTF_8)
                ?.use { reader ->
                    val array = Json.parseToJsonElement(reader.readText()).jsonArray
                    array.mapNotNull { if (it is JsonObject) it else null }
                }
                ?: emptyList()
        require(items.isNotEmpty()) { "Test resource import/restrooms_2gis_sample.json not found or empty" }
        items
    }

    private val mixedCityTwoGisRestrooms: List<JsonObject> by lazy {
        loadImportResourceItems("import/2gis_scraped_places.json")
    }

    @Test
    @DisplayName("GIVEN valid 2GIS payload and existing city WHEN POST /import THEN restroom and import record created")
    fun given_valid_payload_when_import_single_then_creates_restroom_and_import_record() =
        runTest {
            val env = DatabaseTestHelper.createTestEnvironment(dslContext)
            val payload = sampleRestrooms.first()
            val body = buildImportBody(listOf(payload))
            val headers = importHeaders(env.cityId)

            KtorTestApplication.withApp(dslContext) { client ->
                val response = client.testPost("/api/v1/import", body, headers)

                response.assertStatusAndJsonContent(HttpStatusCode.Created)
                val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                val restroomId = json["restroomId"]?.jsonPrimitive?.content?.let { UUID.fromString(it) }
                val importId = json["importId"]?.jsonPrimitive?.content?.let { UUID.fromString(it) }
                assertNotNull(restroomId)
                assertNotNull(importId)

                val importRecord =
                    dslContext
                        .selectFrom(RESTROOM_IMPORTS)
                        .where(RESTROOM_IMPORTS.ID.eq(importId!!))
                        .fetchOne()
                assertNotNull(importRecord)
                assertEquals("SUCCESS", importRecord!!.status)
                assertEquals(1, importRecord.attempts)
                assertEquals(restroomId, importRecord.restroomId)

                val restroom =
                    dslContext
                        .select(RESTROOMS.ID, RESTROOMS.ORIGIN_ID, RESTROOMS.ORIGIN_PROVIDER, RESTROOMS.NAME)
                        .from(RESTROOMS)
                        .where(RESTROOMS.ID.eq(restroomId!!))
                        .fetchOne()
                assertNotNull(restroom)
                assertEquals(payload["id"]!!.jsonPrimitive.content, restroom!!.get(RESTROOMS.ORIGIN_ID))
                assertEquals("TWO_GIS", restroom.get(RESTROOMS.ORIGIN_PROVIDER))
                assertEquals(payload["title"]!!.jsonPrimitive.content, restroom.get(RESTROOMS.NAME))
            }
        }

    @Test
    @DisplayName("GIVEN identical POST /import requests WHEN repeated THEN attempts increment once per request")
    fun given_repeated_single_import_when_posted_twice_then_attempts_increment() =
        runTest {
            val env = DatabaseTestHelper.createTestEnvironment(dslContext)
            val payload = sampleRestrooms.first()
            val body = buildImportBody(listOf(payload))
            val headers = importHeaders(env.cityId)

            KtorTestApplication.withApp(dslContext) { client ->
                val firstResponse = client.testPost("/api/v1/import", body, headers)
                firstResponse.assertStatusAndJsonContent(HttpStatusCode.Created)

                val secondResponse = client.testPost("/api/v1/import", body, headers)
                secondResponse.assertStatusAndJsonContent(HttpStatusCode.Created)

                val importRow =
                    dslContext
                        .selectFrom(RESTROOM_IMPORTS)
                        .where(RESTROOM_IMPORTS.PROVIDER.eq("TWO_GIS"))
                        .and(RESTROOM_IMPORTS.ENTITY_TYPE.eq("place"))
                        .and(RESTROOM_IMPORTS.EXTERNAL_ID.eq(payload["id"]!!.jsonPrimitive.content))
                        .fetchOne()

                assertNotNull(importRow)
                assertEquals(2, importRow!!.attempts)

                val rowCount =
                    dslContext
                        .selectCount()
                        .from(RESTROOM_IMPORTS)
                        .where(RESTROOM_IMPORTS.PROVIDER.eq("TWO_GIS"))
                        .and(RESTROOM_IMPORTS.ENTITY_TYPE.eq("place"))
                        .and(RESTROOM_IMPORTS.EXTERNAL_ID.eq(payload["id"]!!.jsonPrimitive.content))
                        .fetchOne()
                        ?.value1() ?: 0
                assertEquals(1, rowCount)
            }
        }

    @Test
    @DisplayName("GIVEN multiple items WHEN POST /import THEN returns 400")
    fun given_multiple_items_when_import_then_returns_400() =
        runTest {
            val env = DatabaseTestHelper.createTestEnvironment(dslContext)
            val body = buildImportBody(sampleRestrooms.take(2))
            val headers = importHeaders(env.cityId)

            KtorTestApplication.withApp(dslContext) { client ->
                val response = client.testPost("/api/v1/import", body, headers)

                response.assertStatusAndJsonContent(HttpStatusCode.BadRequest)
                val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                assertTrue(
                    json["message"]?.jsonPrimitive?.content?.contains("Expected exactly one item") == true
                )
            }
        }

    @Test
    @DisplayName("GIVEN batch of 2 items WHEN POST /import/batch THEN both restrooms and 2 import records created")
    fun given_batch_payload_when_import_batch_then_creates_restrooms_and_import_record() =
        runTest {
            val env = DatabaseTestHelper.createTestEnvironment(dslContext)
            val items = sampleRestrooms.take(2)
            val body = buildImportBody(items)
            val headers = importHeaders(env.cityId)

            KtorTestApplication.withApp(dslContext) { client ->
                val response = client.testPost("/api/v1/import/batch", body, headers)

                response.assertStatusAndJsonContent(HttpStatusCode.Created)
                val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                assertEquals(2, json["totalProcessed"]?.jsonPrimitive?.content?.toInt())
                assertEquals(2, json["successful"]?.jsonPrimitive?.content?.toInt())
                assertEquals(0, json["failed"]?.jsonPrimitive?.content?.toInt())
                assertEquals(2, json["results"]?.jsonArray?.size)
                val importId = json["importId"]?.jsonPrimitive?.content?.let { UUID.fromString(it) }
                assertNotNull(importId)

                val successImportCount =
                    dslContext
                        .selectCount()
                        .from(RESTROOM_IMPORTS)
                        .where(RESTROOM_IMPORTS.STATUS.eq("SUCCESS"))
                        .and(RESTROOM_IMPORTS.CITY_ID.eq(env.cityId))
                        .fetchOne()
                        ?.value1() ?: 0
                assertEquals(2, successImportCount)

                val importRecords =
                    dslContext
                        .selectFrom(RESTROOM_IMPORTS)
                        .where(RESTROOM_IMPORTS.STATUS.eq("SUCCESS"))
                        .and(RESTROOM_IMPORTS.CITY_ID.eq(env.cityId))
                        .fetch()

                assertEquals(2, importRecords.size)
                val restroomIds = importRecords.map { it[RESTROOM_IMPORTS.RESTROOM_ID] }.toSet()
                assertEquals(2, restroomIds.size)

                importRecords.forEach { record ->
                    val payloadObj = record[RESTROOM_IMPORTS.RAW_PAYLOAD].toJsonObject()!!
                    assertTrue("items" !in payloadObj, "raw_payload must be single item, not {\"items\": [...]}")
                }

                val restroomCount =
                    dslContext
                        .selectCount()
                        .from(RESTROOMS)
                        .where(RESTROOMS.ORIGIN_PROVIDER.eq("TWO_GIS"))
                        .fetchOne()
                        ?.value1() ?: 0
                assertEquals(2, restroomCount)
            }
        }

    @Test
    @DisplayName("GIVEN mixed-city 2GIS batch without city header WHEN POST /import/batch THEN city is resolved per source group")
    fun given_mixed_city_batch_without_city_header_when_import_batch_then_resolves_city_per_item() =
        runTest {
            val countryId =
                DatabaseTestHelper.insertTestCountry(
                    dslContext,
                    DatabaseTestHelper.createTestCountryData(
                        nameRu = "Беларусь",
                        nameEn = "Belarus",
                        code = "BY"
                    )
                )
            val minskId =
                DatabaseTestHelper.insertTestCity(
                    dslContext,
                    countryId,
                    DatabaseTestHelper.createTestCityData(
                        nameRu = "Минск",
                        nameEn = "Minsk",
                        lat = 53.9,
                        lon = 27.5667
                    )
                )
            val fanipolId =
                DatabaseTestHelper.insertTestCity(
                    dslContext,
                    countryId,
                    DatabaseTestHelper.createTestCityData(
                        nameRu = "Фаниполь",
                        nameEn = "Fanipal",
                        lat = 53.75,
                        lon = 27.3333
                    )
                )
            val minskItem = mixedCityTwoGisRestrooms.first { item -> item["city"]?.jsonPrimitive?.content == "Минск" }
            val fanipolItem = mixedCityTwoGisRestrooms.first { item -> item["city"]?.jsonPrimitive?.content == "Фаниполь" }
            val body = buildImportBody(listOf(minskItem, fanipolItem))
            val headers = importHeaders()

            KtorTestApplication.withApp(dslContext) { client ->
                val response = client.testPost("/api/v1/import/batch", body, headers)

                response.assertStatusAndJsonContent(HttpStatusCode.Created)
                val importedCities =
                    dslContext
                        .select(RESTROOM_IMPORTS.EXTERNAL_ID, RESTROOM_IMPORTS.CITY_ID)
                        .from(RESTROOM_IMPORTS)
                        .fetchMap(RESTROOM_IMPORTS.EXTERNAL_ID, RESTROOM_IMPORTS.CITY_ID)

                assertEquals(minskId, importedCities[minskItem["id"]!!.jsonPrimitive.content])
                assertEquals(fanipolId, importedCities[fanipolItem["id"]!!.jsonPrimitive.content])
            }
        }

    @Test
    @DisplayName("GIVEN 2GIS item without source city and without city header WHEN POST /import THEN nearest city fallback is used")
    fun given_item_without_source_city_and_without_city_header_when_import_then_uses_nearest_city_fallback() =
        runTest {
            val countryId =
                DatabaseTestHelper.insertTestCountry(
                    dslContext,
                    DatabaseTestHelper.createTestCountryData(
                        nameRu = "Беларусь",
                        nameEn = "Belarus",
                        code = "BY"
                    )
                )
            val minskId =
                DatabaseTestHelper.insertTestCity(
                    dslContext,
                    countryId,
                    DatabaseTestHelper.createTestCityData(
                        nameRu = "Минск",
                        nameEn = "Minsk",
                        lat = 53.9,
                        lon = 27.5667
                    )
                )
            DatabaseTestHelper.insertTestCity(
                dslContext,
                countryId,
                DatabaseTestHelper.createTestCityData(
                    nameRu = "Фаниполь",
                    nameEn = "Fanipal",
                    lat = 53.75,
                    lon = 27.3333
                )
            )
            val payload = JsonObject(sampleRestrooms.first().filterKeys { key -> key != "city" })
            val body = buildImportBody(listOf(payload))
            val headers = importHeaders()

            KtorTestApplication.withApp(dslContext) { client ->
                val response = client.testPost("/api/v1/import", body, headers)

                response.assertStatusAndJsonContent(HttpStatusCode.Created)
                val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                val importId = UUID.fromString(json["importId"]!!.jsonPrimitive.content)
                val importRecord =
                    dslContext
                        .selectFrom(RESTROOM_IMPORTS)
                        .where(RESTROOM_IMPORTS.ID.eq(importId))
                        .fetchOne()

                assertNotNull(importRecord)
                assertEquals(minskId, importRecord!!.cityId)
            }
        }

    @Test
    @DisplayName("GIVEN mixed-validity batch WHEN POST /import/batch THEN successes commit and failures are returned")
    fun given_mixed_validity_batch_when_import_batch_then_returns_partial_results() =
        runTest {
            val env = DatabaseTestHelper.createTestEnvironment(dslContext)
            val items =
                listOf(
                    sampleRestrooms.first(),
                    buildJsonObject {
                        put("id", "broken")
                    }
                )
            val body = buildImportBody(items)
            val headers = importHeaders(env.cityId)

            KtorTestApplication.withApp(dslContext) { client ->
                val response = client.testPost("/api/v1/import/batch", body, headers)

                response.assertStatusAndJsonContent(HttpStatusCode.Created)
                val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                assertEquals(2, json["totalProcessed"]?.jsonPrimitive?.content?.toInt())
                assertEquals(1, json["successful"]?.jsonPrimitive?.content?.toInt())
                assertEquals(1, json["failed"]?.jsonPrimitive?.content?.toInt())

                val results = json["results"]!!.jsonArray
                assertEquals("CREATED", results[0].jsonObject["outcome"]?.jsonPrimitive?.content)
                assertEquals("FAILED", results[1].jsonObject["outcome"]?.jsonPrimitive?.content)
                assertEquals("INVALID_PAYLOAD", results[1].jsonObject["errorCode"]?.jsonPrimitive?.content)

                val successImportCount =
                    dslContext
                        .selectCount()
                        .from(RESTROOM_IMPORTS)
                        .where(RESTROOM_IMPORTS.STATUS.eq("SUCCESS"))
                        .and(RESTROOM_IMPORTS.CITY_ID.eq(env.cityId))
                        .fetchOne()
                        ?.value1() ?: 0
                val failedImportCount =
                    dslContext
                        .selectCount()
                        .from(RESTROOM_IMPORTS)
                        .where(RESTROOM_IMPORTS.STATUS.eq("FAILED"))
                        .and(RESTROOM_IMPORTS.CITY_ID.eq(env.cityId))
                        .fetchOne()
                        ?.value1() ?: 0

                assertEquals(1, successImportCount)
                assertEquals(1, failedImportCount)
            }
        }

    @Test
    @DisplayName("GIVEN payload with missing id WHEN POST /import THEN returns 400 (InvalidImportPayload)")
    fun given_invalid_payload_missing_id_when_import_then_returns_400() =
        runTest {
            val env = DatabaseTestHelper.createTestEnvironment(dslContext)
            val invalidItem =
                buildJsonObject {
                    put("title", "No ID")
                    put("address", "Some address")
                    put(
                        "location",
                        buildJsonObject {
                            put("lat", 53.9)
                            put("lng", 27.5)
                        }
                    )
                }
            val body = buildImportBody(listOf(invalidItem))
            val headers = importHeaders(env.cityId)

            KtorTestApplication.withApp(dslContext) { client ->
                val response = client.testPost("/api/v1/import", body, headers)
                response.assertStatusAndJsonContent(HttpStatusCode.BadRequest)
            }
        }

    @Test
    @DisplayName("GIVEN valid payload without cityId header WHEN POST /import THEN returns 400")
    fun given_valid_payload_without_city_id_when_import_then_returns_400() =
        runTest {
            val body = buildImportBody(listOf(sampleRestrooms.first()))
            val headers =
                mapOf(
                    HEADER_IMPORT_PROVIDER to "2gis",
                    HEADER_IMPORT_PAYLOAD_TYPE to "single"
                )

            KtorTestApplication.withApp(dslContext) { client ->
                val response = client.testPost("/api/v1/import", body, headers)

                response.assertStatusAndJsonContent(HttpStatusCode.BadRequest)
            }
        }

    @Test
    @DisplayName("GIVEN unknown category and unknown attribute groups WHEN POST /import THEN imports successfully (skip unknown, no error)")
    fun given_unknown_category_and_unknown_attrs_when_import_then_succeeds_skip_unknown() =
        runTest {
            val env = DatabaseTestHelper.createTestEnvironment(dslContext)
            val item =
                buildJsonObject {
                    put("id", "2gis_unknown_cat_attrs")
                    put("title", "Place with unknown category and attrs")
                    put("category", "unknown_category_not_in_mapping")
                    put("address", "Test street 1")
                    put(
                        "location",
                        buildJsonObject {
                            put("lat", 53.9)
                            put("lng", 27.5)
                        }
                    )
                    put(
                        "attributeGroups",
                        buildJsonArray {
                            add(JsonPrimitive("Unknown attribute not in mapping"))
                            add(JsonPrimitive("Ещё один неизвестный атрибут"))
                        }
                    )
                }
            val body = buildImportBody(listOf(item))
            val headers = importHeaders(env.cityId)

            KtorTestApplication.withApp(dslContext) { client ->
                val response = client.testPost("/api/v1/import", body, headers)
                response.assertStatusAndJsonContent(HttpStatusCode.Created)
                val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                val restroomId = json["restroomId"]?.jsonPrimitive?.content?.let { UUID.fromString(it) }
                assertNotNull(restroomId)
                val restroom =
                    dslContext
                        .select(RESTROOMS.ID, RESTROOMS.ORIGIN_ID, RESTROOMS.NAME)
                        .from(RESTROOMS)
                        .where(RESTROOMS.ID.eq(restroomId!!))
                        .fetchOne()
                assertNotNull(restroom)
                assertEquals("2gis_unknown_cat_attrs", restroom!!.get(RESTROOMS.ORIGIN_ID))
                assertEquals("Place with unknown category and attrs", restroom.get(RESTROOMS.NAME))
            }
        }

    @Test
    @DisplayName("GIVEN empty items WHEN POST /import THEN returns 400")
    fun given_empty_payload_when_import_then_returns_400() =
        runTest {
            val env = DatabaseTestHelper.createTestEnvironment(dslContext)
            val body = buildImportBody(emptyList())
            val headers = importHeaders(env.cityId)

            KtorTestApplication.withApp(dslContext) { client ->
                val response = client.testPost("/api/v1/import", body, headers)

                response.assertStatusAndJsonContent(HttpStatusCode.BadRequest)
            }
        }

    @Test
    @DisplayName("importBatch_shouldImportAllItemsFromResourceFile")
    fun importBatch_shouldImportAllItemsFromResourceFile() =
        runTest {
            val items = loadImportResourceItems("import/2gis_scraped_places.json")
            val n = items.size
            assertTrue(n in 3..20, "Expected 3-20 items in resource, got $n")

            val env = DatabaseTestHelper.createTestEnvironment(dslContext)
            val body = buildImportBody(items)
            val headers = importHeaders(env.cityId)

            KtorTestApplication.withApp(dslContext) { client ->
                val response = client.testPost("/api/v1/import/batch", body, headers)

                response.assertStatusAndJsonContent(HttpStatusCode.Created)
                val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                assertEquals(n, json["totalProcessed"]?.jsonPrimitive?.content?.toInt())
                assertEquals(n, json["successful"]?.jsonPrimitive?.content?.toInt())
                assertEquals(n, json["results"]?.jsonArray?.size)
                val importId = json["importId"]?.jsonPrimitive?.content?.let { UUID.fromString(it) }
                assertNotNull(importId)

                val successImportCount =
                    dslContext
                        .selectCount()
                        .from(RESTROOM_IMPORTS)
                        .where(RESTROOM_IMPORTS.STATUS.eq("SUCCESS"))
                        .and(RESTROOM_IMPORTS.CITY_ID.eq(env.cityId))
                        .fetchOne()
                        ?.value1() ?: 0
                assertEquals(n, successImportCount)

                val restroomCount =
                    dslContext
                        .selectCount()
                        .from(RESTROOMS)
                        .where(RESTROOMS.ORIGIN_PROVIDER.eq("TWO_GIS"))
                        .fetchOne()
                        ?.value1() ?: 0
                assertEquals(n, restroomCount)

                val restroomsWithBuilding =
                    dslContext
                        .select(RESTROOMS.ID, RESTROOMS.BUILDING_ID)
                        .from(RESTROOMS)
                        .where(RESTROOMS.ORIGIN_PROVIDER.eq("TWO_GIS"))
                        .fetch()
                restroomsWithBuilding.forEach { r ->
                    val buildingId = r.get(RESTROOMS.BUILDING_ID)
                    if (buildingId != null) {
                        val buildingExists =
                            dslContext
                                .select(BUILDINGS.ID)
                                .from(BUILDINGS)
                                .where(BUILDINGS.ID.eq(buildingId))
                                .fetchOne()
                        assertNotNull(buildingExists, "Restroom ${r.get(RESTROOMS.ID)} has buildingId $buildingId but building not found")
                    }
                }
            }
        }

    @Test
    @DisplayName("yandex_importBatch_shouldImportAllItemsFromResourceFile")
    fun yandex_importBatch_shouldImportAllItemsFromResourceFile() =
        runTest {
            val items = loadImportResourceItems("import/yandex_scraped_places.json")
            val n = items.size
            assertTrue(n in 3..20, "Expected 3-20 items in Yandex resource, got $n")

            val env = DatabaseTestHelper.createTestEnvironment(dslContext)
            val body = buildImportBody(items)
            val headers = yandexImportHeaders(env.cityId)

            KtorTestApplication.withApp(dslContext) { client ->
                val response = client.testPost("/api/v1/import/batch", body, headers)

                response.assertStatusAndJsonContent(HttpStatusCode.Created)
                val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                assertEquals(n, json["totalProcessed"]?.jsonPrimitive?.content?.toInt())
                assertEquals(n, json["successful"]?.jsonPrimitive?.content?.toInt())
                assertEquals(n, json["results"]?.jsonArray?.size)
                val importId = json["importId"]?.jsonPrimitive?.content?.let { UUID.fromString(it) }
                assertNotNull(importId)

                val successImportCount =
                    dslContext
                        .selectCount()
                        .from(RESTROOM_IMPORTS)
                        .where(RESTROOM_IMPORTS.STATUS.eq("SUCCESS"))
                        .and(RESTROOM_IMPORTS.CITY_ID.eq(env.cityId))
                        .fetchOne()
                        ?.value1() ?: 0
                assertEquals(n, successImportCount)

                val restroomCount =
                    dslContext
                        .selectCount()
                        .from(RESTROOMS)
                        .where(RESTROOMS.ORIGIN_PROVIDER.eq("YANDEX_MAPS"))
                        .fetchOne()
                        ?.value1() ?: 0
                assertEquals(n, restroomCount)
            }
        }

    @Test
    @DisplayName("yandex_importSingle_shouldImportOneItemFromResourceFile_and_upsert_on_repeat")
    fun yandex_importSingle_shouldImportOneItemFromResourceFile_and_upsert_on_repeat() =
        runTest {
            val items = loadImportResourceItems("import/yandex_scraped_places.json")
            val firstItem = items.first()

            val env = DatabaseTestHelper.createTestEnvironment(dslContext)
            val body = buildImportBody(listOf(firstItem))
            val headers = yandexImportHeaders(env.cityId)

            KtorTestApplication.withApp(dslContext) { client ->
                val response1 = client.testPost("/api/v1/import", body, headers)
                response1.assertStatusAndJsonContent(HttpStatusCode.Created)
                val json1 = Json.parseToJsonElement(response1.bodyAsText()).jsonObject
                val restroomId1 = json1["restroomId"]?.jsonPrimitive?.content?.let { UUID.fromString(it) }
                val status1 = json1["status"]?.jsonPrimitive?.content
                assertNotNull(restroomId1)
                assertTrue(status1 == "SUCCESS", "Expected status SUCCESS, got $status1")

                val response2 = client.testPost("/api/v1/import", body, headers)
                response2.assertStatusAndJsonContent(HttpStatusCode.Created)
                val json2 = Json.parseToJsonElement(response2.bodyAsText()).jsonObject
                val restroomId2 = json2["restroomId"]?.jsonPrimitive?.content?.let { UUID.fromString(it) }
                assertNotNull(restroomId2)
                assertEquals(restroomId1, restroomId2, "Re-import of same Yandex item should return same restroomId (upsert by originId)")

                val placeId = firstItem["placeId"]!!.jsonPrimitive.content
                val countByOrigin =
                    dslContext
                        .selectCount()
                        .from(RESTROOMS)
                        .where(RESTROOMS.ORIGIN_ID.eq(placeId))
                        .and(RESTROOMS.ORIGIN_PROVIDER.eq("YANDEX_MAPS"))
                        .fetchOne()
                        ?.value1() ?: 0
                assertEquals(1, countByOrigin, "Exactly one restroom should exist for this Yandex originId")
            }
        }

    @Test
    @DisplayName("importSingle_shouldImportOneItemFromResourceFile")
    fun importSingle_shouldImportOneItemFromResourceFile() =
        runTest {
            val items = loadImportResourceItems("import/2gis_scraped_places.json")
            val firstItem = items.first()

            val env = DatabaseTestHelper.createTestEnvironment(dslContext)
            val body = buildImportBody(listOf(firstItem))
            val headers = importHeaders(env.cityId)

            KtorTestApplication.withApp(dslContext) { client ->
                val response1 = client.testPost("/api/v1/import", body, headers)
                response1.assertStatusAndJsonContent(HttpStatusCode.Created)
                val json1 = Json.parseToJsonElement(response1.bodyAsText()).jsonObject
                val restroomId1 = json1["restroomId"]?.jsonPrimitive?.content?.let { UUID.fromString(it) }
                val status1 = json1["status"]?.jsonPrimitive?.content
                assertNotNull(restroomId1)
                assertTrue(status1 == "SUCCESS", "Expected status SUCCESS, got $status1")

                val response2 = client.testPost("/api/v1/import", body, headers)
                response2.assertStatusAndJsonContent(HttpStatusCode.Created)
                val json2 = Json.parseToJsonElement(response2.bodyAsText()).jsonObject
                val restroomId2 = json2["restroomId"]?.jsonPrimitive?.content?.let { UUID.fromString(it) }
                assertNotNull(restroomId2)
                assertEquals(restroomId1, restroomId2, "Re-import of same item should return same restroomId (upsert by originId)")

                val countByOrigin =
                    dslContext
                        .selectCount()
                        .from(RESTROOMS)
                        .where(RESTROOMS.ORIGIN_ID.eq(firstItem["id"]!!.jsonPrimitive.content))
                        .and(RESTROOMS.ORIGIN_PROVIDER.eq("TWO_GIS"))
                        .fetchOne()
                        ?.value1() ?: 0
                assertEquals(1, countByOrigin, "Exactly one restroom should exist for this originId")

                val importInboxCount =
                    dslContext
                        .selectCount()
                        .from(RESTROOM_IMPORTS)
                        .where(RESTROOM_IMPORTS.PROVIDER.eq("TWO_GIS"))
                        .and(RESTROOM_IMPORTS.ENTITY_TYPE.eq("place"))
                        .and(RESTROOM_IMPORTS.EXTERNAL_ID.eq(firstItem["id"]!!.jsonPrimitive.content))
                        .fetchOne()
                        ?.value1() ?: 0
                assertEquals(1, importInboxCount, "Exactly one import inbox row should exist for this external ID")
            }
        }

    private fun importHeaders(cityId: UUID? = null): Map<String, String> =
        mutableMapOf(
            HEADER_IMPORT_PROVIDER to "TWO_GIS",
            HEADER_IMPORT_PAYLOAD_TYPE to "TWO_GIS_SCRAPED_PLACE_JSON"
        ).apply {
            cityId?.let { put(HEADER_IMPORT_CITY_ID, it.toString()) }
        }

    private fun yandexImportHeaders(cityId: UUID? = null): Map<String, String> =
        mutableMapOf(
            HEADER_IMPORT_PROVIDER to "YANDEX_MAPS",
            HEADER_IMPORT_PAYLOAD_TYPE to "YANDEX_MAPS_SCRAPED_PLACE_JSON"
        ).apply {
            cityId?.let { put(HEADER_IMPORT_CITY_ID, it.toString()) }
        }

    private fun buildImportBody(items: List<JsonObject>): String =
        buildJsonObject {
            put("items", buildJsonArray { items.forEach { add(it) } })
        }.toString()
}
