package com.strataguard.server.controller.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class IncidentDto(
    val id: String,
    val title: String,
    val description: String?,
    val incidentDate: String,      // ISO-8601 date: YYYY-MM-DD
    val category: String?,
    val status: String,
    val strataId: String?,
    val evidenceCount: Int,
    val createdAt: Long,
    val updatedAt: Long,
)

data class IncidentDetailDto(
    val id: String,
    val title: String,
    val description: String?,
    val incidentDate: String,
    val category: String?,
    val status: String,
    val strataId: String?,
    val evidence: List<EvidenceItemDto>,
    val createdAt: Long,
    val updatedAt: Long,
)

data class CreateIncidentRequest(
    @field:NotBlank val title: String,
    val description: String? = null,
    @field:NotBlank val incidentDate: String,  // YYYY-MM-DD
    val category: String? = null,
    val strataId: String? = null,
)

data class UpdateIncidentRequest(
    val title: String? = null,
    val description: String? = null,
    val incidentDate: String? = null,
    val category: String? = null,
    val status: String? = null,
)

data class LinkEvidenceRequest(
    @field:NotBlank val evidenceId: String,
)
