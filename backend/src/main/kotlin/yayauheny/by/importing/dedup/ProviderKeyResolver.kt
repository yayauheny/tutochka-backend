package yayauheny.by.importing.dedup

import yayauheny.by.model.enums.ImportProvider

object ProviderKeyResolver {
    fun providerKey(provider: ImportProvider): String? =
        when (provider) {
            ImportProvider.TWO_GIS -> "2gis"
            ImportProvider.YANDEX_MAPS -> "yandex"
            ImportProvider.GOOGLE_MAPS -> "google"
            ImportProvider.OSM -> "osm"
            ImportProvider.USER,
            ImportProvider.MANUAL -> null
        }
}
