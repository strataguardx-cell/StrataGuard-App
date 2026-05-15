package com.strataguard.app.platform

import com.strataguard.app.data.evidence.EvidenceItem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

private val pendingDir: File
    get() = File(checkNotNull(androidAppContext) { "androidAppContext not set" }.filesDir, "pending_evidence").also { it.mkdirs() }

actual fun enqueuePendingEvidence(item: EvidenceItem) {
    File(pendingDir, "${item.id}.json").writeText(Json.encodeToString(item))
}

actual fun dequeuePendingEvidence(): List<EvidenceItem> =
    pendingDir.listFiles()
        ?.mapNotNull { file ->
            runCatching { Json.decodeFromString<EvidenceItem>(file.readText()) }.getOrNull()
        }
        ?: emptyList()

actual fun removePendingEvidence(id: String) {
    File(pendingDir, "$id.json").delete()
}

actual fun pendingEvidenceCount(): Int = pendingDir.listFiles()?.size ?: 0
