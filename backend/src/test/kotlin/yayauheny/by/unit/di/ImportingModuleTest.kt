package yayauheny.by.unit.di

import org.jooq.DSLContext
import org.junit.jupiter.api.Test
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import yayauheny.by.di.importingModule
import yayauheny.by.importing.provider.twogis.TwoGisImportAdapter
import yayauheny.by.importing.provider.yandex.YandexImportAdapter
import yayauheny.by.importing.service.ImportAdapterRegistry
import yayauheny.by.repository.CityRepository
import yayauheny.by.service.import.schedule.ScheduleMappingService
import yayauheny.by.model.enums.ImportProvider
import io.mockk.mockk
import kotlin.test.assertEquals

class ImportingModuleTest {
    @Test
    fun given_importing_module_when_resolving_registry_then_all_supported_adapters_are_registered() {
        val app =
            koinApplication {
                modules(
                    module {
                        single<DSLContext> { mockk(relaxed = true) }
                        single<CityRepository> { mockk(relaxed = true) }
                        single<ScheduleMappingService> { mockk(relaxed = true) }
                    },
                    importingModule
                )
            }

        try {
            val twoGisAdapter = app.koin.get<TwoGisImportAdapter>()
            val yandexAdapter = app.koin.get<YandexImportAdapter>()
            val registry = app.koin.get<ImportAdapterRegistry>()

            assertEquals(ImportProvider.TWO_GIS, twoGisAdapter.provider)
            assertEquals(ImportProvider.YANDEX_MAPS, yandexAdapter.provider)
            assertEquals(twoGisAdapter, registry.get(ImportProvider.TWO_GIS))
            assertEquals(yandexAdapter, registry.get(ImportProvider.YANDEX_MAPS))
        } finally {
            app.close()
        }
    }
}
