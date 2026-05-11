package com.strataguard.app.data.dispute

interface DisputeRepository {
    suspend fun getDisputes(): List<Dispute>
    suspend fun getDispute(id: String): Dispute?
    suspend fun createDispute(
        state: String,
        disputeType: String,
        strataId: String?,
        filingDeadline: String?,
        notes: String?,
    ): Result<Dispute>
    suspend fun linkIncident(disputeId: String, incidentId: String): Result<Unit>
    suspend fun deleteDispute(id: String)
    suspend fun runAssessment(disputeId: String): Result<RiskAssessment>
}

data class RiskAssessment(
    val score: Float,
    val verdict: String,
    val factors: Map<String, String>,
)
