package yayauheny.by.integrationTest.kotlin.service.import

import integration.base.BaseIntegrationTest
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import yayauheny.by.helpers.DatabaseTestHelper
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.repository.RestroomRepository
import yayauheny.by.repository.impl.RestroomRepositoryImpl
import yayauheny.by.service.import.InvalidImportPayload
import yayauheny.by.service.import.twogis.TwoGisScrapedImportStrategy

@Tag("integration")
@DisplayName("TwoGisScrapedImportStrategy Integration Tests")
class TwoGisScrapedImportStrategyTest : BaseIntegrationTest() {
    private lateinit var importStrategy: TwoGisScrapedImportStrategy
    private lateinit var restroomRepository: RestroomRepository

    @BeforeEach
    override fun openConnectionAndResetData() {
        super.openConnectionAndResetData()
        restroomRepository = RestroomRepositoryImpl(dslContext)
        importStrategy = TwoGisScrapedImportStrategy(dslContext, restroomRepository)
    }

    @Nested
    @DisplayName("Single Item Import")
    inner class SingleItemImport {
        @Test
        fun `should import single restroom`() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
                val cityId = testEnv.cityId

                val payload =
                    buildJsonObject {
                        put("id", "2gis_12345")
                        put("title", "Test Public Toilet")
                        put("category", "toilet")
                        put("address", "Test Street 1")
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
                                add(JsonPrimitive("бесплатный туалет"))
                            }
                        )
                    }

                val result = importStrategy.importObject(cityId, ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON, payload)

                assertNotNull(result.restroomId)
                val restroom = restroomRepository.findById(result.restroomId)
                assertNotNull(restroom)
                assertEquals("Test Public Toilet", restroom.name)
                assertEquals("Test Street 1", restroom.address)
                assertEquals("2gis_12345", restroom.originId)
            }

        @Test
        fun `should update existing restroom by origin_id`() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
                val cityId = testEnv.cityId

                val payload1 =
                    buildJsonObject {
                        put("id", "2gis_12345")
                        put("title", "Original Name")
                        put("category", "toilet")
                        put("address", "Original Address")
                        put(
                            "location",
                            buildJsonObject {
                                put("lat", 53.9)
                                put("lng", 27.5)
                            }
                        )
                    }

                val result1 = importStrategy.importObject(cityId, ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON, payload1)
                val originalRestroomId = result1.restroomId

                val payload2 =
                    buildJsonObject {
                        put("id", "2gis_12345")
                        put("title", "Updated Name")
                        put("category", "toilet")
                        put("address", "Updated Address")
                        put(
                            "location",
                            buildJsonObject {
                                put("lat", 53.9)
                                put("lng", 27.5)
                            }
                        )
                    }

                val result2 = importStrategy.importObject(cityId, ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON, payload2)

                assertEquals(originalRestroomId, result2.restroomId)
                val restroom = restroomRepository.findById(result2.restroomId)
                assertNotNull(restroom)
                assertEquals("Updated Name", restroom.name)
                assertEquals("Updated Address", restroom.address)
            }
    }

    @Nested
    @DisplayName("Batch Import")
    inner class BatchImport {
        @Test
        fun `should import multiple restrooms in batch`() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
                val cityId = testEnv.cityId

                val payload =
                    buildJsonObject {
                        put(
                            "items",
                            buildJsonArray {
                                add(
                                    buildJsonObject {
                                        put("id", "2gis_1")
                                        put("title", "Toilet 1")
                                        put("category", "toilet")
                                        put("address", "Street 1")
                                        put(
                                            "location",
                                            buildJsonObject {
                                                put("lat", 53.9)
                                                put("lng", 27.5)
                                            }
                                        )
                                    }
                                )
                                add(
                                    buildJsonObject {
                                        put("id", "2gis_2")
                                        put("title", "Toilet 2")
                                        put("category", "toilet")
                                        put("address", "Street 2")
                                        put(
                                            "location",
                                            buildJsonObject {
                                                put("lat", 53.91)
                                                put("lng", 27.51)
                                            }
                                        )
                                    }
                                )
                                add(
                                    buildJsonObject {
                                        put("id", "2gis_3")
                                        put("title", "Toilet 3")
                                        put("category", "toilet")
                                        put("address", "Street 3")
                                        put(
                                            "location",
                                            buildJsonObject {
                                                put("lat", 53.92)
                                                put("lng", 27.52)
                                            }
                                        )
                                    }
                                )
                            }
                        )
                    }

                val results = importStrategy.importBatch(cityId, ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON, payload)

                assertEquals(3, results.size)
                results.forEach { result ->
                    assertNotNull(result.restroomId)
                    val restroom = restroomRepository.findById(result.restroomId)
                    assertNotNull(restroom)
                    assertTrue(restroom.name?.contains("Toilet") == true)
                    assertEquals(ImportProvider.TWO_GIS, restroom.originProvider)
                }
            }

        @Test
        fun `should handle mixed valid and invalid items`() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
                val cityId = testEnv.cityId

                val payload =
                    buildJsonObject {
                        put(
                            "items",
                            buildJsonArray {
                                add(
                                    buildJsonObject {
                                        put("id", "2gis_valid")
                                        put("title", "Valid Toilet")
                                        put("category", "toilet")
                                        put("address", "Valid Street")
                                        put(
                                            "location",
                                            buildJsonObject {
                                                put("lat", 53.9)
                                                put("lng", 27.5)
                                            }
                                        )
                                    }
                                )
                                add(
                                    buildJsonObject {
                                        // Missing required fields - should be skipped
                                        put("id", "2gis_invalid")
                                    }
                                )
                            }
                        )
                    }

                // Fail-fast: batch throws on first invalid item (missing location)
                assertFailsWith<InvalidImportPayload> {
                    importStrategy.importBatch(cityId, ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON, payload)
                }
            }
    }
}
