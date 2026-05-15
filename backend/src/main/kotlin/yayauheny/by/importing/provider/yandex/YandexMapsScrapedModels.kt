package yayauheny.by.importing.provider.yandex

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class YandexMapsScrapedLocation(
    val lat: Double,
    val lng: Double
)

@Serializable
data class YandexMapsScrapedWorkingInterval(
    val from: String,
    val to: String
)

@Serializable
data class YandexMapsScrapedWorkingHour(
    val day: String,
    val from: String,
    val to: String,
    val intervals: List<YandexMapsScrapedWorkingInterval> = emptyList()
)

@Serializable
data class YandexMapsScrapedPlace(
    val title: String? = null,
    val shortTitle: String? = null,
    val address: String? = null,
    val location: YandexMapsScrapedLocation,
    val statusText: String? = null,
    val isOpenNow: Boolean? = null,
    @SerialName("placeId") val placeId: String,
    @SerialName("workingHours") val workingHours: List<YandexMapsScrapedWorkingHour>? = null,
    val features: List<String> = emptyList(),
)
