package yayauheny.by.importing.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import yayauheny.by.entity.RestroomImportEntity
import yayauheny.by.model.enums.ImportProvider
import java.util.UUID

@Repository
interface RestroomImportJpaRepository : JpaRepository<RestroomImportEntity, UUID> {
    fun findByProviderAndEntityTypeAndExternalId(provider: ImportProvider, entityType: String, externalId: String): RestroomImportEntity?
}
