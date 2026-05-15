package com.strataguard.app.data.evidence

import kotlinx.serialization.Serializable

@Serializable
data class EvidenceItem(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val thumbnail: String = "",   // base64-encoded JPEG thumbnail (~10-30 KB)
    val capturedAt: Long = 0L,
    val aiVerdict: String = AiVerdict.PENDING.name,
    val aiScore: Float = 0f,
    val aiFlags: List<String> = emptyList(),
    val syncStatus: String = SyncStatus.SYNCED.name,
)

enum class SyncStatus { SYNCED, PENDING }

enum class AiVerdict { PENDING, AUTHENTIC, SUSPICIOUS, AI_GENERATED }

enum class AiFlag(val displayName: String) {
    MISSING_EXIF("Missing metadata"),
    NO_CAMERA_MODEL("No camera model"),
    NO_GPS_DATA("No location data"),
    TIMESTAMP_MISMATCH("Timestamp inconsistency"),
    AI_TOOL_METADATA("AI tool signature detected"),
}

val EvidenceItem.verdict: AiVerdict
    get() = runCatching { AiVerdict.valueOf(aiVerdict) }.getOrDefault(AiVerdict.PENDING)

val EvidenceItem.flags: List<AiFlag>
    get() = aiFlags.mapNotNull { runCatching { AiFlag.valueOf(it) }.getOrNull() }
