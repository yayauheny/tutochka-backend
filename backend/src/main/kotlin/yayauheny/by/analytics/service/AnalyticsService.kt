package yayauheny.by.analytics.service

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import yayauheny.by.analytics.entity.AnalyticsEventEntity
import yayauheny.by.analytics.entity.UserEntity
import yayauheny.by.analytics.model.EncryptedAnalyticsEventCommand
import yayauheny.by.analytics.repository.AnalyticsEventJpaRepository
import yayauheny.by.analytics.repository.UserAnalyticsJpaRepository
import yayauheny.by.analytics.repository.UserJpaRepository
import yayauheny.by.model.analytics.AnalyticsEventCommand
import yayauheny.by.model.analytics.AnalyticsSource
import yayauheny.by.model.analytics.ProductAnalyticsEvent
import yayauheny.by.util.EncryptionService
import yayauheny.by.util.point
import java.time.Instant
import java.util.UUID

@Service
class AnalyticsService(
    private val userJpaRepository: UserJpaRepository,
    private val userAnalyticsJpaRepository: UserAnalyticsJpaRepository,
    private val analyticsEventJpaRepository: AnalyticsEventJpaRepository,
    private val encryptionService: EncryptionService
) {
    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Transactional
    fun trackProductEvent(command: AnalyticsEventCommand): Boolean {
        return when {
            command.hasTelegramIdentity() -> {
                trackEncryptedProductEvent(command.toEncryptedCommand())
                true
            }
            command.canBeTrackedAnonymously() -> {
                trackEncryptedProductEvent(command.toAnonymousCommand())
                true
            }
            else -> false
        }
    }

    private fun trackEncryptedProductEvent(command: EncryptedAnalyticsEventCommand) {
        var user: UserEntity? = null
        if (command.tgUserId != null) {
            // TODO improve: replace native SQL upsert with cleaner Hibernate/JPA approach when behavior is fully covered.
            val userId =
                upsertUser(
                    tgUserId = command.tgUserId!!,
                    tgChatId = requireNotNull(command.tgChatId),
                    source = command.source.value,
                    username = command.username,
                    acquisitionTag = command.acquisitionTag?.value
                )
            user = userJpaRepository.getReferenceById(userId)
            userAnalyticsJpaRepository.ensureUserAnalyticsRow(userId)

            val isSearch = command.event == ProductAnalyticsEvent.NEAREST_RESTROOMS_RETURNED ||
                command.event == ProductAnalyticsEvent.NEAREST_RESTROOMS_NO_RESULTS
            val isSuccessSearch = command.event == ProductAnalyticsEvent.NEAREST_RESTROOMS_RETURNED
            val isEmptySearch = command.event == ProductAnalyticsEvent.NEAREST_RESTROOMS_NO_RESULTS
            val isDetailsOpened = command.event == ProductAnalyticsEvent.RESTROOM_DETAILS_OPENED

            userAnalyticsJpaRepository.incrementMetrics(
                userId = user.id!!,
                searchesInc = if (isSearch) 1 else 0,
                successfulSearchesInc = if (isSuccessSearch) 1 else 0,
                emptySearchesInc = if (isEmptySearch) 1 else 0,
                detailsOpenedInc = if (isDetailsOpened) 1 else 0,
                lastEvent = command.event.value,
                lastEventAt = Instant.now()
            )
        }

        val eventEntity = AnalyticsEventEntity().apply {
            this.event = command.event.value
            this.user = user
            this.source = command.source.value
            this.location = command.toPoint()
            this.resultsCount = command.resultsCount
            this.metadata = command.metadata ?: emptyMap()
        }
        analyticsEventJpaRepository.save(eventEntity)
    }

    private fun AnalyticsEventCommand.hasTelegramIdentity(): Boolean = tgUserId != null && !tgChatId.isNullOrBlank()

    private fun AnalyticsEventCommand.canBeTrackedAnonymously(): Boolean =
        source == AnalyticsSource.API && tgUserId == null && tgChatId.isNullOrBlank()

    private fun AnalyticsEventCommand.toEncryptedCommand(): EncryptedAnalyticsEventCommand {
        return EncryptedAnalyticsEventCommand(
            event = event,
            tgUserId = encryptionService.encrypt(requireNotNull(tgUserId).toString()),
            tgChatId = encryptionService.encrypt(requireNotNull(tgChatId)),
            username = username,
            source = source,
            lat = lat,
            lon = lon,
            resultsCount = resultsCount,
            acquisitionTag = acquisitionTag,
            metadata = metadata
        )
    }

    private fun AnalyticsEventCommand.toAnonymousCommand(): EncryptedAnalyticsEventCommand {
        return EncryptedAnalyticsEventCommand(
            event = event,
            tgUserId = null,
            tgChatId = null,
            username = null,
            source = source,
            lat = lat,
            lon = lon,
            resultsCount = resultsCount,
            acquisitionTag = null,
            metadata = metadata
        )
    }

    private fun upsertUser(
        tgUserId: String,
        tgChatId: String,
        source: String,
        username: String?,
        acquisitionTag: String?
    ): UUID {
        val sql =
            """
                INSERT INTO users (
                    tg_user_id,
                    tg_chat_id,
                    source,
                    username,
                    acquisition_tag
                )
                VALUES (
                    :tgUserId,
                    :tgChatId,
                    :source,
                    :username,
                    COALESCE(:acquisitionTag, 'direct')
                )
                ON CONFLICT (tg_user_id) DO UPDATE
                SET tg_chat_id = EXCLUDED.tg_chat_id,
                    username = COALESCE(EXCLUDED.username, users.username),
                    acquisition_tag = COALESCE(CAST(:acquisitionTag AS text), users.acquisition_tag),
                    updated_at = now()
                RETURNING id
            """.trimIndent()

        val result =
            entityManager.createNativeQuery(sql)
                .setParameter("tgUserId", tgUserId)
                .setParameter("tgChatId", tgChatId)
                .setParameter("source", source)
                .setParameter("username", username)
                .setParameter("acquisitionTag", acquisitionTag)
                .singleResult

        return when (result) {
            is UUID -> result
            else -> UUID.fromString(result.toString())
        }
    }

    private fun EncryptedAnalyticsEventCommand.toPoint() =
        if (lat != null && lon != null) {
            point(lon, lat).also {
                it.srid = 4326
            }
        } else {
            null
        }
}
