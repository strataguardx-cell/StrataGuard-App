package com.strataguard.app.data.dispute

import kotlinx.serialization.Serializable

@Serializable
data class Dispute(
    val id: String = "",
    val userId: String = "",
    val state: String = "",
    val tribunal: String = "",
    val disputeType: String = "",
    val status: String = DisputeStatus.DRAFT.name,
    val riskScore: Float = 0f,
    val riskVerdict: String = "",
    val riskFactors: Map<String, String> = emptyMap(),
    val filingDeadline: String = "",
    val strataId: String = "",
    val strataAddress: String = "",
    val incidentIds: List<String> = emptyList(),
    val notes: String = "",
    val hasPdf: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)

enum class DisputeStatus { DRAFT, READY, FILED, MEDIATION, HEARING, RESOLVED }

enum class DisputeType(val displayName: String, val description: String) {
    BOND("Bond Dispute", "Claim on your security deposit or bond"),
    DEFECT_RECTIFICATION("Defect Rectification", "Force repair of a building defect"),
    LEVY("Levy Dispute", "Challenge strata levy amounts or calculations"),
    NOISE("Noise / Nuisance", "Ongoing noise or nuisance from another lot"),
    BYLAWS("By-Law Enforcement", "Breach of owners corporation by-laws"),
}

enum class AustralianState(
    val displayName: String,
    val tribunal: String,
    val tribunalFull: String,
    val legislation: String,
) {
    NSW("New South Wales", "NCAT", "NSW Civil and Administrative Tribunal", "Strata Schemes Management Act 2015"),
    VIC("Victoria", "VCAT", "Victorian Civil and Administrative Tribunal", "Owners Corporations Act 2006"),
    QLD("Queensland", "QCAT", "Queensland Civil and Administrative Tribunal", "Body Corporate and Community Management Act 1997"),
    WA("Western Australia", "SAT", "State Administrative Tribunal", "Strata Titles Act 1985"),
    SA("South Australia", "SACAT", "South Australian Civil and Administrative Tribunal", "Strata Titles Act 1988"),
}

val Dispute.disputeStatusEnum: DisputeStatus
    get() = runCatching { DisputeStatus.valueOf(this.status) }.getOrDefault(DisputeStatus.DRAFT)

val Dispute.verdictLabel: String
    get() = when (riskVerdict) {
        "STRONG" -> "Strong Case"
        "MODERATE" -> "Moderate Case"
        "WEAK" -> "Weak Case"
        else -> "Not assessed"
    }
