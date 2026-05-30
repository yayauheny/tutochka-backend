package yayauheny.by.importing.provider.yandex

import com.fasterxml.jackson.annotation.JsonProperty

data class YandexMapsScrapedLocation(
    val lat: Double,
    val lng: Double
)

data class YandexMapsScrapedWorkingInterval(
    val from: String,
    val to: String
)

data class YandexMapsScrapedWorkingHour(
    val day: String,
    val from: String,
    val to: String,
    val intervals: List<YandexMapsScrapedWorkingInterval> = emptyList()
)

data class YandexMapsScrapedPlace(
    val title: String? = null,
    val shortTitle: String? = null,
    val address: String? = null,
    val location: YandexMapsScrapedLocation,
    val statusText: String? = null,
    val isOpenNow: Boolean? = null,
    @JsonProperty("placeId") val placeId: String,
    @JsonProperty("workingHours") val workingHours: List<YandexMapsScrapedWorkingHour>? = null,
    val features: List<String> = emptyList(),
)
