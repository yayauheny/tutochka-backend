package integration.api.analytics

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import yayauheny.by.analytics.service.AnalyticsService
import yayauheny.by.model.analytics.AcquisitionTag
import yayauheny.by.model.analytics.AnalyticsEventCommand
import yayauheny.by.model.analytics.AnalyticsSource
import yayauheny.by.model.analytics.ProductAnalyticsEvent
import yayauheny.by.testsupport.TEST_ENCRYPTION_KEYSET_JSON
import yayauheny.by.util.EncryptionService

@Tag("integration")
@DisplayName("Analytics user upsert integration tests")
class AnalyticsUserUpsertIntegrationTest : integration.base.BaseIntegrationTest() {
    @Autowired
    private lateinit var analyticsService: AnalyticsService

    private val encryptionService = EncryptionService(TEST_ENCRYPTION_KEYSET_JSON)

    @Test
    @DisplayName("first event creates a user and stores acquisition tag")
    fun first_event_creates_a_user_and_stores_acquisition_tag() {
        val command = analyticsCommand(
            userId = 10L,
            chatId = "chat-1",
            username = "first-user",
            source = AnalyticsSource.TELEGRAM_BOT,
            acquisitionTag = AcquisitionTag.WEBSITE
        )

        analyticsService.trackProductEvent(command)

        val user = userRepository.findAll().single()
        assertThat(encryptionService.decrypt(user.tgUserId)).isEqualTo("10")
        assertThat(encryptionService.decrypt(user.tgChatId)).isEqualTo("chat-1")
        assertThat(user.username).isEqualTo("first-user")
        assertThat(user.source).isEqualTo(AnalyticsSource.TELEGRAM_BOT.value)
        assertThat(user.acquisitionTag).isEqualTo(AcquisitionTag.WEBSITE.value)

        val userAnalytics = userAnalyticsRepository.findAll().single()
        assertThat(userAnalytics.lastEvent).isEqualTo(ProductAnalyticsEvent.BOT_STARTED.value)
        assertThat(analyticsEventRepository.count()).isEqualTo(1)
    }

    @Test
    @DisplayName("repeated event updates the same user and refreshes mutable fields")
    fun repeated_event_updates_the_same_user_and_refreshes_mutable_fields() {
        analyticsService.trackProductEvent(
            analyticsCommand(
                userId = 10L,
                chatId = "chat-1",
                username = "first-user",
                source = AnalyticsSource.TELEGRAM_BOT,
                acquisitionTag = AcquisitionTag.WEBSITE
            )
        )

        analyticsService.trackProductEvent(
            analyticsCommand(
                userId = 10L,
                chatId = "chat-2",
                username = "second-user",
                source = AnalyticsSource.API,
                acquisitionTag = null
            )
        )

        assertThat(userRepository.count()).isEqualTo(1)
        assertThat(userAnalyticsRepository.count()).isEqualTo(1)
        assertThat(analyticsEventRepository.count()).isEqualTo(2)

        val user = userRepository.findAll().single()
        assertThat(encryptionService.decrypt(user.tgUserId)).isEqualTo("10")
        assertThat(encryptionService.decrypt(user.tgChatId)).isEqualTo("chat-2")
        assertThat(user.username).isEqualTo("second-user")
        assertThat(user.source).isEqualTo(AnalyticsSource.API.value)
        assertThat(user.acquisitionTag).isEqualTo(AcquisitionTag.WEBSITE.value)

        val userAnalytics = userAnalyticsRepository.findAll().single()
        assertThat(userAnalytics.lastEvent).isEqualTo(ProductAnalyticsEvent.BOT_STARTED.value)
    }

    @Test
    @DisplayName("concurrent first event writes do not hit unique constraint errors")
    fun concurrent_first_event_writes_do_not_hit_unique_constraint_errors() {
        val executor = Executors.newFixedThreadPool(2)
        val failures = AtomicReference<Throwable?>(null)
        val task = Callable {
            analyticsService.trackProductEvent(
                analyticsCommand(
                    userId = 42L,
                    chatId = "chat-42",
                    username = "concurrent-user",
                    source = AnalyticsSource.TELEGRAM_BOT,
                    acquisitionTag = AcquisitionTag.DIRECT
                )
            )
        }

        val first = executor.submit(task)
        val second = executor.submit(task)
        executor.shutdown()
        executor.awaitTermination(10, TimeUnit.SECONDS)

        runCatching { first.get() }.onFailure { failures.compareAndSet(null, it) }
        runCatching { second.get() }.onFailure { failures.compareAndSet(null, it) }

        assertThat(failures.get()).isNull()
        assertThat(userRepository.count()).isEqualTo(1)
        assertThat(userAnalyticsRepository.count()).isEqualTo(1)
        assertThat(analyticsEventRepository.count()).isEqualTo(2)
    }

    private fun analyticsCommand(
        userId: Long,
        chatId: String,
        username: String?,
        source: AnalyticsSource,
        acquisitionTag: AcquisitionTag?
    ) = AnalyticsEventCommand(
        event = ProductAnalyticsEvent.BOT_STARTED,
        tgUserId = userId,
        tgChatId = chatId,
        username = username,
        source = source,
        acquisitionTag = acquisitionTag,
        metadata = emptyMap()
    )
}
