package yayauheny.by.unit.cache

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import yayauheny.by.cache.RestroomCacheProperties
import yayauheny.by.cache.RestroomCacheService
import yayauheny.by.cache.RestroomCacheStartupLoader
import yayauheny.by.cache.nearest.NearestRestroomCacheItem
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
        val version = "build-1"
        val row1 = row("Projection restroom 1", FeeType.FREE.name)
        val row2 = row("Projection restroom 2", FeeType.PAID.name)
        val appended = mutableListOf<List<NearestRestroomCacheItem>>()
        every { cacheService.beginRebuild() } returns version
        every { restroomRepository.findActiveNearestCacheRows(any()) } returnsMany listOf(
            PageImpl(listOf(row1), PageRequest.of(0, 1), 2),
            PageImpl(listOf(row2), PageRequest.of(1, 1), 2)
        )
        every { cacheService.appendRebuild(version, any()) } answers {
            appended += secondArg<Collection<NearestRestroomCacheItem>>().toList()
        }
        every { cacheService.finishRebuild(version, 2L) } answers { Unit }

        val loader =
            RestroomCacheStartupLoader(
                properties = RestroomCacheProperties(),
                restroomRepository = restroomRepository,
                restroomCacheService = cacheService
            )

        loader.warmUp()

        verify {
            cacheService.beginRebuild()
            cacheService.appendRebuild(version, any())
            cacheService.appendRebuild(version, any())
            cacheService.finishRebuild(version, 2L)
        }
        assert(appended.map { it.single().displayName } == listOf("Projection restroom 1", "Projection restroom 2"))
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

    private fun row(displayName: String, feeType: String): NearestRestroomCacheRow {
        val row = mockk<NearestRestroomCacheRow>()
        every { row.id } returns UUID.randomUUID()
        every { row.lat } returns 53.9
        every { row.lon } returns 27.56
        every { row.displayName } returns displayName
        every { row.feeType } returns feeType
        return row
    }
}
