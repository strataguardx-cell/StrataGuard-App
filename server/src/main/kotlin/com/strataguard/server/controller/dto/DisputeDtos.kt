package com.strataguard.server.controller.dto

import jakarta.validation.constraints.NotBlank

data class DisputeDto(
    val id: String,
    val state: String,
    val tribunal: String,
    val disputeType: String,
    val status: String,
    val riskScore: Double?,
    val riskVerdict: String?,            // STRONG / MODERATE / WEAK
    val filingDeadline: String?,
    val hearingDate: String?,
    val outcome: String?,
    val strataId: String?,
    val strataAddress: String?,
    val incidentCount: Int,
    val hasPdf: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)

data class DisputeDetailDto(
    val id: String,
    val state: String,
    val tribunal: String,
    val disputeType: String,
    val status: String,
    val riskScore: Double?,
    val riskVerdict: String?,
    val riskFactors: Map<String, Any>?,
    val filingDeadline: String?,
    val hearingDate: String?,
    val outcome: String?,
    val notes: String?,
    val strataId: String?,
    val strataAddress: String?,
    val incidents: List<IncidentDto>,
    val pdfUrl: String?,
    val createdAt: Long,
    val updatedAt: Long,
)

data class RiskAssessmentDto(
    val score: Double,
    val verdict: String,
    val factors: Map<String, Any>,
)

data class CreateDisputeRequest(
    @field:NotBlank val state: String,
    @field:NotBlank val disputeType: String,
    val strataId: String? = null,
    val filingDeadline: String? = null,
    val notes: String? = null,
)

data class LinkIncidentRequest(
    @field:NotBlank val incidentId: String,
)

data class ExportPdfResponse(
    val pdfUrl: String,
    val expiresAt: Long,
)
