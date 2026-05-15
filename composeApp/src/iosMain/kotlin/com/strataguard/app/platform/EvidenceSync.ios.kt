package com.strataguard.app.platform

// iOS: retry happens on next app foreground when EvidenceViewModel.loadEvidence() is called.
// BGTaskScheduler integration can be added post-MVP when background sync is needed.
actual fun scheduleEvidenceSync() = Unit
