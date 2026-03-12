package by.yayauheny.tutochkatgbot.session;

import java.time.Instant;

/**
 * User session data
 */
public record UserSession(
    Location location,
    Integer radius,
    Instant updatedAt
) {
    
    /**
     * Location data
     */
    public record Location(
        double latitude,
        double longitude
    ) {}
    
    /**
     * Create new session with location
     */
    public static UserSession withLocation(Location location, Integer radius) {
        return new UserSession(location, radius, Instant.now());
    }
    
    /**
     * Update session with new location
     */
    public UserSession updateLocation(Location newLocation) {
        return new UserSession(newLocation, this.radius, Instant.now());
    }
    
    /**
     * Update session with new radius
     */
    public UserSession updateRadius(Integer newRadius) {
        return new UserSession(this.location, newRadius, Instant.now());
    }
}
