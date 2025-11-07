package yayauheny.by.unit.controller

import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import yayauheny.by.helpers.assertJsonContentType
import yayauheny.by.helpers.testGet

class HealthRoutingTest : RoutingTestBase() {
    @Nested
    @DisplayName("GET /health")
    inner class HealthEndpoint {
        @Test
        @DisplayName("GIVEN health endpoint WHEN GET /health THEN return 200 with JSON content type")
        fun health_endpoint_returns_200_with_json_content_type() =
            runTest {
                withRoutingApp { client ->
                    val response = client.testGet("/health")

                    assertEquals(HttpStatusCode.OK, response.status, "Should return 200 OK")
                    response.assertJsonContentType()
                }
            }

        @Test
        @DisplayName("GIVEN health endpoint WHEN GET /health THEN response contains healthy status")
        fun health_endpoint_contains_healthy_status() =
            runTest {
                withRoutingApp { client ->
                    val response = client.testGet("/health")
                    val body = response.bodyAsText()

                    assertEquals(HttpStatusCode.OK, response.status, "Should return 200 OK")
                    assertTrue(body.contains("\"status\":\"healthy\""), "Response should contain healthy status")
                }
            }

        @Test
        @DisplayName("GIVEN health endpoint WHEN GET /health multiple times THEN consistently return 200")
        fun health_endpoint_consistently_returns_200() =
            runTest {
                withRoutingApp { client ->
                    repeat(3) { iteration ->
                        val response = client.testGet("/health")
                        assertEquals(HttpStatusCode.OK, response.status, "Health endpoint call #${iteration + 1} should return 200")
                    }
                }
            }
    }
}
