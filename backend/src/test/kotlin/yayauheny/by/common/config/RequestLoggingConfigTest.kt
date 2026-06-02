package yayauheny.by.common.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils

class RequestLoggingConfigTest {
    private val config = RequestLoggingConfig()

    @Test
    fun `request logging filter excludes query string and payload`() {
        val filter = config.requestLoggingFilter()

        assertThat(ReflectionTestUtils.getField(filter, "includeClientInfo")).isEqualTo(true)
        assertThat(ReflectionTestUtils.getField(filter, "includeQueryString")).isEqualTo(false)
        assertThat(ReflectionTestUtils.getField(filter, "includeHeaders")).isEqualTo(false)
        assertThat(ReflectionTestUtils.getField(filter, "includePayload")).isEqualTo(false)
    }
}
