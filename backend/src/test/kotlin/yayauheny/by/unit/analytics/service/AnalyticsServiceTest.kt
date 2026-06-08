package yayauheny.by.unit.analytics.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import jakarta.persistence.EntityManager
import jakarta.persistence.Query
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import yayauheny.by.analytics.entity.AnalyticsEventEntity
import yayauheny.by.analytics.entity.UserEntity
import yayauheny.by.analytics.repository.AnalyticsEventJpaRepository
import yayauheny.by.analytics.repository.UserAnalyticsJpaRepository
import yayauheny.by.analytics.repository.UserJpaRepository
import yayauheny.by.analytics.service.AnalyticsService
import yayauheny.by.model.analytics.AcquisitionTag
import yayauheny.by.model.analytics.AnalyticsEventCommand
import yayauheny.by.model.analytics.AnalyticsSource
import yayauheny.by.model.analytics.ProductAnalyticsEvent
import yayauheny.by.util.EncryptionService
import java.util.UUID

@DisplayName("AnalyticsService Tests")
class AnalyticsServiceTest {

    private val userJpaRepository = mockk<UserJpaRepository>()
    private val userAnalyticsJpaRepository = mockk<UserAnalyticsJpaRepository>()
    private val analyticsEventJpaRepository = mockk<AnalyticsEventJpaRepository>()
    private val encryptionService = mockk<EncryptionService>()
    private val entityManager = mockk<EntityManager>()
    private val query = mockk<Query>()

    private val analyticsService = AnalyticsService(
        userJpaRepository = userJpaRepository,
        userAnalyticsJpaRepository = userAnalyticsJpaRepository,
        analyticsEventJpaRepository = analyticsEventJpaRepository,
        encryptionService = encryptionService
    ).apply {
        // Use reflection to set the @PersistenceContext field if needed,
        // or just use ReflectionTestUtils in Spring, but manual is faster here.
        val field = AnalyticsService::class.java.getDeclaredField("entityManager")
        field.isAccessible = true
        field.set(this, entityManager)
    }

    @BeforeEach
    fun setup() {
        every { entityManager.createNativeQuery(any<String>()) } returns query
        every { query.setParameter(any<String>(), any()) } returns query
    }

    @Test
    fun `Telegram identity event encrypts IDs, upserts user, increments metrics, and saves event`() {
        val userId = UUID.randomUUID()
        val userEntity = UserEntity().apply { this.id = userId }
        val command = AnalyticsEventCommand(
            event = ProductAnalyticsEvent.NEAREST_RESTROOMS_RETURNED,
            tgUserId = 12345L,
            tgChatId = "chat-123",
            username = "test_user",
            source = AnalyticsSource.TELEGRAM_BOT,
            lat = 53.9006,
            lon = 27.5590,
            resultsCount = 5
        )

        every { encryptionService.encrypt("12345") } returns "encrypted_user"
        every { encryptionService.encrypt("chat-123") } returns "encrypted_chat"
        every { query.singleResult } returns userId
        every { userJpaRepository.getReferenceById(userId) } returns userEntity
        every { userAnalyticsJpaRepository.ensureUserAnalyticsRow(userId) } returns Unit
        every { userAnalyticsJpaRepository.incrementMetrics(any(), any(), any(), any(), any(), any(), any()) } returns Unit
        
        val eventSlot = slot<AnalyticsEventEntity>()
        every { analyticsEventJpaRepository.save(capture(eventSlot)) } answers { firstArg() }

        analyticsService.trackProductEvent(command)

        verify { encryptionService.encrypt("12345") }
        verify { encryptionService.encrypt("chat-123") }
        verify { query.setParameter("tgUserId", "encrypted_user") }
        verify { query.setParameter("tgChatId", "encrypted_chat") }
        verify { userAnalyticsJpaRepository.ensureUserAnalyticsRow(userId) }
        verify { userAnalyticsJpaRepository.incrementMetrics(userId, 1, 1, 0, 0, ProductAnalyticsEvent.NEAREST_RESTROOMS_RETURNED.value, any()) }
        
        val saved = eventSlot.captured
        assertThat(saved.event).isEqualTo(ProductAnalyticsEvent.NEAREST_RESTROOMS_RETURNED.value)
        assertThat(saved.user).isEqualTo(userEntity)
        assertThat(saved.location?.y).isEqualTo(command.lat)
        assertThat(saved.location?.x).isEqualTo(command.lon)
        assertThat(saved.resultsCount).isEqualTo(5)
        assertThat(saved.location?.srid).isEqualTo(4326)
    }

