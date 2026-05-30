package yayauheny.by.analytics.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users")
class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @Column(name = "tg_user_id", nullable = false, unique = true)
    var tgUserId: String = ""

    @Column(name = "tg_chat_id", nullable = false)
    var tgChatId: String = ""

    @Column(name = "source", nullable = false)
    var source: String = "telegram_bot"

    @Column(name = "username")
    var username: String? = null

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null

    @Column(name = "is_blocked", nullable = false)
    var isBlocked: Boolean = false

    @Column(name = "blocked_at")
    var blockedAt: Instant? = null
}
