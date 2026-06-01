package integration.api.importing

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import integration.base.BaseIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import yayauheny.by.entity.CountryEntity
import yayauheny.by.helpers.DatabaseTestHelper
import yayauheny.by.importing.repository.DuplicateSuspicionJpaRepository
import yayauheny.by.importing.repository.RestroomImportJpaRepository
import yayauheny.by.model.enums.DataSourceType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.enums.LocationType
import yayauheny.by.model.enums.RestroomStatus
import java.util.UUID

@Tag("integration")
class ImportBatchApiIntegrationTest : BaseIntegrationTest() {
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var restroomImportRepository: RestroomImportJpaRepository

    @Autowired
    private lateinit var duplicateSuspicionRepository: DuplicateSuspicionJpaRepository

    private lateinit var minskCityId: UUID

    @BeforeEach
    fun setUpImportFixtures() {
        DatabaseTestHelper.truncateAllTables()
        val countryId = DatabaseTestHelper.insertTestCountry(data = DatabaseTestHelper.createTestCountryData(nameRu = "Беларусь", nameEn = "Belarus", code = "BY"))
        minskCityId = DatabaseTestHelper.insertTestCity(
            countryId = countryId,
            data = DatabaseTestHelper.createTestCityData(nameRu = "Минск", nameEn = "Minsk", lat = 53.9, lon = 27.5667)
        )
    }

    @Test
    fun `given real twogis batch fixture when import then create restrooms and expose them in nearest search`() {
        val body = objectMapper.writeValueAsString(listOf(twoGisItem("70000001062416076"), twoGisItem("70000001055539762")))

        val response = performImport("TWO_GIS", "TWO_GIS_SCRAPED_PLACE_JSON", body)

        assertThat(response["totalProcessed"].asInt()).isEqualTo(2)
        assertThat(response["successful"].asInt()).isEqualTo(2)
        assertThat(response["failed"].asInt()).isEqualTo(0)
        assertThat(response["results"].map { it["outcome"].asText() }).containsOnly("CREATED")

        val imported = restroomRepository.findByOriginProviderAndOriginId(ImportProvider.TWO_GIS, "70000001062416076")
        assertThat(imported).isNotNull
        assertThat(imported!!.city?.id).isEqualTo(minskCityId)
        assertThat(imported.address).isEqualTo("Рудобельская, 3")
        assertThat(imported.coordinates?.y).isEqualTo(53.861329)
        assertThat(imported.coordinates?.x).isEqualTo(27.638588)
        assertThat(imported.dataSource).isEqualTo(DataSourceType.IMPORT)
        assertThat(imported.originProvider).isEqualTo(ImportProvider.TWO_GIS)
        assertThat(imported.originId).isEqualTo("70000001062416076")
        assertThat(imported.externalMaps?.get("2gis")).isEqualTo("70000001062416076")
        assertThat(imported.building?.id).isNotNull

        val nearest = mockMvc.perform(
            get("/api/v1/restrooms/nearest")
                .param("lat", "53.861329")
                .param("lon", "27.638588")
                .param("limit", "5")
        )
            .andExpect(status().isOk)
            .andReturn()

        val nearestBody = objectMapper.readTree(nearest.response.contentAsString)
        assertThat(nearestBody).hasSize(1)
        assertThat(nearestBody[0]["displayName"].asText()).isEqualTo(imported.name)
    }

    @Test
    fun `given real yandex batch fixture when import then keep inactive rows out of nearest search`() {
        val body = objectMapper.writeValueAsString(mapOf("items" to listOf(yandexItem("137246000290"), yandexItem("239919493009"))))

        val response = performImport("YANDEX_MAPS", "YANDEX_MAPS_SCRAPED_PLACE_JSON", body)

        assertThat(response["results"].map { it["outcome"].asText() }).containsOnly("CREATED")

        val inactive = restroomRepository.findByOriginProviderAndOriginId(ImportProvider.YANDEX_MAPS, "137246000290")
        val active = restroomRepository.findByOriginProviderAndOriginId(ImportProvider.YANDEX_MAPS, "239919493009")
        assertThat(inactive!!.status).isEqualTo(RestroomStatus.INACTIVE)
        assertThat(active!!.status).isEqualTo(RestroomStatus.ACTIVE)
        assertThat(active.city?.id).isEqualTo(minskCityId)
        assertThat(active.externalMaps?.get("yandex")).isEqualTo("239919493009")

        val nearest = mockMvc.perform(
            get("/api/v1/restrooms/nearest")
                .param("lat", "53.909435")
                .param("lon", "27.573736")
                .param("limit", "5")
                .param("distanceMeters", "10000")
        )
            .andExpect(status().isOk)
            .andReturn()

        val nearestBody = objectMapper.readTree(nearest.response.contentAsString)
        assertThat(nearestBody).hasSize(1)
        assertThat(nearestBody[0]["displayName"].asText()).isEqualTo(active.name)
    }

    @Test
    fun `given same payload reimport when import then skip duplicate and keep one restroom row`() {
        val item = twoGisItem("70000001062416076")
        performImport("TWO_GIS", "TWO_GIS_SCRAPED_PLACE_JSON", objectMapper.writeValueAsString(listOf(item)))

        val secondResponse = performImport("TWO_GIS", "TWO_GIS_SCRAPED_PLACE_JSON", objectMapper.writeValueAsString(listOf(item)))

        assertThat(secondResponse["results"][0]["outcome"].asText()).isEqualTo("SKIPPED_DUPLICATE")
        assertThat(restroomRepository.count()).isEqualTo(1)
        assertThat(restroomImportRepository.count()).isEqualTo(1)
    }

