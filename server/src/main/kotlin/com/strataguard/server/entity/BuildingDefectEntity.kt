package com.strataguard.server.entity

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "building_defects")
data class BuildingDefectEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "strata_plan_id", nullable = false)
    val strataPlan: StrataPlanEntity,

    @Column(nullable = false)
    val category: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val description: String,

    @Column(nullable = false)
    val severity: String = "minor",

    @Column(name = "order_type")
    val orderType: String? = null,

    @Column(name = "reported_date")
    val reportedDate: LocalDate? = null,

    @Column(name = "resolved_date")
    val resolvedDate: LocalDate? = null,

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    val resolutionNotes: String? = null,

    @Column(name = "source_document")
    val sourceDocument: String? = null,

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now(),
)
