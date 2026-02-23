package yayauheny.by.unit.service.import

import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.service.import.ImportStrategy
import yayauheny.by.service.import.ImportStrategyRegistry
import yayauheny.by.service.import.UnsupportedImportProvider

@DisplayName("ImportStrategyRegistry Tests")
class ImportStrategyRegistryTest {
    @Test
    @DisplayName("get returns strategy for known provider")
    fun get_returns_strategy_for_known_provider() =
        runTest {
            val twoGisStrategy = mockk<ImportStrategy>()
            every { twoGisStrategy.provider() } returns ImportProvider.TWO_GIS
            val registry = ImportStrategyRegistry(listOf(twoGisStrategy))

            val result = registry.get(ImportProvider.TWO_GIS)

            assertEquals(twoGisStrategy, result)
        }

    @Test
    @DisplayName("get throws UnsupportedImportProvider for unknown provider")
    fun get_throws_for_unknown_provider() {
        val twoGisStrategy = mockk<ImportStrategy>()
        every { twoGisStrategy.provider() } returns ImportProvider.TWO_GIS
        val registry = ImportStrategyRegistry(listOf(twoGisStrategy))

        val e =
            assertFailsWith<UnsupportedImportProvider> {
                registry.get(ImportProvider.YANDEX_MAPS)
            }
        assertEquals("Unsupported import provider: YANDEX_MAPS", e.message)
    }
}
