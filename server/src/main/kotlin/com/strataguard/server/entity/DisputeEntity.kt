package com.strataguard.server.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "disputes")
data class DisputeEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "strata_plan_id")
    val strataPlan: StrataPlanEntity? = null,

    @Column(nullable = false, length = 3)
    val state: String,

    @Column(nullable = false, length = 20)
    val tribunal: String,

    @Column(name = "dispute_type", nullable = false, length = 50)
    val disputeType: String,

    @Column(nullable = false, length = 30)
    var status: String = "draft",

    @Column(name = "risk_score")
    var riskScore: Double? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "risk_factors", columnDefinition = "jsonb")
    var riskFactors: Map<String, Any>? = null,

    @Column(name = "filing_deadline")
    var filingDeadline: LocalDate? = null,

    @Column(name = "hearing_date")
    var hearingDate: LocalDate? = null,

    var outcome: String? = null,

    @Column(columnDefinition = "TEXT")
    var notes: String? = null,

    @Column(name = "pdf_s3_key", length = 500)
    var pdfS3Key: String? = null,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "dispute_incidents",
        joinColumns = [JoinColumn(name = "dispute_id")],
        inverseJoinColumns = [JoinColumn(name = "incident_id")],
    )
    val incidents: MutableSet<IncidentEntity> = mutableSetOf(),

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant = Instant.now(),

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null,
)
