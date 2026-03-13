package by.yayauheny.tutochkatgbot.util;

import java.util.Locale;

public final class Links {
    
    private Links() {}
    
    public static String googleMaps(double lat, double lon) {
        return String.format(Locale.US, "https://www.google.com/maps?q=%.6f,%.6f", lat, lon);
    }
    
    public static String yandexMaps(double lat, double lon) {
        return String.format(Locale.US, "https://yandex.ru/maps/?pt=%.6f,%.6f&z=16&l=map", lon, lat);
    }
    
    public static String appleMaps(double lat, double lon) {
        return String.format(Locale.US, "https://maps.apple.com/?q=%.6f,%.6f", lat, lon);
    }

    public static String twoGis(double lat, double lon) {
        return String.format(Locale.US, "https://2gis.ru/?m=%.6f,%.6f/zoom/16", lon, lat);
    }

    public static String twoGisById(String branchId) {
        return "https://2gis.ru/geo/" + branchId;
    }
    
    public static String getDefaultMapsLink(double lat, double lon) {
        return yandexMaps(lat, lon);
    }
}