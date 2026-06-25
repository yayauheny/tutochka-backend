package yayauheny.by.unit.cache

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import yayauheny.by.cache.RestroomCacheProperties
import yayauheny.by.cache.RestroomCacheServiceImpl
import yayauheny.by.cache.detail.RestroomDetailCacheStore
import yayauheny.by.cache.nearest.NearestRestroomCacheItem
import yayauheny.by.cache.nearest.NearestRestroomCacheStore
import yayauheny.by.helpers.TestDataHelpers
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.model.restroom.RestroomResponseDto
import java.util.UUID

@DisplayName("RestroomCacheServiceImpl Tests")
class RestroomCacheServiceImplTest {
    private val nearestCacheStore = mockk<NearestRestroomCacheStore>()
    private val detailCacheStore = mockk<RestroomDetailCacheStore>()
    private val properties = RestroomCacheProperties()
    private val service = RestroomCacheServiceImpl(properties, nearestCacheStore, detailCacheStore)

    @Test
    fun `findNearestRestrooms returns cached values without database fallback`() {
        val queryLat = 53.9
        val queryLon = 27.56
        val item =
            NearestRestroomCacheItem(
                id = UUID.randomUUID(),
                lat = 53.901,
                lon = 27.561,
                displayName = "Cached restroom",
                feeType = FeeType.FREE
            )
        every {
            nearestCacheStore.read(
                latitude = queryLat,
                longitude = queryLon,
                distanceMeters = 5000,
                candidateLimit = 50
            )
        } returns listOf(item)

        val result =
            service.findNearestRestrooms(
                latitude = queryLat,
                longitude = queryLon,
                limit = 5,
                distanceMeters = 5000
            ) {
                error("database loader must not be called")
            }

        assertThat(result).hasSize(1)
        assertThat(result.single().id).isEqualTo(item.id)
        assertThat(result.single().displayName).isEqualTo("Cached restroom")
        assertThat(result.single().feeType).isEqualTo(FeeType.FREE)
        verify {
            nearestCacheStore.read(
                latitude = queryLat,
                longitude = queryLon,
                distanceMeters = 5000,
                candidateLimit = 50
            )
        }
    }

    @Test
    fun `findNearestRestrooms falls back to database and stores loaded cache items`() {
        val restroom =
            TestDataHelpers.createNearestRestroomSlimDto(
                id = UUID.randomUUID(),
                displayName = "Database restroom",
                feeType = FeeType.PAID,
                lat = 53.902,
                lon = 27.562,
                userLat = 53.9,
                userLon = 27.56,
                distanceMeters = 120.0
            )
        every {
            nearestCacheStore.read(
                latitude = 53.9,
                longitude = 27.56,
                distanceMeters = 5000,
                candidateLimit = 50
            )
        } returns emptyList()
        val savedItems = slot<Collection<NearestRestroomCacheItem>>()
        every { nearestCacheStore.save(capture(savedItems)) } returns Unit

        val result =
            service.findNearestRestrooms(
                latitude = 53.9,
                longitude = 27.56,
                limit = 5,
                distanceMeters = 5000
            ) {
                listOf(restroom)
            }

        assertThat(result).containsExactly(restroom)
        val savedItem = savedItems.captured.single()
        assertThat(savedItem.id).isEqualTo(restroom.id)
        verify { nearestCacheStore.save(any()) }
    }

    @Test
    fun `getRestroomById returns cached detail when available`() {
        val restroom = TestDataHelpers.createRestroomResponseDto()
        every { detailCacheStore.get(restroom.id) } returns restroom

        val result = service.getRestroomById(restroom.id) {
            error("database loader must not be called")
        }

        assertThat(result).isEqualTo(restroom)
        verify { detailCacheStore.get(restroom.id) }
    }

    @Test
    fun `refreshRestroom stores active restroom and removes inactive restroom from nearest cache`() {
        val active = TestDataHelpers.createRestroomResponseDto()
        val inactive = TestDataHelpers.createRestroomResponseDto(status = RestroomStatus.INACTIVE, isHidden = true)

        service.refreshRestroom(active)
        service.refreshRestroom(inactive)

        verify {
            detailCacheStore.put(active)
            nearestCacheStore.save(match { items -> items.any { it.id == active.id } })
            detailCacheStore.put(inactive)
            nearestCacheStore.remove(inactive.id)
        }
    }

    @Test
    fun `removeRestroom removes detail and nearest cache entries`() {
        val restroomId = UUID.randomUUID()

        service.removeRestroom(restroomId)

        verify {
            detailCacheStore.remove(restroomId)
            nearestCacheStore.remove(restroomId)
        }
    }
}
