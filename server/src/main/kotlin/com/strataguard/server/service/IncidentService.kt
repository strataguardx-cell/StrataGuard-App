package com.strataguard.server.service

import com.strataguard.server.controller.dto.*
import com.strataguard.server.entity.IncidentEntity
import com.strataguard.server.entity.UserEntity
import com.strataguard.server.exception.NotFoundException
import com.strataguard.server.repository.EvidenceItemRepository
import com.strataguard.server.repository.IncidentRepository
import com.strataguard.server.repository.StrataPlanRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Service
@Transactional(readOnly = true)
class IncidentService(
    private val repo: IncidentRepository,
    private val evidenceRepo: EvidenceItemRepository,
    private val strataRepo: StrataPlanRepository,
    private val evidenceService: EvidenceService,
) {

    fun list(user: UserEntity, strataId: String?): List<IncidentDto> {
        return if (strataId != null) {
            val sid = runCatching { UUID.fromString(strataId) }.getOrElse { return emptyList() }
            repo.findByUserIdAndStrataIdActive(user.id!!, sid)
        } else {
            repo.findByUserIdActive(user.id!!)
        }.map { it.toDto() }
    }

    fun getDetail(id: String, user: UserEntity): IncidentDetailDto {
        val uid = uuid(id) ?: throw NotFoundException("Incident not found: $id")
        val entity = repo.findByIdAndUserIdActive(uid, user.id!!)
            ?: throw NotFoundException("Incident not found: $id")
        return entity.toDetailDto()
    }

    @Transactional
    fun create(request: CreateIncidentRequest, user: UserEntity): IncidentDto {
        val strata = request.strataId?.let {
            runCatching { strataRepo.findById(UUID.fromString(it)).orElse(null) }.getOrNull()
                ?: strataRepo.findByPlanNumberIgnoreCase(it)
        }
        val entity = IncidentEntity(
            user = user,
            strataPlan = strata,
            title = request.title,
            description = request.description,
            incidentDate = LocalDate.parse(request.incidentDate),
            category = request.category,
        )
        return repo.save(entity).toDto()
    }

    @Transactional
    fun update(id: String, request: UpdateIncidentRequest, user: UserEntity): IncidentDto {
        val uid = uuid(id) ?: throw NotFoundException("Incident not found: $id")
        val entity = repo.findByIdAndUserIdActive(uid, user.id!!)
            ?: throw NotFoundException("Incident not found: $id")

        val updated = entity.copy(
            title = request.title ?: entity.title,
            description = request.description ?: entity.description,
            incidentDate = request.incidentDate?.let { LocalDate.parse(it) } ?: entity.incidentDate,
            category = request.category ?: entity.category,
            status = request.status ?: entity.status,
            updatedAt = Instant.now(),
        )
        return repo.save(updated).toDto()
    }

    @Transactional
    fun linkEvidence(incidentId: String, evidenceId: String, user: UserEntity): IncidentDetailDto {
        val iid = uuid(incidentId) ?: throw NotFoundException("Incident not found: $incidentId")
        val eid = uuid(evidenceId) ?: throw NotFoundException("Evidence not found: $evidenceId")

        val incident = repo.findByIdAndUserIdActive(iid, user.id!!)
            ?: throw NotFoundException("Incident not found: $incidentId")
        val evidence = evidenceRepo.findByIdAndUserIdActive(eid, user.id!!)
            ?: throw NotFoundException("Evidence not found: $evidenceId")

        incident.evidence.add(evidence)
        incident.updatedAt = Instant.now()
        return repo.save(incident).toDetailDto()
    }

    @Transactional
    fun softDelete(id: String, user: UserEntity) {
        val uid = uuid(id) ?: throw NotFoundException("Incident not found: $id")
        val entity = repo.findByIdAndUserIdActive(uid, user.id!!)
            ?: throw NotFoundException("Incident not found: $id")
        entity.deletedAt = Instant.now()
        repo.save(entity)
    }

    private fun uuid(s: String) = runCatching { UUID.fromString(s) }.getOrNull()

    private fun IncidentEntity.toDto() = IncidentDto(
        id = id.toString(),
        title = title,
        description = description,
        incidentDate = incidentDate.toString(),
        category = category,
        status = status,
        strataId = strataPlan?.id?.toString(),
        evidenceCount = evidence.size,
        createdAt = createdAt.toEpochMilli(),
        updatedAt = updatedAt.toEpochMilli(),
    )

    private fun IncidentEntity.toDetailDto() = IncidentDetailDto(
        id = id.toString(),
        title = title,
        description = description,
        incidentDate = incidentDate.toString(),
        category = category,
        status = status,
        strataId = strataPlan?.id?.toString(),
        evidence = evidence.map { e -> evidenceService.getDetail(e.id.toString(), user) },
        createdAt = createdAt.toEpochMilli(),
        updatedAt = updatedAt.toEpochMilli(),
    )
}
