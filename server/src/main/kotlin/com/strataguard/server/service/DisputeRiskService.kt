package com.strataguard.server.service

import com.strataguard.server.entity.DisputeEntity
import com.strataguard.server.entity.EvidenceItemEntity
import com.strataguard.server.entity.IncidentEntity
import com.strataguard.server.entity.StrataPlanEntity
import org.springframework.stereotype.Service

/**
 * Rule-based dispute risk scorer (MVP Layer 1).
 * Post-MVP: delegate to Python ML service for continuous learning.
 *
 * Score range: 0.00–1.00
 *   >= 0.65 → STRONG
 *   >= 0.40 → MODERATE
 *   <  0.40 → WEAK
 */
@Service
class DisputeRiskService {

    data class Assessment(
        val score: Double,
        val verdict: String,
        val factors: Map<String, Any>,
    )

    fun assess(
        dispute: DisputeEntity,
        incidents: List<IncidentEntity>,
        evidence: List<EvidenceItemEntity>,
        strata: StrataPlanEntity?,
    ): Assessment {
        var score = 0.50
        val factors = mutableMapOf<String, Any>()

        // ── Evidence quality ──────────────────────────────────────────────
        val verdicts = evidence.mapNotNull { it.aiDetectionVerdict }
        val hasAiGenerated = verdicts.any { it == "AI_GENERATED" }
        val authenticCount = verdicts.count { it == "AUTHENTIC" }
        val suspiciousCount = verdicts.count { it == "SUSPICIOUS" }

        val evidenceQuality = when {
            hasAiGenerated -> "POOR"
            suspiciousCount > authenticCount -> "MIXED"
            authenticCount > 0 -> "GOOD"
            else -> "UNVERIFIED"
        }
        factors["evidence_quality"] = evidenceQuality
        score += when (evidenceQuality) {
            "GOOD" -> 0.20
            "MIXED" -> 0.05
            "UNVERIFIED" -> -0.05
            "POOR" -> -0.30
            else -> 0.0
        }

        // ── Evidence quantity ─────────────────────────────────────────────
        val evidenceCount = evidence.size
        factors["evidence_count"] = evidenceCount
        score += when {
            evidenceCount >= 5 -> 0.10
            evidenceCount >= 2 -> 0.05
            evidenceCount == 1 -> -0.05
            else -> -0.15
        }

        // ── Incident count ────────────────────────────────────────────────
        val incidentCount = incidents.size
        factors["incident_count"] = incidentCount
        score += when {
            incidentCount >= 3 -> 0.10
            incidentCount >= 1 -> 0.05
            else -> -0.10
        }

        // ── Building defects match ────────────────────────────────────────
        if (strata != null) {
            val activeDefects = strata.defects.filter { it.resolvedDate == null }
            val criticalDefects = activeDefects.filter { it.severity == "critical" }
            val matchingDefects = activeDefects.filter { it.category in disputeTypeCategories(dispute.disputeType) }

            factors["building_defects_critical"] = criticalDefects.size
            factors["building_defects_matching"] = matchingDefects.size

            score += when {
                matchingDefects.isNotEmpty() && criticalDefects.isNotEmpty() -> 0.15
                matchingDefects.isNotEmpty() -> 0.10
                activeDefects.isNotEmpty() -> 0.05
                else -> -0.05
            }
        } else {
            factors["building_defects_critical"] = "unknown"
            factors["building_defects_matching"] = "unknown"
        }

        // ── State-specific deadline risk ──────────────────────────────────
        val deadlineRisk = filingDeadlineRisk(dispute)
        factors["deadline_risk"] = deadlineRisk
        score += when (deadlineRisk) {
            "URGENT" -> -0.10
            "APPROACHING" -> -0.05
            else -> 0.0
        }

        // ── Clamp ─────────────────────────────────────────────────────────
        score = score.coerceIn(0.0, 1.0)
        val roundedScore = (score * 100).toLong() / 100.0

        val verdict = when {
            roundedScore >= 0.65 -> "STRONG"
            roundedScore >= 0.40 -> "MODERATE"
            else -> "WEAK"
        }

        return Assessment(score = roundedScore, verdict = verdict, factors = factors)
    }

    // Maps dispute type to relevant defect categories for cross-referencing
    private fun disputeTypeCategories(disputeType: String): Set<String> = when (disputeType) {
        "bond" -> setOf("structural", "plumbing", "water_damage", "cosmetic", "mould")
        "defect_rectification" -> setOf("structural", "fire_safety", "plumbing", "electrical", "safety")
        "levy" -> emptySet()
        "noise" -> setOf("acoustic", "cosmetic")
        "bylaws" -> emptySet()
        else -> emptySet()
    }

    private fun filingDeadlineRisk(dispute: DisputeEntity): String {
        val deadline = dispute.filingDeadline ?: return "UNKNOWN"
        val daysUntil = java.time.LocalDate.now().until(deadline, java.time.temporal.ChronoUnit.DAYS)
        return when {
            daysUntil < 0 -> "OVERDUE"
            daysUntil <= 7 -> "URGENT"
            daysUntil <= 30 -> "APPROACHING"
            else -> "OK"
        }
    }
}
