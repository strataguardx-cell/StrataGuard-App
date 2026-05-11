package com.strataguard.server.service

import com.strataguard.server.controller.dto.BuildingDefectDto
import com.strataguard.server.controller.dto.CreateStrataPlanRequest
import com.strataguard.server.controller.dto.StrataPlanDetailDto
import com.strataguard.server.controller.dto.StrataPlanSummaryDto
import com.strataguard.server.entity.StrataPlanEntity
import com.strataguard.server.exception.NotFoundException
import com.strataguard.server.repository.StrataPlanRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
@Transactional(readOnly = true)
class StrataPlanService(private val repo: StrataPlanRepository) {

    fun search(query: String, state: String): List<StrataPlanSummaryDto> {
        val q = query.trim()
        if (q.length < 2) return emptyList()

        val isPlanNumber = q.uppercase().startsWith("SP") || q.uppercase().startsWith("OC")
        return if (isPlanNumber) {
            val exact = repo.findByPlanNumberIgnoreCase(q.uppercase())
            if (exact != null) listOf(exact.toSummary())
            else repo.findByPlanNumberPrefix(q.uppercase()).map { it.toSummary() }
        } else {
            repo.searchByAddressOrSuburb(state, q).take(20).map { it.toSummary() }
        }
    }

    fun getDetail(planId: String): StrataPlanDetailDto {
        val plan = repo.findByPlanNumberIgnoreCase(planId.uppercase())
            ?: repo.findById(runCatching { UUID.fromString(planId) }.getOrElse {
                throw NotFoundException("Strata plan not found: $planId")
            }).orElseThrow { NotFoundException("Strata plan not found: $planId") }
        return plan.toDetail()
    }

    @Transactional
    fun create(request: CreateStrataPlanRequest, contributedByUid: String?): StrataPlanDetailDto {
        val existing = repo.findByPlanNumberIgnoreCase(request.planNumber.uppercase())
        if (existing != null) return existing.toDetail()

        val entity = StrataPlanEntity(
            planNumber = request.planNumber.uppercase(),
            address = request.address,
            suburb = request.suburb,
            state = request.state,
            postcode = request.postcode,
            totalLots = request.totalLots,
            yearBuilt = request.yearBuilt,
            dataSource = "user",
        )
        return repo.save(entity).toDetail()
    }

    private fun StrataPlanEntity.toSummary() = StrataPlanSummaryDto(
        id = id.toString(),
        planNumber = planNumber,
        address = address,
        suburb = suburb,
        state = state,
        postcode = postcode,
        totalLots = totalLots,
        yearBuilt = yearBuilt,
        sinkingFundStatus = sinkingFundStatus,
        activeDefectCount = defects.count { it.resolvedDate == null },
        dataSource = dataSource,
    )

    private fun StrataPlanEntity.toDetail() = StrataPlanDetailDto(
        id = id.toString(),
        planNumber = planNumber,
        address = address,
        suburb = suburb,
        state = state,
        postcode = postcode,
        totalLots = totalLots,
        yearBuilt = yearBuilt,
        registrationDate = registrationDate,
        managingAgent = managingAgent,
        managingAgentLicence = managingAgentLicence,
        lastAgm = lastAgm,
        buildingClass = buildingClass,
        sinkingFundStatus = sinkingFundStatus,
        latitude = latitude,
        longitude = longitude,
        dataSource = dataSource,
        defects = defects.map { d ->
            BuildingDefectDto(
                id = d.id.toString(),
                category = d.category,
                description = d.description,
                severity = d.severity,
                orderType = d.orderType,
                reportedDate = d.reportedDate?.toString(),
                resolvedDate = d.resolvedDate?.toString(),
                resolutionNotes = d.resolutionNotes,
                sourceDocument = d.sourceDocument,
            )
        },
        createdAt = createdAt.toEpochMilli(),
        updatedAt = updatedAt.toEpochMilli(),
    )
}
