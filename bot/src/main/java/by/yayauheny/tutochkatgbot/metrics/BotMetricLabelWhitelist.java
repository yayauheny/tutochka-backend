package by.yayauheny.tutochkatgbot.metrics;

import java.util.Set;
import java.util.Locale;

public final class BotMetricLabelWhitelist {

    private static final String UNKNOWN = "unknown";
    private static final Set<String> UPDATE_TYPES = Set.of("message", "callback", "command", "location", "other");
    private static final Set<String> OUTCOMES = Set.of("success", "4xx", "5xx", "error");
    private static final Set<String> CACHE_OUTCOMES = Set.of("hit", "miss", "expired");
    private static final Set<String> CLIENT_TYPES = Set.of("telegram_bot", "telegram_miniapp", "api");
    private static final Set<String> ENDPOINTS = Set.of("/restrooms/nearest", "/restrooms/{id}", UNKNOWN);

    private BotMetricLabelWhitelist() {}

    public static String normalizeUpdateType(String type) {
        return normalize(type, UPDATE_TYPES, "other");
    }

    public static String normalizeOutcome(String outcome) {
        return normalize(outcome, OUTCOMES, "error");
    }

    public static String normalizeCacheOutcome(String outcome) {
        return normalize(outcome, CACHE_OUTCOMES, "miss");
    }

    public static String normalizeClientType(String clientType) {
        return normalize(clientType, CLIENT_TYPES, "telegram_bot");
    }

    public static String normalizeEndpoint(String endpoint) {
        return normalize(endpoint, ENDPOINTS, UNKNOWN);
    }

    private static String normalize(String value, Set<String> allowed, String fallback) {
        if (value == null) {
            return fallback;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return allowed.contains(normalized) ? normalized : fallback;
    }
}
