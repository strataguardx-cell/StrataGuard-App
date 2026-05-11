package com.strataguard.server.controller.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class EvidenceItemDto(
    val id: String,
    val type: String,
    val title: String?,
    val description: String?,
    val s3Url: String?,           // pre-signed URL, valid 1 hour
    val capturedAt: Long,
    val latitude: Double?,
    val longitude: Double?,
    val aiDetectionStatus: String,
    val aiDetectionScore: Double?,
    val aiDetectionVerdict: String?,
    val aiDetectionFlags: List<String>?,
    val aiDetectionModel: String?,
    val createdAt: Long,
)

data class UploadEvidenceRequest(
    @field:NotBlank val title: String,
    val description: String? = null,
    val strataId: String? = null,       // optional — link to a known strata plan
    @field:NotNull val capturedAt: Long, // epoch millis from device
    val latitude: Double? = null,
    val longitude: Double? = null,
    // On-device EXIF analysis results (Layer 1 — sent by app before upload)
    val aiDetectionVerdict: String? = null,
    val aiDetectionScore: Double? = null,
    val aiDetectionFlags: List<String>? = null,
)
