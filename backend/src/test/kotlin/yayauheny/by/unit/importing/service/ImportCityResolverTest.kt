package yayauheny.by.unit.importing.service

import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import yayauheny.by.importing.service.ImportCityResolver

class ImportCityResolverTest {
    private val resolver = ImportCityResolver()

    @Test
    fun `resolveByMetadata currently returns nearest fallback placeholder`() {
        val result = resolver.resolveByMetadata("Belarus", "Minsk")

        assertThat(result)
            .isEqualTo(ImportCityResolver.MetadataCityResolution.NeedsNearestFallback(null, "City resolution is unavailable"))
    }

    @Test
    fun `resolveNearest currently returns null placeholder`() {
        val result = resolver.resolveNearest(53.9, 27.56, UUID.randomUUID())

        assertThat(result).isNull()
    }
}
