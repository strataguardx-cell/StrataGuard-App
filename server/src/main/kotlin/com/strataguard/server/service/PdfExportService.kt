package com.strataguard.server.service

import com.strataguard.server.entity.DisputeEntity
import com.strataguard.server.entity.EvidenceItemEntity
import com.strataguard.server.entity.IncidentEntity
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.io.ByteArrayOutputStream
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val BOLD = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
private val REGULAR = PDType1Font(Standard14Fonts.FontName.HELVETICA)
private val DATE_FMT = DateTimeFormatter.ofPattern("d MMMM yyyy")

// RGB colour constants (0–1 range)
private const val NAVY_R = 0.106f; private const val NAVY_G = 0.165f; private const val NAVY_B = 0.290f
private const val ORANGE_R = 0.910f; private const val ORANGE_G = 0.627f; private const val ORANGE_B = 0.125f

@Service
class PdfExportService(
    private val s3: S3Client,
    private val presigner: S3Presigner,
    @Value("\${aws.s3.bucket}") private val bucket: String,
) {

    data class ExportResult(val s3Key: String, val presignedUrl: String, val expiresAt: Instant)

    fun exportEvidencePack(
        dispute: DisputeEntity,
        incidents: List<IncidentEntity>,
        evidence: List<EvidenceItemEntity>,
        assessment: DisputeRiskService.Assessment,
    ): ExportResult {
        val bytes = buildPdf(dispute, incidents, evidence, assessment)

        val s3Key = "disputes/${dispute.user.id}/${dispute.id}/evidence-pack.pdf"
        s3.putObject(
            PutObjectRequest.builder()
                .bucket(bucket).key(s3Key)
                .contentType("application/pdf")
                .contentLength(bytes.size.toLong())
                .build(),
            RequestBody.fromBytes(bytes),
        )

        val expiresAt = Instant.now().plus(Duration.ofHours(24))
        val url = presigner.presignGetObject(
            GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(24))
                .getObjectRequest(GetObjectRequest.builder().bucket(bucket).key(s3Key).build())
                .build()
        ).url().toString()

        return ExportResult(s3Key, url, expiresAt)
    }

    private fun buildPdf(
        dispute: DisputeEntity,
        incidents: List<IncidentEntity>,
        evidence: List<EvidenceItemEntity>,
        assessment: DisputeRiskService.Assessment,
    ): ByteArray {
        val doc = PDDocument()
        try {
            coverPage(doc, dispute, assessment)
            assessmentPage(doc, assessment)
            if (incidents.isNotEmpty()) timelinePage(doc, incidents)
            if (evidence.isNotEmpty()) evidencePage(doc, evidence)
            legalPage(doc, dispute)
            val out = ByteArrayOutputStream()
            doc.save(out)
            return out.toByteArray()
        } finally {
            doc.close()
        }
    }

    // ── Page builders ─────────────────────────────────────────────────────

    private fun coverPage(doc: PDDocument, dispute: DisputeEntity, assessment: DisputeRiskService.Assessment) {
        val page = addPage(doc)
        PDPageContentStream(doc, page).use { cs ->
            navy(cs); cs.addRect(0f, 720f, 595f, 121f); cs.fill()

            white(cs)
            drawText(cs, BOLD, 24f, "StrataGuard", 50f, 805f)
            drawText(cs, REGULAR, 12f, "Tribunal Evidence Pack", 50f, 778f)
            grey(cs)
            drawText(cs, REGULAR, 10f, "Generated: ${LocalDate.now().format(DATE_FMT)}", 50f, 755f)
            black(cs)

            var y = 690f
            y = labelValue(cs, "Dispute Type", disputeTypeLabel(dispute.disputeType), y)
            y = labelValue(cs, "Tribunal", "${dispute.tribunal} (${dispute.state})", y)
            y = labelValue(cs, "Status", dispute.status.replaceFirstChar { it.uppercase() }, y)
            dispute.strataPlan?.let { y = labelValue(cs, "Strata Plan", "${it.planNumber} — ${it.address}", y) }
            dispute.filingDeadline?.let { labelValue(cs, "Filing Deadline", it.format(DATE_FMT), y) }

            // Risk verdict badge
            when (assessment.verdict) {
                "STRONG" -> cs.setNonStrokingColor(0.18f, 0.63f, 0.34f)
                "MODERATE" -> cs.setNonStrokingColor(ORANGE_R, ORANGE_G, ORANGE_B)
                else -> cs.setNonStrokingColor(0.86f, 0.21f, 0.27f)
            }
            cs.addRect(50f, 560f, 220f, 36f); cs.fill()
            white(cs)
            drawText(cs, BOLD, 13f, "${assessment.verdict} CASE  ${(assessment.score * 100).toInt()}%", 65f, 574f)

            black(cs); footer(cs, 1)
        }
    }

    private fun assessmentPage(doc: PDDocument, assessment: DisputeRiskService.Assessment) {
        val page = addPage(doc)
        PDPageContentStream(doc, page).use { cs ->
            sectionHeader(cs, "Risk Assessment", 2)
            var y = 680f

            black(cs)
            drawText(cs, BOLD, 11f, "Overall Score: ${(assessment.score * 100).toInt()}% — ${assessment.verdict}", 50f, y)
            y -= 25f
            drawText(cs, BOLD, 10f, "Scoring Factors", 50f, y); y -= 18f
            for ((key, value) in assessment.factors) {
                val label = key.replace('_', ' ').replaceFirstChar { it.uppercase() }
                drawText(cs, REGULAR, 9f, "  $label: $value", 60f, y); y -= 14f
            }
            y -= 20f
            drawText(cs, BOLD, 10f, "What This Means", 50f, y); y -= 18f
            val explanation = when (assessment.verdict) {
                "STRONG" -> listOf("Your evidence and building defect record support a strong case.", "The documented evidence should be compelling for the tribunal.")
                "MODERATE" -> listOf("You have a reasonable case but could strengthen it with additional evidence", "or by documenting more incidents.")
                else -> listOf("Your case needs more supporting evidence. Capture additional photos using", "the in-app camera (auto-verified) and document all related incidents.")
            }
            for (line in explanation) { drawText(cs, REGULAR, 9f, line, 60f, y); y -= 14f }
            footer(cs, 2)
        }
    }

    private fun timelinePage(doc: PDDocument, incidents: List<IncidentEntity>) {
        val page = addPage(doc)
        PDPageContentStream(doc, page).use { cs ->
            sectionHeader(cs, "Incident Timeline", 3)
            var y = 680f
            for (incident in incidents.sortedBy { it.incidentDate }) {
                if (y < 100f) break
                cs.setNonStrokingColor(ORANGE_R, ORANGE_G, ORANGE_B)
                cs.addRect(50f, y - 2f, 4f, 16f); cs.fill()
                black(cs)
                drawText(cs, BOLD, 10f, incident.incidentDate.format(DATE_FMT), 62f, y + 10f)
                drawText(cs, BOLD, 10f, incident.title.take(80), 62f, y - 2f)
                incident.description?.take(100)?.let { drawText(cs, REGULAR, 8f, it, 62f, y - 14f) }
                val ev = incident.evidence.size
                drawText(cs, REGULAR, 8f, "$ev evidence item${if (ev != 1) "s" else ""} attached", 62f, y - 26f)
                y -= 52f
            }
            footer(cs, 3)
        }
    }

    private fun evidencePage(doc: PDDocument, evidence: List<EvidenceItemEntity>) {
        val page = addPage(doc)
        PDPageContentStream(doc, page).use { cs ->
            sectionHeader(cs, "Evidence Items", 4)
            var y = 680f
            for ((i, item) in evidence.withIndex()) {
                if (y < 80f) break
                val verdict = item.aiDetectionVerdict ?: "PENDING"
                val verdictLabel = when (verdict) {
                    "AUTHENTIC" -> "Verified"
                    "SUSPICIOUS" -> "Unverified — metadata incomplete"
                    "AI_GENERATED" -> "AI-Generated — EXCLUDED FROM PACK"
                    else -> "Pending verification"
                }
                black(cs)
                drawText(cs, BOLD, 10f, "${i + 1}. ${(item.title ?: "Evidence").take(70)}", 50f, y)
                val capturedDate = item.capturedAt.atZone(ZoneId.of("Australia/Sydney"))
                    .toLocalDate().format(DATE_FMT)
                drawText(cs, REGULAR, 8f, "Captured: $capturedDate  |  $verdictLabel", 62f, y - 14f)
                item.description?.take(110)?.let { drawText(cs, REGULAR, 8f, it, 62f, y - 26f) }
                if (verdict == "AI_GENERATED") {
                    cs.setNonStrokingColor(0.86f, 0.21f, 0.27f)
                    drawText(cs, BOLD, 8f, "Excluded: AI-generated media cannot be used as tribunal evidence.", 62f, y - 38f)
                    black(cs)
                }
                y -= 56f
            }
            footer(cs, 4)
        }
    }

    private fun legalPage(doc: PDDocument, dispute: DisputeEntity) {
        val page = addPage(doc)
        PDPageContentStream(doc, page).use { cs ->
            sectionHeader(cs, "Legal Reference", 5)
            var y = 680f
            val (legislation, rights) = when (dispute.state.uppercase()) {
                "NSW" -> "Strata Schemes Management Act 2015 (NSW)" to listOf(
                    "Owners corporation must maintain common property in good repair (s106).",
                    "Tenants may seek orders at NCAT for breach of agreement or defect rectification.",
                    "Bond disputes: apply within 6 months of vacating or discovering the damage.",
                    "NCAT filing fee: \$52 (up to \$10,000) or \$111 (over \$10,000).",
                    "Mediation is usually required before a hearing is listed.",
                )
                "VIC" -> "Owners Corporations Act 2006 (VIC)" to listOf(
                    "Owners corporation must keep common property in good repair.",
                    "Tenants may apply to VCAT for compensation or orders for repair.",
                    "Residential Tenancies Act 1997 governs bond and tenancy disputes.",
                    "Bond disputes: file within 14 days of vacating (or by written agreement).",
                    "VCAT filing fee: \$74.20 for residential tenancy disputes (2025 rate).",
                )
                else -> "Relevant state legislation" to emptyList()
            }
            black(cs)
            drawText(cs, BOLD, 10f, "Applicable Legislation", 50f, y); y -= 18f
            drawText(cs, REGULAR, 9f, legislation, 60f, y); y -= 28f
            drawText(cs, BOLD, 10f, "Key Rights and Obligations", 50f, y); y -= 18f
            for (right in rights) { drawText(cs, REGULAR, 9f, "  $right", 60f, y); y -= 14f }
            y -= 20f
            drawText(cs, BOLD, 10f, "Disclaimer", 50f, y); y -= 14f
            grey(cs)
            for (line in listOf(
                "This document is generated by StrataGuard for informational purposes only.",
                "It does not constitute legal advice. Consult a qualified legal practitioner",
                "for advice specific to your circumstances.",
            )) { drawText(cs, REGULAR, 8f, line, 60f, y); y -= 12f }
            footer(cs, 5)
        }
    }

    // ── Layout helpers ────────────────────────────────────────────────────

    private fun addPage(doc: PDDocument): PDPage = PDPage(PDRectangle.A4).also { doc.addPage(it) }

    private fun sectionHeader(cs: PDPageContentStream, title: String, pageNum: Int) {
        navy(cs); cs.addRect(0f, 720f, 595f, 80f); cs.fill()
        white(cs)
        drawText(cs, BOLD, 16f, title, 50f, 748f)
        drawText(cs, REGULAR, 9f, "StrataGuard Evidence Pack", 50f, 728f)
        black(cs)
        footer(cs, pageNum)
    }

    private fun footer(cs: PDPageContentStream, pageNum: Int) {
        grey(cs)
        drawText(cs, REGULAR, 8f, "Page $pageNum  |  StrataGuard  |  ${LocalDate.now().format(DATE_FMT)}", 50f, 20f)
        black(cs)
    }

    private fun labelValue(cs: PDPageContentStream, label: String, value: String, y: Float): Float {
        drawText(cs, BOLD, 10f, "$label:", 50f, y)
        drawText(cs, REGULAR, 10f, value.take(70), 50f + (label.length * 6f + 12f), y)
        return y - 22f
    }

    private fun drawText(cs: PDPageContentStream, font: PDType1Font, size: Float, text: String, x: Float, y: Float) {
        cs.beginText(); cs.setFont(font, size); cs.newLineAtOffset(x, y); cs.showText(text.take(100)); cs.endText()
    }

    private fun navy(cs: PDPageContentStream) = cs.setNonStrokingColor(NAVY_R, NAVY_G, NAVY_B)
    private fun white(cs: PDPageContentStream) = cs.setNonStrokingColor(1f, 1f, 1f)
    private fun black(cs: PDPageContentStream) = cs.setNonStrokingColor(0f, 0f, 0f)
    private fun grey(cs: PDPageContentStream) = cs.setNonStrokingColor(0.5f, 0.5f, 0.5f)

    private fun disputeTypeLabel(type: String) = when (type) {
        "bond" -> "Bond / Security Deposit Dispute"
        "defect_rectification" -> "Defect Rectification"
        "levy" -> "Levy Dispute"
        "noise" -> "Noise / Nuisance"
        "bylaws" -> "By-Law Enforcement"
        else -> type.replaceFirstChar { it.uppercase() }
    }
}
