package yayauheny.by.importing.api

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import yayauheny.by.importing.service.ImportService
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.imports.ImportBatchResult
import yayauheny.by.model.imports.ImportItemResult
import yayauheny.by.model.imports.ImportStatus
import java.util.UUID

@WebMvcTest(ImportController::class)
@Import(ImportControllerTest.TestConfig::class)
class ImportControllerTest {
    @TestConfiguration
    class TestConfig {
        @Bean
        fun importService(): ImportService = mockk()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var importService: ImportService

    @Test
    fun `given raw array body when batch import then normalize items array and return created`() {
        every {
            importService.importBatch(
                provider = ImportProvider.TWO_GIS,
                payloadType = ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON,
                cityId = null,
                payload = any()
            )
        } answers {
            val payload = arg<com.fasterxml.jackson.databind.JsonNode>(3)
            require(payload.has("items"))
            require(payload["items"].isArray)
            ImportBatchResult(
                importId = UUID.fromString("00000000-0000-0000-0000-000000000111"),
                totalProcessed = 1,
                successful = 1,
                failed = 0,
                results = listOf(
                    ImportItemResult(
                        index = 0,
                        outcome = ImportStatus.CREATED,
                        providerExternalId = "70000001042444951",
                        restroomId = UUID.fromString("00000000-0000-0000-0000-000000000222"),
                        buildingId = UUID.fromString("00000000-0000-0000-0000-000000000333")
                    )
                )
            )
        }

        mockMvc.perform(
            post("/api/v1/import/batch")
                .header("Import-Provider", "TWO_GIS")
                .header("Import-Payload-Type", "TWO_GIS_SCRAPED_PLACE_JSON")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""[{"id":"70000001042444951"}]""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.totalProcessed").value(1))
            .andExpect(jsonPath("$.results[0].outcome").value("CREATED"))
    }

    @Test
    fun `given alias headers and object body when batch import then pass parsed parameters to service`() {
        val cityId = UUID.fromString("00000000-0000-0000-0000-000000000444")
        every {
            importService.importBatch(
                provider = ImportProvider.YANDEX_MAPS,
                payloadType = ImportPayloadType.YANDEX_MAPS_SCRAPED_PLACE_JSON,
                cityId = cityId,
                payload = any()
            )
        } returns
            ImportBatchResult(
            importId = UUID.fromString("00000000-0000-0000-0000-000000000555"),
            totalProcessed = 1,
            successful = 1,
            failed = 0,
            results = listOf(
                ImportItemResult(
                    index = 0,
                    outcome = ImportStatus.UPDATED,
                    providerExternalId = "137246000290",
                    restroomId = UUID.fromString("00000000-0000-0000-0000-000000000666"),
                    buildingId = null
                )
            )
        )

        val requestBody = objectMapper.writeValueAsString(mapOf("items" to listOf(mapOf("placeId" to "137246000290"))))
        mockMvc.perform(
            post("/api/v1/import/batch")
                .header("X-Import-Provider", "YANDEX_MAPS")
                .header("X-Import-Payload-Type", "YANDEX_MAPS_SCRAPED_PLACE_JSON")
                .header("X-Import-City-Id", cityId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.results[0].outcome").value("UPDATED"))

        verify(exactly = 1) {
            importService.importBatch(
                provider = ImportProvider.YANDEX_MAPS,
                payloadType = ImportPayloadType.YANDEX_MAPS_SCRAPED_PLACE_JSON,
                cityId = cityId,
                payload = any()
            )
        }
    }

    @Test
    fun `given invalid import city id header when batch import then return bad request`() {
        mockMvc.perform(
            post("/api/v1/import/batch")
                .header("Import-Provider", "TWO_GIS")
                .header("Import-Payload-Type", "TWO_GIS_SCRAPED_PLACE_JSON")
                .header("Import-City-Id", "not-a-uuid")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"items":[]}""")
        )
            .andExpect(status().isBadRequest)
    }
}
