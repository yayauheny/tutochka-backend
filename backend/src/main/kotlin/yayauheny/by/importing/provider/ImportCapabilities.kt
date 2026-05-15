package yayauheny.by.importing.provider

import yayauheny.by.importing.exception.UnsupportedPayloadType
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.model.enums.ImportProvider

object ImportCapabilities {
    private val allowed: Map<ImportProvider, Set<ImportPayloadType>> =
        mapOf(
            ImportProvider.TWO_GIS to setOf(ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON),
            ImportProvider.YANDEX_MAPS to setOf(ImportPayloadType.YANDEX_MAPS_SCRAPED_PLACE_JSON)
        )

    fun requireSupported(
        provider: ImportProvider,
        payloadType: ImportPayloadType
    ) {
        val ok = allowed[provider]?.contains(payloadType) == true
        if (!ok) throw UnsupportedPayloadType(provider, payloadType)
    }
}
