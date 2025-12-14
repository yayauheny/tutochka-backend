package by.yayauheny.tutochkatgbot.cache;

import java.time.Instant;

/**
 * Represents last known location of a user
 */
public record LastLocation(
    double latitude,
    double longitude,
    Instant timestamp,
    String source
) {
    public LastLocation {
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("Source cannot be null or blank");
        }
    }
}


