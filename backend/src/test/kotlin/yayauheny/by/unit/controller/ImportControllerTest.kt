package yayauheny.by.unit.controller

import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.slot
import java.util.UUID
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import yayauheny.by.helpers.assertStatusAndJsonContent
import yayauheny.by.helpers.testPost
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.import.ImportBatchResult
import yayauheny.by.model.import.ImportItemResult
import yayauheny.by.model.import.ImportStatus
import yayauheny.by.util.HEADER_IMPORT_CITY_ID
import yayauheny.by.util.HEADER_IMPORT_PAYLOAD_TYPE
import yayauheny.by.util.HEADER_IMPORT_PROVIDER

@DisplayName("ImportController Tests")
class ImportControllerTest : RoutingTestBase() {
    @Test
    @DisplayName("GIVEN raw array body WHEN POST /import/batch THEN whole file body is accepted")
    fun given_raw_array_body_when_post_import_batch_then_whole_file_body_is_accepted() =
        runTest {
            val cityId = UUID.randomUUID()
            val importId = UUID.randomUUID()
            val firstItem =
                buildJsonObject {
                    put("id", "70000001062416076")
                    put("title", "GreenTime")
                }
            val secondItem =
                buildJsonObject {
                    put("id", "70000001062416077")
                    put("title", "Galileo")
                }
            val payloadSlot = slot<JsonObject>()

            coEvery {
                importService.importBatch(
                    provider = ImportProvider.TWO_GIS,
                    payloadType = ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON,
                    cityId = cityId,
                    payload = capture(payloadSlot)
                )
            } returns
                ImportBatchResult(
                    importId = importId,
                    totalProcessed = 2,
                    successful = 2,
                    failed = 0,
                    results =
                        listOf(
                            ImportItemResult(
                                index = 0,
                                outcome = ImportStatus.CREATED,
                                providerExternalId = "70000001062416076",
                                restroomId = UUID.randomUUID(),
                                buildingId = null
                            ),
                            ImportItemResult(
                                index = 1,
                                outcome = ImportStatus.CREATED,
                                providerExternalId = "70000001062416077",
                                restroomId = UUID.randomUUID(),
                                buildingId = null
                            )
                        )
                )

            val body =
                buildJsonArray {
                    add(firstItem)
                    add(secondItem)
                }.toString()
            val headers =
                mapOf(
                    HEADER_IMPORT_PROVIDER to "TWO_GIS",
                    HEADER_IMPORT_PAYLOAD_TYPE to "TWO_GIS_SCRAPED_PLACE_JSON",
                    HEADER_IMPORT_CITY_ID to cityId.toString()
                )

            withRoutingApp { client ->
                val response = client.testPost("/api/v1/import/batch", body, headers)

                response.assertStatusAndJsonContent(HttpStatusCode.Created)
                val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                assertEquals(2, json["totalProcessed"]?.jsonPrimitive?.content?.toInt())
                assertEquals(2, json["successful"]?.jsonPrimitive?.content?.toInt())
                assertEquals(2, payloadSlot.captured["items"]?.jsonArray?.size)
                assertEquals(firstItem, payloadSlot.captured["items"]!!.jsonArray[0].jsonObject)
                assertEquals(secondItem, payloadSlot.captured["items"]!!.jsonArray[1].jsonObject)
            }

            coVerify(exactly = 1) {
                importService.importBatch(
                    provider = ImportProvider.TWO_GIS,
                    payloadType = ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON,
                    cityId = cityId,
                    payload = any()
                )
            }
        }
}
