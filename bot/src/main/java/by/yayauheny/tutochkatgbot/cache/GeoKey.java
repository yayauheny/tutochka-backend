package by.yayauheny.tutochkatgbot.cache;

import java.util.Objects;

/**
 * Immutable key for geo-based cache entries.
 */
public final class GeoKey {
    private final double latitude;
    private final double longitude;
    private final int radius;
    private final int limit;

    public GeoKey(double latitude, double longitude, int radius, int limit) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.limit = limit;
    }

    public double latitude() {
        return latitude;
    }

    public double longitude() {
        return longitude;
    }

    public int radius() {
        return radius;
    }

    public int limit() {
        return limit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeoKey geoKey = (GeoKey) o;
        return Double.compare(geoKey.latitude, latitude) == 0
            && Double.compare(geoKey.longitude, longitude) == 0
            && radius == geoKey.radius
            && limit == geoKey.limit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude, radius, limit);
    }

    @Override
    public String toString() {
        return "GeoKey{" +
            "latitude=" + latitude +
            ", longitude=" + longitude +
            ", radius=" + radius +
            ", limit=" + limit +
            '}';
    }
}
