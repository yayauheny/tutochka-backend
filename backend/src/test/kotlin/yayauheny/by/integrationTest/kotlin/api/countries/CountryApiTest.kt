package integration.api.countries

import integration.base.BaseIntegrationTest
import integration.base.KtorTestApplication
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.intOrNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import yayauheny.by.helpers.assertStatusAndJsonContent
import yayauheny.by.helpers.testPost

@Tag("integration")
class CountryApiTest : BaseIntegrationTest() {
    @Nested
    @DisplayName("E2E Test Cases - Database Constraints")
    inner class E2EDatabaseConstraints {
        @Test
        @DisplayName("GIVEN existing country code WHEN POST country with duplicate code THEN return 409 Conflict")
        fun create_country_duplicate_code_returns_409() =
            runTest {
                val firstCountryJson =
                    """
                    {
                        "nameRu": "Первая страна",
                        "nameEn": "First Country",
                        "code": "FC"
                    }
                    """.trimIndent()

                KtorTestApplication.withApp(dslContext) { client ->
                    val firstResponse = client.testPost("/api/v1/countries", firstCountryJson)
                    assertEquals(HttpStatusCode.Created, firstResponse.status)

                    val duplicateCodeJson =
                        """
                        {
                            "nameRu": "Вторая страна",
                            "nameEn": "Second Country",
                            "code": "FC"
                        }
                        """.trimIndent()
                    val response = client.testPost("/api/v1/countries", duplicateCodeJson)

                    response.assertStatusAndJsonContent(HttpStatusCode.Conflict)
                    val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                    assertEquals(409, json["status"]!!.jsonPrimitive.intOrNull)
                    val message = json["message"]?.jsonPrimitive?.content
                    assertTrue(
                        message != null &&
                            (message.lowercase().contains("exist") || message.contains("существует") || message.contains("уже"))
                    )
                }
            }
    }
}
