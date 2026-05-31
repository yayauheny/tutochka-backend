package yayauheny.by.bot.util

import java.util.Locale

object Links {
    fun googleMaps(lat: Double, lon: Double): String {
        return String.format(Locale.US, "https://www.google.com/maps?q=%.6f,%.6f", lat, lon)
    }

    fun yandexMaps(lat: Double, lon: Double): String {
        return String.format(Locale.US, "https://yandex.ru/maps/?pt=%.6f,%.6f&z=16&l=map", lon, lat)
    }

    fun appleMaps(lat: Double, lon: Double): String {
        return String.format(Locale.US, "https://maps.apple.com/?q=%.6f,%.6f", lat, lon)
    }

    fun twoGis(lat: Double, lon: Double): String {
        return String.format(Locale.US, "https://2gis.ru/geo/%.5f,%.5f", lon, lat)
    }

    fun twoGisById(branchId: String): String {
        return String.format("https://2gis.ru/firm/%s", branchId)
    }

    fun getDefaultMapsLink(lat: Double, lon: Double): String {
        return yandexMaps(lat, lon)
    }
}
