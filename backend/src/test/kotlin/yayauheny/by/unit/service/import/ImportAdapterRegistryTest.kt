package yayauheny.by.unit.importing.service

import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import yayauheny.by.importing.exception.UnsupportedImportProvider
import yayauheny.by.importing.provider.ImportSourceAdapter
import yayauheny.by.importing.service.ImportAdapterRegistry
import yayauheny.by.model.enums.ImportProvider

@DisplayName("ImportAdapterRegistry Tests")
class ImportAdapterRegistryTest {
    @Test
    @DisplayName("get returns adapter for known provider")
    fun get_returns_adapter_for_known_provider() {
        val twoGisAdapter = mockk<ImportSourceAdapter>()
        every { twoGisAdapter.provider } returns ImportProvider.TWO_GIS
        val registry = ImportAdapterRegistry(listOf(twoGisAdapter))

        val result = registry.get(ImportProvider.TWO_GIS)

        assertEquals(twoGisAdapter, result)
    }

    @Test
    @DisplayName("get throws UnsupportedImportProvider for unknown provider")
    fun get_throws_for_unknown_provider() {
        val twoGisAdapter = mockk<ImportSourceAdapter>()
        every { twoGisAdapter.provider } returns ImportProvider.TWO_GIS
        val registry = ImportAdapterRegistry(listOf(twoGisAdapter))

        val e =
            assertFailsWith<UnsupportedImportProvider> {
                registry.get(ImportProvider.YANDEX_MAPS)
            }
        assertEquals("Unsupported import provider: YANDEX_MAPS", e.message)
    }
}
