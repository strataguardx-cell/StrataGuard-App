package com.strataguard.app.platform

import androidx.exifinterface.media.ExifInterface
import com.strataguard.app.data.evidence.AiFlag
import com.strataguard.app.data.evidence.AiVerdict
import java.io.ByteArrayInputStream

actual fun analyzeImageExif(imageBytes: ByteArray): ExifAnalysisResult {
    val flags = mutableListOf<AiFlag>()

    return runCatching {
        val exif = ExifInterface(ByteArrayInputStream(imageBytes))

        val make = exif.getAttribute(ExifInterface.TAG_MAKE)
        val model = exif.getAttribute(ExifInterface.TAG_MODEL)
        val software = exif.getAttribute(ExifInterface.TAG_SOFTWARE)
        val gpsLat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)
        val datetime = exif.getAttribute(ExifInterface.TAG_DATETIME)
        val dateOrig = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)

        if (make.isNullOrBlank() && model.isNullOrBlank()) {
            flags += AiFlag.MISSING_EXIF
            flags += AiFlag.NO_CAMERA_MODEL
        } else if (make.isNullOrBlank() || model.isNullOrBlank()) {
            flags += AiFlag.NO_CAMERA_MODEL
        }

        if (gpsLat.isNullOrBlank()) flags += AiFlag.NO_GPS_DATA

        val knownAiTools = listOf(
            "dall-e", "midjourney", "stable diffusion", "adobe firefly",
            "bing image", "canva ai", "runwayml", "imagen", "ideogram",
        )
        val softwareLower = software?.lowercase().orEmpty()
        if (knownAiTools.any { softwareLower.contains(it) }) {
            flags += AiFlag.AI_TOOL_METADATA
        }

        if (datetime.isNullOrBlank() && dateOrig.isNullOrBlank()) {
            flags += AiFlag.TIMESTAMP_MISMATCH
        }

        val score = when {
            AiFlag.AI_TOOL_METADATA in flags -> 0.92f
            AiFlag.MISSING_EXIF in flags && AiFlag.TIMESTAMP_MISMATCH in flags -> 0.60f
            AiFlag.MISSING_EXIF in flags -> 0.45f
            flags.size >= 2 -> 0.38f
            flags.size == 1 -> 0.18f
            else -> 0.04f
        }

        val verdict = when {
            score >= 0.7f -> AiVerdict.AI_GENERATED
            score >= 0.3f -> AiVerdict.SUSPICIOUS
            else -> AiVerdict.AUTHENTIC
        }

        ExifAnalysisResult(verdict, score, flags)
    }.getOrElse {
        ExifAnalysisResult(AiVerdict.SUSPICIOUS, 0.4f, listOf(AiFlag.MISSING_EXIF))
    }
}
