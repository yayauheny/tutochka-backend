package yayauheny.by.importing.provider.osm

data class OsmScrapedLocation(
    val lat: Double,
    val lng: Double
)

data class OsmScrapedToilet(
    val nodeId: String,
    val tags: Map<String, String>,
    val location: OsmScrapedLocation,
    val sourceUrl: String? = null,
    val country: String? = null,
    val city: String? = null
)
