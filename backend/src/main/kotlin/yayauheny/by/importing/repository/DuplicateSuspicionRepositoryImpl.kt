package yayauheny.by.importing.repository

import java.util.UUID
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType

class DuplicateSuspicionRepositoryImpl : DuplicateSuspicionRepository {
    private val suspicionTable = DSL.table(DSL.name("restroom_duplicate_suspicions"))
    private val suspicionExistingRestroomId = DSL.field(DSL.name("existing_restroom_id"), SQLDataType.UUID.nullable(false))
    private val suspicionCandidateRestroomId = DSL.field(DSL.name("candidate_restroom_id"), SQLDataType.UUID.nullable(false))
    private val suspicionDistanceMeters = DSL.field(DSL.name("distance_m"), SQLDataType.DOUBLE.nullable(false))
    private val suspicionReason = DSL.field(DSL.name("reason"), SQLDataType.VARCHAR(100).nullable(false))
    private val suspicionStatus = DSL.field(DSL.name("status"), SQLDataType.VARCHAR(20).nullable(false))
    private val suspicionProvider = DSL.field(DSL.name("provider"), SQLDataType.VARCHAR(50).nullable(false))
    private val suspicionExternalId = DSL.field(DSL.name("external_id"), SQLDataType.VARCHAR(128))

    override fun logNearbySuspicionInTx(
        txCtx: DSLContext,
        existingRestroomId: UUID,
        candidateRestroomId: UUID,
        distanceMeters: Double,
        provider: String,
        externalId: String?
    ) {
        txCtx
            .insertInto(suspicionTable)
            .set(suspicionExistingRestroomId, existingRestroomId)
            .set(suspicionCandidateRestroomId, candidateRestroomId)
            .set(suspicionDistanceMeters, distanceMeters)
            .set(suspicionReason, "NEARBY_WITHIN_15M")
            .set(suspicionStatus, "PENDING")
            .set(suspicionProvider, provider)
            .set(suspicionExternalId, externalId)
            .execute()
    }
}
