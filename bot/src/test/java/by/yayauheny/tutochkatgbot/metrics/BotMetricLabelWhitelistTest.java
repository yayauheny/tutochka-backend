package by.yayauheny.tutochkatgbot.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BotMetricLabelWhitelistTest {

    @ParameterizedTest
    @CsvSource(
        value = {
            "message,message",
            "CALLBACK,callback",
            " Command ,command",
            "unknown-type,other",
            "null,other"
        },
        nullValues = {"null"}
    )
    void normalizeUpdateType_shouldBeCaseInsensitiveAndSafe(String input, String expected) {
        assertEquals(expected, BotMetricLabelWhitelist.normalizeUpdateType(input));
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "success,success",
            "4XX,4xx",
            " 5xx ,5xx",
            "bad,error",
            "null,error"
        },
        nullValues = {"null"}
    )
    void normalizeOutcome_shouldBeCaseInsensitiveAndSafe(String input, String expected) {
        assertEquals(expected, BotMetricLabelWhitelist.normalizeOutcome(input));
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "google,google",
            "YANDEX,yandex",
            " 2GiS ,2gis",
            "mystery,unknown",
            "null,unknown"
        },
        nullValues = {"null"}
    )
    void normalizeProvider_shouldBeCaseInsensitiveAndSafe(String input, String expected) {
        assertEquals(expected, BotMetricLabelWhitelist.normalizeProvider(input));
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "telegram_bot,telegram_bot",
            "TELEGRAM_MINIAPP,telegram_miniapp",
            " Api ,api",
            "desktop,telegram_bot",
            "null,telegram_bot"
        },
        nullValues = {"null"}
    )
    void normalizeClientType_shouldBeCaseInsensitiveAndSafe(String input, String expected) {
        assertEquals(expected, BotMetricLabelWhitelist.normalizeClientType(input));
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "/restrooms/nearest,/restrooms/nearest",
            "/RESTROOMS/{ID},/restrooms/{id}",
            " /Restrooms/Nearest ,/restrooms/nearest",
            "/restrooms/custom,unknown",
            "null,unknown"
        },
        nullValues = {"null"}
    )
    void normalizeEndpoint_shouldBeCaseInsensitiveAndSafe(String input, String expected) {
        assertEquals(expected, BotMetricLabelWhitelist.normalizeEndpoint(input));
    }
}
