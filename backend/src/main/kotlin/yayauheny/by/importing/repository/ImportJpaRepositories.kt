package yayauheny.by.importing.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import yayauheny.by.entity.DuplicateSuspicionEntity
import yayauheny.by.entity.RestroomImportEntity
import yayauheny.by.model.enums.ImportProvider
import java.util.UUID

@Repository
interface RestroomImportJpaRepository : JpaRepository<RestroomImportEntity, UUID> {
    fun findByProviderAndEntityTypeAndExternalId(provider: ImportProvider, entityType: String, externalId: String): RestroomImportEntity?
}

@Repository
interface DuplicateSuspicionJpaRepository : JpaRepository<DuplicateSuspicionEntity, UUID> {
    fun findByProviderAndExternalId(provider: String, externalId: String): DuplicateSuspicionEntity?
    fun existsByCandidateRestroomId(candidateRestroomId: UUID): Boolean
    
    @Query("SELECT d FROM DuplicateSuspicionEntity d WHERE d.status = 'PENDING'")
    fun findPendingSuspicions(): List<DuplicateSuspicionEntity>
}