    @Test
    fun `Telegram identity event accepts string uuid from native query result`() {
        val userId = UUID.randomUUID()
        val userEntity = UserEntity().apply { this.id = userId }
        val command = AnalyticsEventCommand(
            event = ProductAnalyticsEvent.RESTROOM_DETAILS_OPENED,
            tgUserId = 12345L,
            tgChatId = "chat-123",
            username = "test_user",
            source = AnalyticsSource.TELEGRAM_BOT
        )

        every { encryptionService.encrypt("12345") } returns "encrypted_user"
        every { encryptionService.encrypt("chat-123") } returns "encrypted_chat"
        every { query.singleResult } returns userId.toString()
        every { userJpaRepository.getReferenceById(userId) } returns userEntity
        every { userAnalyticsJpaRepository.ensureUserAnalyticsRow(userId) } returns Unit
        every { userAnalyticsJpaRepository.incrementMetrics(any(), any(), any(), any(), any(), any(), any()) } returns Unit
        every { analyticsEventJpaRepository.save(any()) } answers { firstArg() }

        analyticsService.trackProductEvent(command)

        verify { userJpaRepository.getReferenceById(userId) }
        verify { userAnalyticsJpaRepository.incrementMetrics(userId, 0, 0, 0, 1, ProductAnalyticsEvent.RESTROOM_DETAILS_OPENED.value, any()) }
    }

    @Test
    fun `Anonymous API event does not upsert user but saves event`() {
        val command = AnalyticsEventCommand(
            event = ProductAnalyticsEvent.RESTROOM_DETAILS_OPENED,
            tgUserId = null,
            tgChatId = null,
            username = null,
            source = AnalyticsSource.API
        )

        val eventSlot = slot<AnalyticsEventEntity>()
        every { analyticsEventJpaRepository.save(capture(eventSlot)) } answers { firstArg() }

        analyticsService.trackProductEvent(command)

        verify(exactly = 0) { encryptionService.encrypt(any()) }
        verify(exactly = 0) { entityManager.createNativeQuery(any<String>()) }
        verify(exactly = 0) { userAnalyticsJpaRepository.incrementMetrics(any(), any(), any(), any(), any(), any(), any()) }

        val saved = eventSlot.captured
        assertThat(saved.event).isEqualTo(ProductAnalyticsEvent.RESTROOM_DETAILS_OPENED.value)
        assertThat(saved.user).isNull()
        assertThat(saved.source).isEqualTo(AnalyticsSource.API.value)
    }

    @Test
    fun `Anonymous API event preserves metadata and strips identity only fields`() {
        val command = AnalyticsEventCommand(
            event = ProductAnalyticsEvent.RESTROOM_DETAILS_OPENED,
            tgUserId = null,
            tgChatId = null,
            username = "should-be-dropped",
            source = AnalyticsSource.API,
            acquisitionTag = AcquisitionTag.WEBSITE,
            metadata = mapOf("screen" to "details")
        )

        val eventSlot = slot<AnalyticsEventEntity>()
        every { analyticsEventJpaRepository.save(capture(eventSlot)) } answers { firstArg() }

        analyticsService.trackProductEvent(command)

        val saved = eventSlot.captured
        assertThat(saved.user).isNull()
        assertThat(saved.metadata).containsEntry("screen", "details")
        verify(exactly = 0) { encryptionService.encrypt(any()) }
        verify(exactly = 0) { entityManager.createNativeQuery(any<String>()) }
    }

    @Test
    fun `Non-trackable event does not save anything`() {
        val command = AnalyticsEventCommand(
            event = ProductAnalyticsEvent.RESTROOM_DETAILS_OPENED,
            tgUserId = null,
            tgChatId = null,
            username = null,
            source = AnalyticsSource.TELEGRAM_BOT
        )

        val tracked = analyticsService.trackProductEvent(command)

        assertThat(tracked).isFalse()
        verify(exactly = 0) { encryptionService.encrypt(any()) }
        verify(exactly = 0) { entityManager.createNativeQuery(any<String>()) }
        verify(exactly = 0) { analyticsEventJpaRepository.save(any()) }
    }

    @Test
    fun `Event-specific increment matrix for search success`() {
        testIncrementMatrix(ProductAnalyticsEvent.NEAREST_RESTROOMS_RETURNED, searches = 1, success = 1, empty = 0, details = 0)
    }

    @Test
    fun `Event-specific increment matrix for search empty`() {
        testIncrementMatrix(ProductAnalyticsEvent.NEAREST_RESTROOMS_NO_RESULTS, searches = 1, success = 0, empty = 1, details = 0)
    }

    @Test
    fun `Event-specific increment matrix for details opened`() {
        testIncrementMatrix(ProductAnalyticsEvent.RESTROOM_DETAILS_OPENED, searches = 0, success = 0, empty = 0, details = 1)
    }

    private fun testIncrementMatrix(event: ProductAnalyticsEvent, searches: Int, success: Int, empty: Int, details: Int) {
        val userId = UUID.randomUUID()
        val command = AnalyticsEventCommand(event = event, tgUserId = 1L, tgChatId = "chat", username = "test", source = AnalyticsSource.TELEGRAM_BOT)
        every { encryptionService.encrypt(any()) } returns "encrypted"
        every { query.singleResult } returns userId
        every { userJpaRepository.getReferenceById(userId) } returns UserEntity().apply { this.id = userId }
        every { userAnalyticsJpaRepository.ensureUserAnalyticsRow(userId) } returns Unit
        every { userAnalyticsJpaRepository.incrementMetrics(any(), any(), any(), any(), any(), any(), any()) } returns Unit
        every { analyticsEventJpaRepository.save(any()) } answers { firstArg() }

        analyticsService.trackProductEvent(command)

        verify { userAnalyticsJpaRepository.incrementMetrics(userId, searches, success, empty, details, event.value, any()) }
    }
}