    @Test
    fun `given changed provider payload reimport when import then update existing restroom`() {
        val original = twoGisItem("70000001062416076")
        performImport("TWO_GIS", "TWO_GIS_SCRAPED_PLACE_JSON", objectMapper.writeValueAsString(listOf(original)))

        val changed = original.deepCopy().apply {
            put("title", "GreenTime Updated")
            put("address", "Рудобельская, 3Б")
            val location = get("location") as ObjectNode
            location.put("lng", 27.638999)
        }

        val response = performImport("TWO_GIS", "TWO_GIS_SCRAPED_PLACE_JSON", objectMapper.writeValueAsString(listOf(changed)))

        assertThat(response["results"][0]["outcome"].asText()).isEqualTo("UPDATED")
        assertThat(restroomRepository.count()).isEqualTo(1)

        val updated = restroomRepository.findByOriginProviderAndOriginId(ImportProvider.TWO_GIS, "70000001062416076")!!
        assertThat(updated.name).isEqualTo("GreenTime Updated")
        assertThat(updated.address).isEqualTo("Рудобельская, 3Б")
        assertThat(updated.coordinates?.x).isEqualTo(27.638999)
    }

    @Test
    fun `given exact restroom match reimport under new provider object id when import then link duplicate without new restroom`() {
        val original = twoGisItem("70000001062416076")
        performImport("TWO_GIS", "TWO_GIS_SCRAPED_PLACE_JSON", objectMapper.writeValueAsString(listOf(original)))

        val duplicate = original.deepCopy().apply {
            put("id", "70000001062416077")
            put("url", "https://2gis.by/minsk/firm/70000001062416077")
        }

        val response = performImport("TWO_GIS", "TWO_GIS_SCRAPED_PLACE_JSON", objectMapper.writeValueAsString(listOf(duplicate)))

        assertThat(response["results"][0]["outcome"].asText()).isEqualTo("LINKED_DUPLICATE")
        assertThat(response["results"][0]["duplicateReason"].asText()).isEqualTo("EXACT_RESTROOM_MATCH_KEY")
        assertThat(restroomRepository.count()).isEqualTo(1)
    }

    @Test
    fun `given mixed valid and invalid items when import then return partial success with failure details`() {
        val valid = twoGisItem("70000001062416076")
        val invalid = objectMapper.createObjectNode().apply {
            put("title", "broken")
            put("country", "Республика Беларусь")
            put("city", "Минск")
            set<ObjectNode>(
                "location",
                objectMapper.createObjectNode()
                    .put("lat", 53.9)
                    .put("lng", 27.56)
            )
        }
        val body = objectMapper.writeValueAsString(listOf(valid, invalid))

        val response = performImport("TWO_GIS", "TWO_GIS_SCRAPED_PLACE_JSON", body)

        assertThat(response["totalProcessed"].asInt()).isEqualTo(2)
        assertThat(response["successful"].asInt()).isEqualTo(1)
        assertThat(response["failed"].asInt()).isEqualTo(1)
        assertThat(response["results"][0]["outcome"].asText()).isEqualTo("CREATED")
        assertThat(response["results"][1]["outcome"].asText()).isEqualTo("FAILED")
        assertThat(response["results"][1]["errorCode"].asText()).isEqualTo("INVALID_PAYLOAD")
        assertThat(restroomRepository.count()).isEqualTo(1)
    }

    @Test
    fun `given nearby non exact duplicate when import then create suspicion row`() {
        val original = twoGisItem("70000001062416076")
        performImport("TWO_GIS", "TWO_GIS_SCRAPED_PLACE_JSON", objectMapper.writeValueAsString(listOf(original)))

        val nearby = original.deepCopy().apply {
            put("id", "70000001062416099")
            put("title", "GreenTime Annex")
            val location = get("location") as ObjectNode
            location.put("lat", 53.86144)
            location.put("lng", 27.63870)
        }

        val response = performImport("TWO_GIS", "TWO_GIS_SCRAPED_PLACE_JSON", objectMapper.writeValueAsString(listOf(nearby)))

        assertThat(response["results"][0]["outcome"].asText()).isEqualTo("CREATED")
        assertThat(restroomRepository.count()).isEqualTo(2)
        assertThat(duplicateSuspicionRepository.findPendingSuspicions()).isNotEmpty
    }

    private fun performImport(provider: String, payloadType: String, body: String): JsonNode {
        val mvcResult = mockMvc.perform(
            post("/api/v1/import/batch")
                .header("Import-Provider", provider)
                .header("Import-Payload-Type", payloadType)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
            .andExpect(status().isCreated)
            .andReturn()

        return objectMapper.readTree(mvcResult.response.contentAsString)
    }

    private fun twoGisItem(id: String): ObjectNode {
        val root = readResourceNode("import/2gis/restrooms_2gis_sample.json") as ArrayNode
        return root.first { it["id"].asText() == id }.deepCopy()
    }

    private fun yandexItem(placeId: String): ObjectNode {
        val root = readResourceNode("import/yandex/yandex_scraped_places.json") as ObjectNode
        val items = root.withArray("items")
        return items.first { it["placeId"].asText() == placeId }.deepCopy()
    }

    private fun readResourceNode(resourcePath: String): JsonNode =
        objectMapper.readTree(
            object {}.javaClass.classLoader
                .getResourceAsStream(resourcePath)
                ?.reader(Charsets.UTF_8)
                ?.use { it.readText() }
                ?: error("Resource not found: $resourcePath")
        )
}
