package com.strataguard.app.data.evidence

interface EvidenceRepository {
    suspend fun addEvidence(
        imageBytes: ByteArray,
        title: String,
        description: String,
        verdict: AiVerdict,
        flags: List<AiFlag>,
        isFromCamera: Boolean,
    ): Result<EvidenceItem>

    suspend fun getEvidence(): List<EvidenceItem>
    suspend fun deleteEvidence(id: String)
}
