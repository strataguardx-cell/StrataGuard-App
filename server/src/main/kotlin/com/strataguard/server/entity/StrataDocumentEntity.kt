package com.strataguard.server.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "strata_documents")
data class StrataDocumentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "strata_plan_id")
    val strataPlan: StrataPlanEntity? = null,

    @Column(name = "doc_type", nullable = false, length = 50)
    val docType: String,

    val title: String? = null,

    @Column(name = "original_filename", length = 255)
    val originalFilename: String? = null,

    @Column(name = "file_size_bytes")
    val fileSizeBytes: Long? = null,

    @Column(name = "ocr_status", nullable = false, length = 20)
    var ocrStatus: String = "pending",

    @Column(name = "ocr_extracted_text", columnDefinition = "TEXT")
    var ocrExtractedText: String? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ocr_risk_flags", columnDefinition = "jsonb")
    var ocrRiskFlags: String? = null,

    @Column(name = "uploaded_at")
    val uploadedAt: Instant = Instant.now(),

    @Column(name = "processed_at")
    var processedAt: Instant? = null,

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now(),
)
