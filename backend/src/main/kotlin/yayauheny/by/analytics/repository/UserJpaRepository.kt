package yayauheny.by.analytics.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import yayauheny.by.analytics.entity.UserEntity
import java.util.UUID

@Repository
interface UserJpaRepository : JpaRepository<UserEntity, UUID> {
    fun findByTgUserId(tgUserId: String): UserEntity?
}
