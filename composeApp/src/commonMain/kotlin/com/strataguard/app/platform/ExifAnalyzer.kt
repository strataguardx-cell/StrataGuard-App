package com.strataguard.app.platform

import com.strataguard.app.data.evidence.AiFlag
import com.strataguard.app.data.evidence.AiVerdict

data class ExifAnalysisResult(
    val verdict: AiVerdict,
    val score: Float,
    val flags: List<AiFlag>,
)

expect fun analyzeImageExif(imageBytes: ByteArray): ExifAnalysisResult
