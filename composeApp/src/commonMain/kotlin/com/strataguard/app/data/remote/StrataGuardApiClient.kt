package com.strataguard.app.data.remote

import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.readRawBytes
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

// 10.0.2.2 is the Android emulator's alias for the host machine's localhost
private const val BASE_URL = "http://10.0.2.2:8080"

class StrataGuardApiClient {
    private val client = createPlatformHttpClient()

    suspend fun generatePdf(request: PdfExportRequest): Result<ByteArray> = runCatching {
        val response = client.post("$BASE_URL/api/v1/disputes/generate-pdf") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        response.readRawBytes()
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
