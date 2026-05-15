package com.strataguard.app.data.evidence

import com.strataguard.app.platform.dequeuePendingEvidence
import com.strataguard.app.platform.enqueuePendingEvidence
import com.strataguard.app.platform.removePendingEvidence
import com.strataguard.app.platform.scheduleEvidenceSync
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
            syncStatus = SyncStatus.SYNCED.name,
        )

        val uploaded = runCatching { col.document(id).set(item) }
        if (uploaded.isFailure) {
            // Offline — store locally and schedule background sync
            enqueuePendingEvidence(item.copy(syncStatus = SyncStatus.PENDING.name))
            scheduleEvidenceSync()
            return@runCatching item.copy(syncStatus = SyncStatus.PENDING.name)
        }
        item
    }

    /** Called by the upload worker to re-persist an already-built item. */
    suspend fun saveItem(item: EvidenceItem) {
        col.document(item.id).set(item)
    }

    override suspend fun getEvidence(): List<EvidenceItem> {
        val pending = dequeuePendingEvidence()

        val synced = runCatching {
            val uid = auth.currentUser?.uid ?: return@runCatching emptyList()
            col.where { "userId" equalTo uid }
                .get()
                .documents
                .mapNotNull { runCatching { it.data<EvidenceItem>() }.getOrNull() }
        }.getOrElse { emptyList() }

        // Flush any pending items that might have synced via Firestore already
        val syncedIds = synced.map { it.id }.toSet()
        pending.filter { it.id in syncedIds }.forEach { removePendingEvidence(it.id) }

        val pendingNotYetSynced = pending.filter { it.id !in syncedIds }
        return (pendingNotYetSynced + synced).sortedByDescending { it.capturedAt }
    }

    override suspend fun deleteEvidence(id: String) {
        removePendingEvidence(id)
        runCatching { col.document(id).delete() }
    }
}
