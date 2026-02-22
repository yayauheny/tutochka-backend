package by.yayauheny.tutochkatgbot.cache;

import java.util.Optional;

/**
 * Service for caching last known user locations
 */
public interface LastLocationCacheService {
    /**
     * Get last known location for a chat
     * @param chatId chat ID
     * @return last location if exists
     */
    Optional<LastLocation> getLastLocation(long chatId);

    /**
     * Save last known location for a chat
     * @param chatId chat ID
     * @param latitude latitude
     * @param longitude longitude
     * @param source location source (e.g., "location", "venue")
     */
    void putLastLocation(long chatId, double latitude, double longitude, String source);

    /**
     * Evict last location for a chat
     * @param chatId chat ID
     */
    void evictLastLocation(long chatId);

    /**
     * Evict all cached locations
     */
    void evictAll();
}



