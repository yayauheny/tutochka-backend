package yayauheny.by.unit.di

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import yayauheny.by.importing.provider.ImportSourceAdapter
import yayauheny.by.importing.provider.osm.OsmImportAdapter
import yayauheny.by.importing.provider.twogis.TwoGisImportAdapter
import yayauheny.by.importing.provider.yandex.YandexImportAdapter
import yayauheny.by.importing.service.ImportAdapterRegistry
import yayauheny.by.model.enums.ImportProvider

@SpringJUnitConfig(classes = [ImportingModuleTest.Config::class])
class ImportingModuleTest {
    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Test
    fun given_application_context_when_resolving_import_registry_then_all_supported_adapters_are_registered() {
        val osmAdapter = applicationContext.getBean(OsmImportAdapter::class.java)
        val twoGisAdapter = applicationContext.getBean(TwoGisImportAdapter::class.java)
        val yandexAdapter = applicationContext.getBean(YandexImportAdapter::class.java)
        val registry = applicationContext.getBean(ImportAdapterRegistry::class.java)
        val adapters = applicationContext.getBeansOfType(ImportSourceAdapter::class.java).values.toList()

        assertThat(adapters).containsExactlyInAnyOrder(osmAdapter, twoGisAdapter, yandexAdapter)
        assertThat(osmAdapter.provider).isEqualTo(ImportProvider.OSM)
        assertThat(twoGisAdapter.provider).isEqualTo(ImportProvider.TWO_GIS)
        assertThat(yandexAdapter.provider).isEqualTo(ImportProvider.YANDEX_MAPS)
        assertThat(registry.get(ImportProvider.OSM)).isSameAs(osmAdapter)
        assertThat(registry.get(ImportProvider.TWO_GIS)).isSameAs(twoGisAdapter)
        assertThat(registry.get(ImportProvider.YANDEX_MAPS)).isSameAs(yandexAdapter)
    }

    @TestConfiguration
    class Config {
        @Bean
        fun osmImportAdapter(): OsmImportAdapter = OsmImportAdapter()

        @Bean
        fun twoGisImportAdapter(): TwoGisImportAdapter = TwoGisImportAdapter()

        @Bean
        fun yandexImportAdapter(): YandexImportAdapter = YandexImportAdapter()

        @Bean
        fun importAdapterRegistry(adapters: List<ImportSourceAdapter>): ImportAdapterRegistry = ImportAdapterRegistry(adapters)
    }
}
