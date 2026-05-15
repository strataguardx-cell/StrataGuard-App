package com.strataguard.app.data.remote

import com.strataguard.app.data.strata.StrataDocument
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.readRawBytes
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

class StrataGuardApiClient {
    private val client = createPlatformHttpClient()

    suspend fun generatePdf(request: PdfExportRequest): Result<ByteArray> = runCatching {
        val response = client.post("$serverBaseUrl/api/v1/disputes/generate-pdf") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        response.readRawBytes()
    }

    suspend fun uploadDocument(
        planNumber: String,
        bytes: ByteArray,
        filename: String,
        title: String?,
        docType: String,
    ): Result<StrataDocument> = runCatching {
        client.post("$serverBaseUrl/api/v1/strata/$planNumber/documents") {
            setBody(MultiPartFormDataContent(formData {
                append("file", bytes, Headers.build {
                    append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
                    append(HttpHeaders.ContentType, "application/pdf")
                })
                if (title != null) append("title", title)
                append("docType", docType)
            }))
        }.body<StrataDocument>()
    }

    suspend fun listDocuments(planNumber: String): Result<List<StrataDocument>> = runCatching {
        client.get("$serverBaseUrl/api/v1/strata/$planNumber/documents").body<List<StrataDocument>>()
    }
}

@Serializable
data class PdfExportRequest(
    val disputeType: String,
    val state: String,
    val tribunal: String,
    val status: String = "draft",
    val strataAddress: String? = null,
    val filingDeadline: String? = null,
    val riskScore: Double? = null,
    val riskVerdict: String? = null,
    val riskFactors: Map<String, String>? = null,
    val incidents: List<IncidentPayload> = emptyList(),
    val evidenceItems: List<EvidencePayload> = emptyList(),
) {
    @Serializable
    data class IncidentPayload(
        val title: String,
        val description: String? = null,
        val incidentDate: String,
        val evidenceCount: Int = 0,
    )

    @Serializable
    data class EvidencePayload(
        val title: String? = null,
        val description: String? = null,
        val capturedAt: Long,
        val aiDetectionVerdict: String? = null,
    )
}
