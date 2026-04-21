package yayauheny.by.model.import.yandex

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Модели для парсинга scraped JSON формата Яндекс.Карт.
 * Основаны на data set'ах вида dataset_yandex-maps-places-scraper_*.json.
 */
@Serializable
data class YandexMapsScrapedLocation(
    val lat: Double,
    val lng: Double
)

@Serializable
data class YandexMapsScrapedWorkingHour(
    val day: String,
    val from: String,
    val to: String
)

@Serializable
data class YandexMapsScrapedPlace(
    val title: String,
    val shortTitle: String? = null,
    val categoryName: String? = null,
    val categories: List<String> = emptyList(),
    val address: String? = null,
    val state: String? = null,
    val country: String? = null,
    val website: String? = null,
    val urls: List<String> = emptyList(),
    val phone: String? = null,
    val phoneUnformatted: String? = null,
    val location: YandexMapsScrapedLocation,
    val totalScore: Double? = null,
    val ratingCount: Int? = null,
    val reviewsCount: Int? = null,
    val reviewCrop: String? = null,
    val reviewText: String? = null,
    val statusText: String? = null,
    val isOpenNow: Boolean? = null,
    @SerialName("placeId") val placeId: String,
    val yandexUri: String? = null,
    val geoId: String? = null,
    val url: String? = null,
    val searchString: String? = null,
    val openingHoursText: String? = null,
    @SerialName("workingHours") val workingHours: List<YandexMapsScrapedWorkingHour>? = null,
    val features: List<String> = emptyList(),
    val socialLinks: List<String> = emptyList(),
    val metro: List<String> = emptyList(),
    val metroDistanceM: List<Int> = emptyList(),
    val stops: List<String> = emptyList(),
    val photos: List<String> = emptyList(),
    val photosCount: Int? = null,
    val scrapedAt: String? = null,
    /**
     * Поле для хранения произвольных дополнительных данных (если скрапер будет расширять формат).
     */
    val extra: JsonObject? = null
)
