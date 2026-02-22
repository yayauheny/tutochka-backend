package yayauheny.by.model.enums

/**
 * Провайдер импорта данных.
 */
enum class ImportProvider {
    TWO_GIS,
    YANDEX_MAPS,
    GOOGLE_MAPS,
    OSM,
    USER,
    MANUAL;

    companion object {
        fun fromPayloadType(payloadType: ImportPayloadType): ImportProvider =
            when (payloadType) {
                ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON -> TWO_GIS
                ImportPayloadType.YANDEX_MAPS_SCRAPED_PLACE_JSON -> YANDEX_MAPS
                ImportPayloadType.MANUAL -> MANUAL
            }
    }
}
