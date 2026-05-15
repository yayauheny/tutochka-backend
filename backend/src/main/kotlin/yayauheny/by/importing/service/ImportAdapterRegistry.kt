package yayauheny.by.importing.service

import yayauheny.by.importing.exception.UnsupportedImportProvider
import yayauheny.by.importing.provider.ImportSourceAdapter
import yayauheny.by.model.enums.ImportProvider

class ImportAdapterRegistry(
    adapters: List<ImportSourceAdapter>
) {
    private val byProvider = adapters.associateBy { it.provider }

    fun get(provider: ImportProvider): ImportSourceAdapter = byProvider[provider] ?: throw UnsupportedImportProvider(provider.name)
}
