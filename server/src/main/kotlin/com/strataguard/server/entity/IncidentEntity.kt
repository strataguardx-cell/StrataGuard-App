package com.strataguard.server.entity

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "incidents")
data class IncidentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "strata_plan_id")
    val strataPlan: StrataPlanEntity? = null,

    @Column(nullable = false)
    val title: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "incident_date", nullable = false)
    val incidentDate: LocalDate,

    val category: String? = null,

    @Column(nullable = false, length = 20)
    var status: String = "open",

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "incident_evidence",
        joinColumns = [JoinColumn(name = "incident_id")],
        inverseJoinColumns = [JoinColumn(name = "evidence_id")],
    )
    val evidence: MutableSet<EvidenceItemEntity> = mutableSetOf(),

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant = Instant.now(),

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null,
)
