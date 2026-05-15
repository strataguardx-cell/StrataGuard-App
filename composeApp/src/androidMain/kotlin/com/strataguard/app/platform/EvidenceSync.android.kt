package com.strataguard.app.platform

import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

actual fun scheduleEvidenceSync() {
    val request = OneTimeWorkRequestBuilder<EvidenceUploadWorker>()
        .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
        .build()
    val ctx = androidAppContext ?: return
    WorkManager.getInstance(ctx)
        .enqueueUniqueWork("evidence_sync", androidx.work.ExistingWorkPolicy.KEEP, request)
}
