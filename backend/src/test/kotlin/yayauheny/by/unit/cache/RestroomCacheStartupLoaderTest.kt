package yayauheny.by.unit.cache

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import yayauheny.by.cache.RestroomCacheProperties
import yayauheny.by.cache.RestroomCacheService
import yayauheny.by.cache.RestroomCacheStartupLoader
import yayauheny.by.cache.nearest.NearestRestroomCacheRow
import yayauheny.by.model.enums.FeeType
import yayauheny.by.repository.RestroomJpaRepository
import java.util.UUID

@DisplayName("RestroomCacheStartupLoader Tests")
class RestroomCacheStartupLoaderTest {
    private val restroomRepository = mockk<RestroomJpaRepository>()
    private val cacheService = mockk<RestroomCacheService>(relaxed = true)

    @Test
    fun `warmUp rebuilds nearest cache from paged projection rows`() {
        val row = mockk<NearestRestroomCacheRow>()
        every { row.id } returns UUID.randomUUID()
        every { row.lat } returns 53.9
        every { row.lon } returns 27.56
        every { row.displayName } returns "Projection restroom"
        every { row.feeType } returns FeeType.FREE.name
        every { restroomRepository.findActiveNearestCacheRows(any()) } returns PageImpl(listOf(row))

        val loader =
            RestroomCacheStartupLoader(
                properties = RestroomCacheProperties(),
                restroomRepository = restroomRepository,
                restroomCacheService = cacheService
            )

        loader.warmUp()

        verify {
            cacheService.rebuildNearestCache(
                match { items ->
                    items.single().displayName == "Projection restroom" &&
                        items.single().feeType == FeeType.FREE
                }
            )
        }
    }

    @Test
    fun `warmUp skips rebuild when startup load mode is lazy`() {
        val loader =
            RestroomCacheStartupLoader(
                properties = RestroomCacheProperties(nearest = RestroomCacheProperties.Nearest(startupLoadMode = RestroomCacheProperties.StartupLoadMode.LAZY)),
                restroomRepository = restroomRepository,
                restroomCacheService = cacheService
            )

        loader.warmUp()

        verify(exactly = 0) {
            restroomRepository.findActiveNearestCacheRows(any())
        }
    }
}
