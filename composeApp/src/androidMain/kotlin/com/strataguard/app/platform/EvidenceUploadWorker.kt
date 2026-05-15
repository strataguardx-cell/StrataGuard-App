package com.strataguard.app.platform

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.strataguard.app.data.evidence.FirebaseEvidenceRepository
import com.strataguard.app.data.evidence.SyncStatus

class EvidenceUploadWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val pending = dequeuePendingEvidence()
        if (pending.isEmpty()) return Result.success()

        val repo = FirebaseEvidenceRepository()
        var allSucceeded = true

        for (item in pending) {
            val result = runCatching {
                // Re-upload: write directly to Firestore with the already-computed thumbnail
                repo.saveItem(item.copy(syncStatus = SyncStatus.SYNCED.name))
            }
            if (result.isSuccess) {
                removePendingEvidence(item.id)
            } else {
                allSucceeded = false
            }
        }

        return if (allSucceeded) Result.success() else Result.retry()
    }
}
