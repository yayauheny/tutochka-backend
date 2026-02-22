package integration.api.importapi

import integration.base.BaseIntegrationTest
import integration.base.KtorTestApplication
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import yayauheny.by.helpers.DatabaseTestHelper
import yayauheny.by.helpers.assertStatusAndJsonContent
import yayauheny.by.helpers.parseErrorResponse
import yayauheny.by.helpers.testPost
import yayauheny.by.tables.references.RESTROOM_IMPORTS
import yayauheny.by.tables.references.RESTROOMS

@Tag("integration")
class ImportApiIntegrationTest : BaseIntegrationTest() {
    @Test
    @DisplayName("GIVEN valid 2GIS payload and existing city WHEN POST /import THEN restroom and import record created")
    fun given_valid_payload_when_import_single_then_creates_restroom_and_import_record() =
        runTest {
            val env = DatabaseTestHelper.createTestEnvironment(dslContext)
            val payload = build2GisItem(id = "2gis-001", title = "ТЦ GreenTime", address = "Рудобельская, 3", lat = 53.861, lon = 27.638)
            val body = buildImportRequest(env.cityId, payload)

            KtorTestApplication.withApp(dslContext) { client ->
                val response = client.testPost("/api/v1/import", body)

                response.assertStatusAndJsonContent(HttpStatusCode.Created)
                val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                val restroomId = json["restroomId"]?.jsonPrimitive?.content?.let { java.util.UUID.fromString(it) }
                val importId = json["importId"]?.jsonPrimitive?.content?.let { java.util.UUID.fromString(it) }
                assertNotNull(restroomId)
                assertNotNull(importId)

                val importRecord =
                    dslContext
                        .selectFrom(RESTROOM_IMPORTS)
                        .where(RESTROOM_IMPORTS.ID.eq(importId!!))
                        .fetchOne()
                assertNotNull(importRecord)
                assertEquals("SUCCESS", importRecord!!.status)
                assertEquals(restroomId, importRecord.restroomId)

                val restroom =
                    dslContext
                        .select(RESTROOMS.ID, RESTROOMS.ORIGIN_ID, RESTROOMS.ORIGIN_PROVIDER, RESTROOMS.NAME)
                        .from(RESTROOMS)
                        .where(RESTROOMS.ID.eq(restroomId!!))
                        .fetchOne()
                assertNotNull(restroom)
                assertEquals("2gis-001", restroom!!.get(RESTROOMS.ORIGIN_ID))
                assertEquals("TWO_GIS", restroom.get(RESTROOMS.ORIGIN_PROVIDER))
                assertEquals("ТЦ GreenTime", restroom.get(RESTROOMS.NAME))
            }
        }

    @Test
    @DisplayName("GIVEN batch of 2 items WHEN POST /import/batch THEN both restrooms and import record created")
    fun given_batch_payload_when_import_batch_then_creates_restrooms_and_import_record() =
        runTest {
            val env = DatabaseTestHelper.createTestEnvironment(dslContext)
            val items =
                buildJsonArray {
                    add(build2GisItem("2gis-batch-1", "Гиппо ТЦ", "Максима Горецкого, 2", 53.876, 27.465))
                    add(build2GisItem("2gis-batch-2", "Юго-западная", "Железнодорожная, 41", 53.872, 27.500))
                }
            val body = buildImportRequest(env.cityId, buildJsonObject { put("items", items) })

            KtorTestApplication.withApp(dslContext) { client ->
                val response = client.testPost("/api/v1/import/batch", body)

                response.assertStatusAndJsonContent(HttpStatusCode.Created)
                val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                assertEquals(2, json["totalProcessed"]?.jsonPrimitive?.content?.toInt())
                assertEquals(2, json["successful"]?.jsonPrimitive?.content?.toInt())
                val importId = json["importId"]?.jsonPrimitive?.content?.let { java.util.UUID.fromString(it) }
                assertNotNull(importId)

                val importRecord =
                    dslContext
                        .selectFrom(RESTROOM_IMPORTS)
                        .where(RESTROOM_IMPORTS.ID.eq(importId!!))
                        .fetchOne()
                assertNotNull(importRecord)
                assertEquals("SUCCESS", importRecord!!.status)

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
    @DisplayName("GIVEN payload with missing id WHEN POST /import THEN returns 400 validation error")
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
            val body = buildImportRequest(env.cityId, invalidItem)

            KtorTestApplication.withApp(dslContext) { client ->
                val response = client.testPost("/api/v1/import", body)

                response.assertStatusAndJsonContent(HttpStatusCode.BadRequest)
                val errorResponse = response.parseErrorResponse()
                assertTrue(errorResponse.errors?.any { it.field?.contains("payload") == true } == true)
            }
        }

    @Test
    @DisplayName("GIVEN empty payload WHEN POST /import THEN returns 400")
    fun given_empty_payload_when_import_then_returns_400() =
        runTest {
            val env = DatabaseTestHelper.createTestEnvironment(dslContext)
            val body =
                buildJsonObject {
                    put("provider", "TWO_GIS")
                    put("payloadType", "TWO_GIS_SCRAPED_PLACE_JSON")
                    put("cityId", env.cityId.toString())
                    put("payload", buildJsonObject {})
                }.toString()

            KtorTestApplication.withApp(dslContext) { client ->
                val response = client.testPost("/api/v1/import", body)

                response.assertStatusAndJsonContent(HttpStatusCode.BadRequest)
            }
        }

    private fun build2GisItem(
        id: String,
        title: String,
        address: String,
        lat: Double,
        lon: Double
    ) = buildJsonObject {
        put("id", id)
        put("title", title)
        put("address", address)
        put("category", "mall")
        put(
            "location",
            buildJsonObject {
                put("lat", lat)
                put("lng", lon)
            }
        )
        put("attributeGroups", buildJsonArray { add(JsonPrimitive("Туалет")) })
    }

    private fun buildImportRequest(
        cityId: java.util.UUID,
        payload: kotlinx.serialization.json.JsonObject
    ): String =
        buildJsonObject {
            put("provider", "TWO_GIS")
            put("payloadType", "TWO_GIS_SCRAPED_PLACE_JSON")
            put("cityId", cityId.toString())
            put("payload", payload)
        }.toString()
}
