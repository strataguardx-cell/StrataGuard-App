package com.strataguard.app.platform

import com.strataguard.app.data.evidence.EvidenceItem

expect fun enqueuePendingEvidence(item: EvidenceItem)
expect fun dequeuePendingEvidence(): List<EvidenceItem>
expect fun removePendingEvidence(id: String)
expect fun pendingEvidenceCount(): Int
