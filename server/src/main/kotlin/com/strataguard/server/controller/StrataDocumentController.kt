package com.strataguard.server.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.strataguard.server.entity.StrataDocumentEntity
import com.strataguard.server.service.OcrService
import com.strataguard.server.service.StrataDocumentService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/strata/{planNumber}/documents")
class StrataDocumentController(
    private val service: StrataDocumentService,
    private val objectMapper: ObjectMapper,
) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun upload(
        @PathVariable planNumber: String,
        @RequestPart("file") file: MultipartFile,
        @RequestPart("title", required = false) title: String?,
        @RequestPart("docType", required = false) docType: String?,
    ): StrataDocumentDto {
        val entity = service.upload(
            planNumber = planNumber,
            fileBytes = file.bytes,
            filename = file.originalFilename ?: file.name,
            title = title,
            docType = docType ?: "strata_report",
        )
        return entity.toDto(objectMapper)
    }

    @GetMapping
    fun list(@PathVariable planNumber: String): List<StrataDocumentDto> =
        service.listForPlan(planNumber).map { it.toDto(objectMapper) }
}

data class StrataDocumentDto(
    val id: String,
    val planNumber: String,
    val docType: String,
    val title: String?,
    val originalFilename: String?,
    val fileSizeBytes: Long?,
    val ocrStatus: String,
    val riskFlags: List<RiskFlagDto>,
    val uploadedAt: Long,
    val processedAt: Long?,
)

data class RiskFlagDto(
    val category: String,
    val label: String,
    val severity: String,
    val context: String,
)

private fun StrataDocumentEntity.toDto(objectMapper: ObjectMapper): StrataDocumentDto {
    val flags: List<OcrService.RiskFlag> = ocrRiskFlags?.let { json ->
        runCatching {
            objectMapper.readValue(json, object : TypeReference<List<OcrService.RiskFlag>>() {})
        }.getOrElse { emptyList() }
    } ?: emptyList()

    return StrataDocumentDto(
        id = id.toString(),
        planNumber = strataPlan?.planNumber ?: "",
        docType = docType,
        title = title,
        originalFilename = originalFilename,
        fileSizeBytes = fileSizeBytes,
        ocrStatus = ocrStatus,
        riskFlags = flags.map { RiskFlagDto(it.category, it.label, it.severity, it.context) },
        uploadedAt = uploadedAt.toEpochMilli(),
        processedAt = processedAt?.toEpochMilli(),
    )
}
