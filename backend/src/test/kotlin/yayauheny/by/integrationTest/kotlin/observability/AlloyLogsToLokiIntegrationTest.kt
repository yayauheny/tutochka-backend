package integration.observability

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import java.util.UUID
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.images.builder.Transferable
import org.testcontainers.utility.DockerImageName

@Tag("integration")
class AlloyLogsToLokiIntegrationTest {
    @Test
    fun `alloy should deliver docker logs to loki`() {
        val dockerSocket = Path.of("/var/run/docker.sock")
        assertTrue(Files.exists(dockerSocket), "Docker socket not found at /var/run/docker.sock")

        val network = Network.newNetwork()
        val logToken = "alloy-e2e-${UUID.randomUUID()}"
        val loki = createLokiContainer(network)
        val logger = createLogEmitterContainer(network, logToken)
        val alloy = createAlloyContainer(network, dockerSocket, buildAlloyConfig())

        try {
            loki.start()
            logger.start()
            alloy.start()

            val lokiBaseUrl = "http://localhost:${loki.getMappedPort(3100)}"
            val delivered = awaitLogInLoki(lokiBaseUrl, logToken)

            assertTrue(
                delivered,
                "Expected Loki to contain token '$logToken', but it was not found in query results"
            )
        } finally {
            runCatching { alloy.stop() }
            runCatching { logger.stop() }
            runCatching { loki.stop() }
            runCatching { network.close() }
        }
    }

    @Test
    fun `alloy should not ingest logs when loki endpoint is invalid`() {
        val dockerSocket = Path.of("/var/run/docker.sock")
        assertTrue(Files.exists(dockerSocket), "Docker socket not found at /var/run/docker.sock")

        val network = Network.newNetwork()
        val logToken = "alloy-e2e-${UUID.randomUUID()}"
        val loki = createLokiContainer(network)
        val logger = createLogEmitterContainer(network, logToken)
        val alloy =
            createAlloyContainer(
                network,
                dockerSocket,
                buildAlloyConfig("http://loki:3100/invalid")
            )

        try {
            loki.start()
            logger.start()
            alloy.start()

            val lokiBaseUrl = "http://localhost:${loki.getMappedPort(3100)}"
            val writeHasError = awaitAlloyWriteError(alloy)
            val delivered = awaitLogInLoki(lokiBaseUrl, logToken, attempts = 12, sleepMs = 1000)

            assertTrue(writeHasError, "Expected Alloy to report loki.write error for invalid endpoint")
            assertFalse(
                delivered,
                "Expected token '$logToken' to be absent in Loki when loki.write endpoint is invalid"
            )
        } finally {
            runCatching { alloy.stop() }
            runCatching { logger.stop() }
            runCatching { loki.stop() }
            runCatching { network.close() }
        }
    }

    private fun createLokiContainer(network: Network): GenericContainer<*> =
        GenericContainer(DockerImageName.parse("grafana/loki:3.5.2"))
            .withNetwork(network)
            .withNetworkAliases("loki")
            .withExposedPorts(3100)
            .withCommand("-config.file=/etc/loki/local-config.yaml")
            .waitingFor(Wait.forHttp("/ready").forPort(3100).forStatusCode(200))
            .withStartupTimeout(Duration.ofSeconds(90))

    private fun createLogEmitterContainer(
        network: Network,
        token: String
    ): GenericContainer<*> =
        GenericContainer(DockerImageName.parse("alpine:3.20"))
            .withNetwork(network)
            .withLabel("alloy.e2e.logsource", "true")
            .withCommand("sh", "-c", "while true; do echo \"$token\"; sleep 1; done")
            .withStartupTimeout(Duration.ofSeconds(30))

    private fun createAlloyContainer(
        network: Network,
        dockerSocket: Path,
        alloyConfig: String
    ): GenericContainer<*> =
        GenericContainer(DockerImageName.parse("grafana/alloy:v1.15.1"))
            .withNetwork(network)
            .withFileSystemBind(dockerSocket.toString(), "/var/run/docker.sock", BindMode.READ_ONLY)
            .withCopyToContainer(
                Transferable.of(alloyConfig.toByteArray(StandardCharsets.UTF_8), 0b110100100),
                "/etc/alloy/config.alloy"
            ).withCommand("run", "--storage.path=/var/lib/alloy/data", "/etc/alloy/config.alloy")
            .withStartupTimeout(Duration.ofSeconds(90))

    private fun buildAlloyConfig(lokiPushUrl: String = "http://loki:3100/loki/api/v1/push"): String =
        """
        discovery.docker "log_source" {
          host = "unix:///var/run/docker.sock"
          filter {
            name   = "label"
            values = ["alloy.e2e.logsource=true"]
          }
        }

        loki.source.docker "log_source" {
          host             = "unix:///var/run/docker.sock"
          targets          = discovery.docker.log_source.targets
          refresh_interval = "1s"
          labels = {
            job          = "alloy-e2e",
            service_name = "alloy-e2e",
          }
          forward_to = [loki.write.local.receiver]
        }

        loki.write "local" {
          endpoint {
            url = "$lokiPushUrl"
          }
        }
        """.trimIndent()

    private fun awaitLogInLoki(
        lokiBaseUrl: String,
        token: String,
        attempts: Int = 40,
        sleepMs: Long = 1500
    ): Boolean {
        val client =
            HttpClient
                .newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build()
        val startNs = Instant.now().minusSeconds(120).toEpochMilli() * 1_000_000

        for (attempt in 1..attempts) {
            val query = URLEncoder.encode("{job=\"alloy-e2e\"} |= \"$token\"", StandardCharsets.UTF_8)
            val endNs = Instant.now().toEpochMilli() * 1_000_000
            val request =
                HttpRequest
                    .newBuilder(
                        URI.create(
                            "$lokiBaseUrl/loki/api/v1/query_range?query=$query&start=$startNs&end=$endNs&limit=20&direction=backward"
                        )
                    ).timeout(Duration.ofSeconds(5))
                    .GET()
                    .build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() == 200 && response.body().contains(token)) {
                return true
            }

            Thread.sleep(sleepMs)
        }

        return false
    }

    private fun awaitAlloyWriteError(alloy: GenericContainer<*>): Boolean {
        for (attempt in 1..30) {
            val logs = runCatching { alloy.logs.lowercase() }.getOrElse { "" }
            if (
                logs.contains(" 404 ") ||
                logs.contains("status=404") ||
                logs.contains("status code 404") ||
                logs.contains("404 not found") ||
                logs.contains("failed to send batch") ||
                logs.contains("error sending batch")
            ) {
                return true
            }
            Thread.sleep(1000)
        }

        return false
    }
}
