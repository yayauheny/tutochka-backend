package by.yayauheny.tutochkatgbot.util;

/**
 * Utility class for formatting distances
 */
public final class DistanceFormat {
    
    private DistanceFormat() {}
    
    /**
     * Format distance in meters to human-readable string
     * @param meters distance in meters
     * @return formatted distance string
     */
    public static String meters(double meters) {
        if (meters < 1000) {
            return String.format("%.0f м", meters);
        } else {
            double kilometers = meters / 1000.0;
            if (kilometers < 10) {
                return String.format("%.1f км", kilometers);
            } else {
                return String.format("%.0f км", kilometers);
            }
        }
    }
    
    /**
     * Format distance in meters to short string
     * @param meters distance in meters
     * @return short formatted distance string
     */
    public static String shortFormat(double meters) {
        if (meters < 1000) {
            return String.format("%.0fм", meters);
        } else {
            double kilometers = meters / 1000.0;
            return String.format("%.1fкм", kilometers);
        }
    }
}
