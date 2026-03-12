package yayauheny.by.service.import

import yayauheny.by.model.enums.ImportProvider

class ImportStrategyRegistry(
    strategies: List<ImportStrategy>
) {
    private val byProvider = strategies.associateBy { it.provider() }

    fun get(provider: ImportProvider): ImportStrategy = byProvider[provider] ?: throw UnsupportedImportProvider(provider.name)
}
