package by.yayauheny.tutochkatgbot.cache;

import java.util.Objects;

/**
 * Immutable key for geo-based cache entries.
 */
public final class GeoKey {
    private final double latitude;
    private final double longitude;

    public GeoKey(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double latitude() {
        return latitude;
    }

    public double longitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeoKey geoKey = (GeoKey) o;
        return Double.compare(geoKey.latitude, latitude) == 0
            && Double.compare(geoKey.longitude, longitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @Override
    public String toString() {
        return "GeoKey{" +
            "latitude=" + latitude +
            ", longitude=" + longitude +
            '}';
    }
}
