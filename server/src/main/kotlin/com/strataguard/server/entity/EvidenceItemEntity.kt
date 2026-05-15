package com.strataguard.server.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "evidence_items")
data class EvidenceItemEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "strata_plan_id")
    val strataPlan: StrataPlanEntity? = null,

    @Column(nullable = false, length = 20)
    val type: String = "photo",

    val title: String? = null,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "s3_key", length = 500)
    val s3Key: String? = null,

    @Column(name = "captured_at", nullable = false)
    val capturedAt: Instant,

    val latitude: Double? = null,
    val longitude: Double? = null,

    @Column(name = "ai_detection_status", nullable = false, length = 20)
    var aiDetectionStatus: String = "pending",

    @Column(name = "ai_detection_score")
    var aiDetectionScore: Double? = null,

    @Column(name = "ai_detection_verdict", length = 20)
    var aiDetectionVerdict: String? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ai_detection_flags", columnDefinition = "jsonb")
    var aiDetectionFlags: List<String>? = null,

    @Column(name = "ai_detection_model", length = 50)
    var aiDetectionModel: String? = null,

    @Column(name = "ai_detected_at")
    var aiDetectedAt: Instant? = null,

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null,
)
