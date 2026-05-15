package com.strataguard.app.data.strata

import kotlinx.serialization.Serializable

@Serializable
data class StrataDocument(
    val id: String = "",
    val planNumber: String = "",
    val docType: String = "",
    val title: String? = null,
    val originalFilename: String? = null,
    val fileSizeBytes: Long? = null,
    val ocrStatus: String = "pending",
    val riskFlags: List<RiskFlag> = emptyList(),
    val uploadedAt: Long = 0L,
    val processedAt: Long? = null,
)

@Serializable
data class RiskFlag(
    val category: String = "",
    val label: String = "",
    val severity: String = "",
    val context: String = "",
)

val RiskFlag.severityColor: Long
    get() = when (severity) {
        "CRITICAL" -> 0xFFDC3545
        "HIGH" -> 0xFFE8A020
        "MEDIUM" -> 0xFF1B6CA8
        else -> 0xFF6C757D
    }
