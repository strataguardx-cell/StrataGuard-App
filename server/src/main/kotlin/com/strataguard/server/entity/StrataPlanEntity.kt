package com.strataguard.server.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "strata_plans")
data class StrataPlanEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "plan_number", unique = true, nullable = false)
    val planNumber: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val address: String,

    val suburb: String? = null,

    @Column(nullable = false, length = 3)
    val state: String,

    val postcode: String? = null,

    @Column(name = "total_lots")
    val totalLots: Int? = null,

    @Column(name = "year_built")
    val yearBuilt: Int? = null,

    @Column(name = "registration_date")
    val registrationDate: String? = null,

    @Column(name = "managing_agent")
    val managingAgent: String? = null,

    @Column(name = "managing_agent_licence")
    val managingAgentLicence: String? = null,

    @Column(name = "last_agm")
    val lastAgm: String? = null,

    @Column(name = "building_class")
    val buildingClass: String? = null,

    @Column(name = "sinking_fund_status")
    val sinkingFundStatus: String = "unknown",

    val latitude: Double? = null,
    val longitude: Double? = null,

    @Column(name = "data_source")
    val dataSource: String = "seed",

    @OneToMany(mappedBy = "strataPlan", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val defects: List<BuildingDefectEntity> = emptyList(),

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant = Instant.now(),
)
