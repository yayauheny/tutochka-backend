package yayauheny.by.unit.observability

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

class LoggingConfigTest {
    @Test
    fun `alloy config should read backend and bot logs from files`() {
        val alloyConfig = readRepoFile("alloy/config.alloy")

        assertTrue(alloyConfig.contains("""loki.source.file "backend" """))
        assertTrue(alloyConfig.contains("""loki.source.file "bot" """))
        assertTrue(alloyConfig.contains("""__path__     = "/var/log/tutochka/backend/*.log""""))
        assertTrue(alloyConfig.contains("""__path__     = "/var/log/tutochka/bot/*.log""""))
        assertFalse(alloyConfig.contains("discovery.docker"))
        assertFalse(alloyConfig.contains("loki.source.docker"))
    }

    @Test
    fun `backend and bot logback configs should rotate files`() {
        val backendLogback = readRepoFile("backend/src/main/resources/logback.xml")
        val botLogback = readRepoFile("bot/src/main/resources/logback.xml")

        assertTrue(backendLogback.contains("RollingFileAppender"))
        assertTrue(backendLogback.contains("SizeAndTimeBasedRollingPolicy"))
        assertTrue(backendLogback.contains("value=\"\${BACKEND_LOG_DIR:-backend/logs}\""))
        assertTrue(backendLogback.contains("<file>\${BACKEND_LOG_DIR}/backend.log</file>"))
        assertTrue(
            backendLogback.contains(
                "<fileNamePattern>\${BACKEND_LOG_DIR}/archive/backend.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>"
            )
        )

        assertTrue(botLogback.contains("RollingFileAppender"))
        assertTrue(botLogback.contains("SizeAndTimeBasedRollingPolicy"))
        assertTrue(botLogback.contains("value=\"\${BOT_LOG_DIR:-bot/logs}\""))
        assertTrue(botLogback.contains("<file>\${BOT_LOG_DIR}/bot.log</file>"))
        assertTrue(
            botLogback.contains(
                "<fileNamePattern>\${BOT_LOG_DIR}/archive/bot.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>"
            )
        )
    }

    private fun readRepoFile(relativePath: String): String {
        val workingDir = Path.of("").toAbsolutePath().normalize()
        val candidates =
            sequenceOf(workingDir, workingDir.parent, workingDir.parent?.parent)
                .filterNotNull()
                .map { it.resolve(relativePath).normalize() }

        val file =
            candidates.firstOrNull(Files::exists)
                ?: error("Could not find '$relativePath' from working dir '$workingDir'")

        return Files.readString(file)
    }
}
