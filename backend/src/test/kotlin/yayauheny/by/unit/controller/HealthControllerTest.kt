package yayauheny.by.unit.controller

import java.sql.Connection
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import yayauheny.by.controller.HealthController
import yayauheny.by.helpers.assertBodyContains
import yayauheny.by.helpers.assertJsonContentType
import yayauheny.by.helpers.testGet

@WebMvcTest(HealthController::class)
@DisplayName("HealthController Tests")
class HealthControllerTest : RoutingTestBase() {
    @Nested
    @DisplayName("healthRoutes Tests")
    inner class HealthRoutesTests {
        @Test
        @DisplayName("GIVEN healthy database WHEN GET /health THEN return 200 with healthy status")
        fun health_endpoint_with_healthy_db_returns_200() {
            val connection = org.mockito.Mockito.mock(Connection::class.java)
            given(dataSource.connection).willReturn(connection)
            given(connection.isValid(1)).willReturn(true)

            withRoutingApp {
                val response = testGet("/health")

                assert(response.status == HttpStatus.OK)
                response.assertJsonContentType()
                response.assertBodyContains("\"status\":\"healthy\"")
                response.assertBodyContains("\"database\":\"connected\"")
            }
        }

        @Test
        @DisplayName("GIVEN unhealthy database WHEN GET /health THEN return 503 with unhealthy status")
        fun health_endpoint_with_unhealthy_db_returns_503() {
            val connection = org.mockito.Mockito.mock(Connection::class.java)
            given(dataSource.connection).willReturn(connection)
            given(connection.isValid(1)).willReturn(false)

            withRoutingApp {
                val response = testGet("/health")

                assert(response.status == HttpStatus.SERVICE_UNAVAILABLE)
                response.assertJsonContentType()
                response.assertBodyContains("\"status\":\"unhealthy\"")
                response.assertBodyContains("\"database\":\"disconnected\"")
            }
        }

        @Test
        @DisplayName("GIVEN healthy database WHEN GET /health/ready THEN return 200 with ready status")
        fun health_ready_endpoint_with_healthy_db_returns_200() {
            val connection = org.mockito.Mockito.mock(Connection::class.java)
            given(dataSource.connection).willReturn(connection)
            given(connection.isValid(1)).willReturn(true)

            withRoutingApp {
                val response = testGet("/health/ready")

                assert(response.status == HttpStatus.OK)
                response.assertJsonContentType()
                response.assertBodyContains("\"status\":\"ready\"")
                response.assertBodyContains("\"database\":\"connected\"")
            }
        }

        @Test
        @DisplayName("GIVEN unhealthy database WHEN GET /health/ready THEN return 503 with not ready status")
        fun health_ready_endpoint_with_unhealthy_db_returns_503() {
            val connection = org.mockito.Mockito.mock(Connection::class.java)
            given(dataSource.connection).willReturn(connection)
            given(connection.isValid(1)).willReturn(false)

            withRoutingApp {
                val response = testGet("/health/ready")

                assert(response.status == HttpStatus.SERVICE_UNAVAILABLE)
                response.assertJsonContentType()
                response.assertBodyContains("\"status\":\"not ready\"")
                response.assertBodyContains("\"database\":\"disconnected\"")
            }
        }

        @Test
        @DisplayName("GIVEN any state WHEN GET /health/live THEN return 200 with alive status")
        fun health_live_endpoint_always_returns_200() {
            withRoutingApp {
                val response = testGet("/health/live")

                assert(response.status == HttpStatus.OK)
                response.assertJsonContentType()
                response.assertBodyContains("\"status\":\"alive\"")
                response.assertBodyContains("\"service\":\"TuTochka API\"")
            }
        }
    }
}
