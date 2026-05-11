package com.strataguard.app.data.dispute

import com.strataguard.app.platform.analyzeImageExif
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.datetime.Clock

class FirestoreDisputeRepository : DisputeRepository {

    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val col get() = db.collection("disputes")

    override suspend fun getDisputes(): List<Dispute> = runCatching {
        val uid = auth.currentUser?.uid ?: return@runCatching emptyList()
        col.where { "userId" equalTo uid }
            .get()
            .documents
            .mapNotNull { runCatching { it.data<Dispute>() }.getOrNull() }
            .sortedByDescending { it.createdAt }
    }.getOrElse { emptyList() }

    override suspend fun getDispute(id: String): Dispute? = runCatching {
        val doc = col.document(id).get()
        if (doc.exists) doc.data<Dispute>() else null
    }.getOrNull()

    override suspend fun createDispute(
        state: String,
        disputeType: String,
        strataId: String?,
        filingDeadline: String?,
        notes: String?,
    ): Result<Dispute> = runCatching {
        val uid = auth.currentUser?.uid ?: error("Not authenticated")
        val ts = Clock.System.now().toEpochMilliseconds()
        val id = "${uid}_dispute_$ts"
        val tribunal = tribunalFor(state)

        val dispute = Dispute(
            id = id,
            userId = uid,
            state = state,
            tribunal = tribunal,
            disputeType = disputeType,
            status = DisputeStatus.DRAFT.name,
            strataId = strataId ?: "",
            filingDeadline = filingDeadline ?: "",
            notes = notes ?: "",
            createdAt = ts,
            updatedAt = ts,
        )
        col.document(id).set(dispute)
        dispute
    }

    override suspend fun linkIncident(disputeId: String, incidentId: String): Result<Unit> = runCatching {
        val dispute = getDispute(disputeId) ?: error("Dispute not found")
        val updated = dispute.copy(
            incidentIds = (dispute.incidentIds + incidentId).distinct(),
            updatedAt = Clock.System.now().toEpochMilliseconds(),
        )
        col.document(disputeId).set(updated)
    }

    override suspend fun deleteDispute(id: String) {
        runCatching { col.document(id).delete() }
    }

    override suspend fun runAssessment(disputeId: String): Result<RiskAssessment> = runCatching {
        val dispute = getDispute(disputeId) ?: error("Dispute not found")
        // MVP: rule-based on-device assessment (mirrors server logic)
        val score = computeScore(dispute)
        val verdict = when {
            score >= 0.65f -> "STRONG"
            score >= 0.40f -> "MODERATE"
            else -> "WEAK"
        }
        val factors = mapOf(
            "Incident count" to "${dispute.incidentIds.size}",
            "State" to dispute.state,
            "Dispute type" to (DisputeType.entries.find { it.name == dispute.disputeType }?.displayName ?: dispute.disputeType),
        )
        val updated = dispute.copy(
            riskScore = score,
            riskVerdict = verdict,
            riskFactors = factors,
            updatedAt = Clock.System.now().toEpochMilliseconds(),
        )
        col.document(disputeId).set(updated)
        RiskAssessment(score, verdict, factors)
    }

    private fun computeScore(dispute: Dispute): Float {
        var score = 0.50f
        score += when {
            dispute.incidentIds.size >= 3 -> 0.15f
            dispute.incidentIds.size >= 1 -> 0.05f
            else -> -0.15f
        }
        if (dispute.strataId.isNotBlank()) score += 0.05f
        if (dispute.filingDeadline.isNotBlank()) score += 0.05f
        return score.coerceIn(0f, 1f)
    }

    private fun tribunalFor(state: String) = when (state.uppercase()) {
        "NSW" -> "NCAT"; "VIC" -> "VCAT"; "QLD" -> "QCAT"
        "WA" -> "SAT"; "SA" -> "SACAT"; else -> "Tribunal"
    }
}
