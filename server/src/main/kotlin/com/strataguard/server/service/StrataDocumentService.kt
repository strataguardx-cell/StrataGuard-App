package com.strataguard.server.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.strataguard.server.entity.StrataDocumentEntity
import com.strataguard.server.repository.StrataDocumentRepository
import com.strataguard.server.repository.StrataPlanRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@Service
class StrataDocumentService(
    private val documentRepository: StrataDocumentRepository,
    private val planRepository: StrataPlanRepository,
    private val ocrService: OcrService,
    private val objectMapper: ObjectMapper,
) {

    fun upload(
        planNumber: String,
        fileBytes: ByteArray,
        filename: String,
        title: String?,
        docType: String,
    ): StrataDocumentEntity {
        val plan = planRepository.findByPlanNumberIgnoreCase(planNumber)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Strata plan not found: $planNumber")

        var doc = documentRepository.save(
            StrataDocumentEntity(
                strataPlan = plan,
                docType = docType,
                title = title?.ifBlank { null } ?: filename,
                originalFilename = filename,
                fileSizeBytes = fileBytes.size.toLong(),
                ocrStatus = "processing",
            ),
        )

        doc = try {
            val mimeType = if (filename.endsWith(".pdf", ignoreCase = true)) "application/pdf" else "image/jpeg"
            val result = ocrService.analyze(fileBytes, mimeType)
            documentRepository.save(
                doc.copy(
                    ocrStatus = "completed",
                    ocrExtractedText = result.extractedText.take(50_000),
                    ocrRiskFlags = objectMapper.writeValueAsString(result.riskFlags),
                    processedAt = Instant.now(),
                ),
            )
        } catch (e: Exception) {
            documentRepository.save(doc.copy(ocrStatus = "failed"))
        }

        return doc
    }

    fun listForPlan(planNumber: String): List<StrataDocumentEntity> {
        val plan = planRepository.findByPlanNumberIgnoreCase(planNumber) ?: return emptyList()
        return documentRepository.findByStrataPlanOrderByCreatedAtDesc(plan)
    }
}
