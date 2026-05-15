package yayauheny.by.importing.repository

import java.util.UUID
import org.jooq.DSLContext

interface DuplicateSuspicionRepository {
    fun logNearbySuspicionInTx(
        txCtx: DSLContext,
        existingRestroomId: UUID,
        candidateRestroomId: UUID,
        distanceMeters: Double,
        provider: String,
        externalId: String?
    )
}
