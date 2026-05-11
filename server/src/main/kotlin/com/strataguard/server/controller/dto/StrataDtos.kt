package com.strataguard.server.controller.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class StrataPlanSummaryDto(
    val id: String,
    val planNumber: String,
    val address: String,
    val suburb: String?,
    val state: String,
    val postcode: String?,
    val totalLots: Int?,
    val yearBuilt: Int?,
    val sinkingFundStatus: String,
    val activeDefectCount: Int,
    val dataSource: String,
)

data class StrataPlanDetailDto(
    val id: String,
    val planNumber: String,
    val address: String,
    val suburb: String?,
    val state: String,
    val postcode: String?,
    val totalLots: Int?,
    val yearBuilt: Int?,
    val registrationDate: String?,
    val managingAgent: String?,
    val managingAgentLicence: String?,
    val lastAgm: String?,
    val buildingClass: String?,
    val sinkingFundStatus: String,
    val latitude: Double?,
    val longitude: Double?,
    val dataSource: String,
    val defects: List<BuildingDefectDto>,
    val createdAt: Long,
    val updatedAt: Long,
)

data class BuildingDefectDto(
    val id: String,
    val category: String,
    val description: String,
    val severity: String,
    val orderType: String?,
    val reportedDate: String?,
    val resolvedDate: String?,
    val resolutionNotes: String?,
    val sourceDocument: String?,
)

data class CreateStrataPlanRequest(
    @field:NotBlank @field:Size(min = 4, max = 20)
    val planNumber: String,

    @field:NotBlank
    val address: String,

    val suburb: String? = null,

    @field:NotBlank @field:Size(min = 2, max = 3)
    val state: String,

    @field:Size(max = 4)
    val postcode: String? = null,

    val totalLots: Int? = null,
    val yearBuilt: Int? = null,
)

data class ErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
)
