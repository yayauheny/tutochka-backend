package yayauheny.by.unit.metrics

import kotlin.test.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import yayauheny.by.metrics.MetricLabelWhitelist

class MetricLabelWhitelistTest {
    @ParameterizedTest
    @CsvSource(
        value = [
            "telegram_bot,telegram_bot",
            "TELEGRAM_BOT,telegram_bot",
            " Telegram_Bot ,telegram_bot",
            "mobile_app,api",
            "null,api"
        ],
        nullValues = ["null"]
    )
    fun `client type normalization should be case insensitive and safe`(
        input: String?,
        expected: String
    ) {
        assertEquals(expected, MetricLabelWhitelist.clientTypeOrDefault(input))
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "validation,validation",
            "VALIDATION,validation",
            " Validation ,validation",
            "bad_reason,internal",
            "null,internal"
        ],
        nullValues = ["null"]
    )
    fun `failure reason normalization should be case insensitive and safe`(
        input: String?,
        expected: String
    ) {
        assertEquals(expected, MetricLabelWhitelist.failureReasonOrDefault(input))
    }
}
