package com.strataguard.app.platform

import com.strataguard.app.data.evidence.AiFlag
import com.strataguard.app.data.evidence.AiVerdict

actual fun analyzeImageExif(imageBytes: ByteArray): ExifAnalysisResult {
    // Full CGImageSource EXIF analysis is platform.ImageIO territory.
    // For MVP, gallery imports on iOS are marked Suspicious (incomplete metadata
    // provenance). In-app camera captures bypass this and are auto-marked Authentic
    // in EvidenceViewModel.
    return ExifAnalysisResult(
        verdict = AiVerdict.SUSPICIOUS,
        score = 0.35f,
        flags = listOf(AiFlag.MISSING_EXIF),
    )
}
