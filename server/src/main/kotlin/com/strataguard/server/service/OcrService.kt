package com.strataguard.server.service

import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import org.springframework.stereotype.Service

@Service
class OcrService {

    data class RiskFlag(
        val category: String,
        val label: String,
        val severity: String,
        val context: String,
    )

    data class OcrResult(
        val extractedText: String,
        val riskFlags: List<RiskFlag>,
    )

    fun analyze(fileBytes: ByteArray, mimeType: String): OcrResult {
        val text = if (mimeType == "application/pdf") extractPdfText(fileBytes) else ""
        return OcrResult(text, detectRiskFlags(text))
    }

    private fun extractPdfText(bytes: ByteArray): String = try {
        Loader.loadPDF(bytes).use { doc -> PDFTextStripper().getText(doc) }
    } catch (e: Exception) {
        ""
    }

    private fun detectRiskFlags(text: String): List<RiskFlag> {
        if (text.isBlank()) return emptyList()
        val lower = text.lowercase()
        return RISK_RULES
            .mapNotNull { rule ->
                val hit = rule.keywords.firstOrNull { lower.contains(it) } ?: return@mapNotNull null
                RiskFlag(rule.category, rule.label, rule.severity, extractContext(lower, hit))
            }
            .distinctBy { it.category }
    }

    private fun extractContext(text: String, keyword: String): String {
        val idx = text.indexOf(keyword)
        if (idx < 0) return ""
        val start = maxOf(0, idx - 80)
        val end = minOf(text.length, idx + keyword.length + 80)
        return "...${text.substring(start, end).trim()}..."
    }

    private data class RiskRule(val keywords: List<String>, val category: String, val label: String, val severity: String)

    companion object {
        private val RISK_RULES = listOf(
            RiskRule(listOf("combustible cladding", "aluminium composite panel", "acm panel"), "COMBUSTIBLE_CLADDING", "Combustible Cladding", "CRITICAL"),
            RiskRule(listOf("asbestos"), "ASBESTOS", "Asbestos", "CRITICAL"),
            RiskRule(listOf("water ingress", "water damage", "water penetration", "water leak"), "WATER_INGRESS", "Water Ingress", "HIGH"),
            RiskRule(listOf("mould", "mold", "black mould"), "MOULD", "Mould / Mold", "HIGH"),
            RiskRule(listOf("fire safety order", "fire compliance", "fire protection"), "FIRE_SAFETY", "Fire Safety", "HIGH"),
            RiskRule(listOf("structural crack", "structural defect", "structural damage", "subsidence"), "STRUCTURAL", "Structural Issue", "HIGH"),
            RiskRule(listOf("waterproofing failure", "waterproofing defect"), "WATERPROOFING", "Waterproofing Failure", "MEDIUM"),
            RiskRule(listOf("electrical fault", "electrical defect", "electrical non-compliance"), "ELECTRICAL", "Electrical Issue", "MEDIUM"),
            RiskRule(listOf("defect notice", "building defect"), "BUILDING_DEFECT", "Building Defect Notice", "MEDIUM"),
            RiskRule(listOf("rectification order"), "RECTIFICATION_ORDER", "Rectification Order", "MEDIUM"),
            RiskRule(listOf("plumbing", "drainage"), "PLUMBING", "Plumbing Issue", "LOW"),
        )
    }
}
