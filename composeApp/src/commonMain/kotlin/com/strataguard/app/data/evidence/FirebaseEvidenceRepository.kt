package com.strataguard.app.data.evidence

import com.strataguard.app.platform.toThumbnailBytes
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.datetime.Clock

class FirebaseEvidenceRepository : EvidenceRepository {

    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val col get() = db.collection("evidence_items")

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun addEvidence(
        imageBytes: ByteArray,
        title: String,
        description: String,
        verdict: AiVerdict,
        flags: List<AiFlag>,
        isFromCamera: Boolean,
    ): Result<EvidenceItem> = runCatching {
        val uid = auth.currentUser?.uid ?: error("Not authenticated")
        val ts = Clock.System.now().toEpochMilliseconds()
        val id = "${uid}_$ts"

        val thumbnail = Base64.encode(imageBytes.toThumbnailBytes(maxDim = 300))

        val item = EvidenceItem(
            id = id,
            userId = uid,
            title = title.ifBlank { "Evidence" },
            description = description,
            thumbnail = thumbnail,
            capturedAt = ts,
            aiVerdict = verdict.name,
            aiScore = 0f,
            aiFlags = flags.map { it.name },
        )
        col.document(id).set(item)
        item
    }

    override suspend fun getEvidence(): List<EvidenceItem> = runCatching {
        val uid = auth.currentUser?.uid ?: return@runCatching emptyList()
        col.where { "userId" equalTo uid }
            .get()
            .documents
            .mapNotNull { runCatching { it.data<EvidenceItem>() }.getOrNull() }
            .sortedByDescending { it.capturedAt }
    }.getOrElse { emptyList() }

    override suspend fun deleteEvidence(id: String) {
        runCatching { col.document(id).delete() }
    }
}
