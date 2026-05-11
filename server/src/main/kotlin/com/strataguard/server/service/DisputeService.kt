package com.strataguard.server.service

import com.strataguard.server.controller.dto.*
import com.strataguard.server.entity.DisputeEntity
import com.strataguard.server.entity.UserEntity
import com.strataguard.server.exception.NotFoundException
import com.strataguard.server.repository.DisputeRepository
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
class DisputeService(
    private val repo: DisputeRepository,
    private val incidentRepo: IncidentRepository,
    private val evidenceRepo: EvidenceItemRepository,
    private val strataRepo: StrataPlanRepository,
    private val riskService: DisputeRiskService,
    private val pdfService: PdfExportService,
) {

    fun list(user: UserEntity): List<DisputeDto> =
        repo.findByUserIdActive(user.id!!).map { it.toDto() }

    fun getDetail(id: String, user: UserEntity): DisputeDetailDto {
        val entity = findOrThrow(id, user)
        return entity.toDetailDto()
    }

    @Transactional
    fun create(request: CreateDisputeRequest, user: UserEntity): DisputeDto {
        val strata = request.strataId?.let {
            runCatching { strataRepo.findById(UUID.fromString(it)).orElse(null) }.getOrNull()
                ?: strataRepo.findByPlanNumberIgnoreCase(it)
        }
        val tribunal = tribunalFor(request.state)
        val entity = DisputeEntity(
            user = user,
            strataPlan = strata,
            state = request.state.uppercase(),
            tribunal = tribunal,
            disputeType = request.disputeType,
            filingDeadline = request.filingDeadline?.let { LocalDate.parse(it) },
            notes = request.notes,
        )
        return repo.save(entity).toDto()
    }

    @Transactional
    fun assess(id: String, user: UserEntity): RiskAssessmentDto {
        val dispute = findOrThrow(id, user)
        val incidents = dispute.incidents.toList()
        val evidence = incidents.flatMap { it.evidence }.distinctBy { it.id }
        val strata = dispute.strataPlan

        val result = riskService.assess(dispute, incidents, evidence, strata)

        dispute.riskScore = result.score
        dispute.riskFactors = result.factors
        dispute.updatedAt = Instant.now()
        repo.save(dispute)

        return RiskAssessmentDto(result.score, result.verdict, result.factors)
    }

    @Transactional
    fun exportPdf(id: String, user: UserEntity): ExportPdfResponse {
        val dispute = findOrThrow(id, user)
        val incidents = dispute.incidents.toList()
        val evidence = incidents.flatMap { it.evidence }.distinctBy { it.id }

        val assessment = if (dispute.riskScore != null) {
            DisputeRiskService.Assessment(
                score = dispute.riskScore!!,
                verdict = verdictFor(dispute.riskScore!!),
                factors = dispute.riskFactors ?: emptyMap(),
            )
        } else {
            val result = riskService.assess(dispute, incidents, evidence, dispute.strataPlan)
            dispute.riskScore = result.score
            dispute.riskFactors = result.factors
            result
        }

        val export = pdfService.exportEvidencePack(dispute, incidents, evidence, assessment)

        dispute.pdfS3Key = export.s3Key
        dispute.status = if (dispute.status == "draft") "ready" else dispute.status
        dispute.updatedAt = Instant.now()
        repo.save(dispute)

        return ExportPdfResponse(export.presignedUrl, export.expiresAt.toEpochMilli())
    }

    @Transactional
    fun linkIncident(disputeId: String, incidentId: String, user: UserEntity): DisputeDetailDto {
        val dispute = findOrThrow(disputeId, user)
        val iid = uuid(incidentId) ?: throw NotFoundException("Incident not found: $incidentId")
        val incident = incidentRepo.findByIdAndUserIdActive(iid, user.id!!)
            ?: throw NotFoundException("Incident not found: $incidentId")
        dispute.incidents.add(incident)
        dispute.updatedAt = Instant.now()
        return repo.save(dispute).toDetailDto()
    }

    @Transactional
    fun softDelete(id: String, user: UserEntity) {
        val entity = findOrThrow(id, user)
        entity.deletedAt = Instant.now()
        repo.save(entity)
    }

    // ── Mapping ───────────────────────────────────────────────────────────

    private fun DisputeEntity.toDto() = DisputeDto(
        id = id.toString(),
        state = state,
        tribunal = tribunal,
        disputeType = disputeType,
        status = status,
        riskScore = riskScore,
        riskVerdict = riskScore?.let { verdictFor(it) },
        filingDeadline = filingDeadline?.toString(),
        hearingDate = hearingDate?.toString(),
        outcome = outcome,
        strataId = strataPlan?.id?.toString(),
        strataAddress = strataPlan?.address,
        incidentCount = incidents.size,
        hasPdf = pdfS3Key != null,
        createdAt = createdAt.toEpochMilli(),
        updatedAt = updatedAt.toEpochMilli(),
    )

    private fun DisputeEntity.toDetailDto(): DisputeDetailDto {
        val incidentDtos = incidents.map { incident ->
            IncidentDto(
                id = incident.id.toString(),
                title = incident.title,
                description = incident.description,
                incidentDate = incident.incidentDate.toString(),
                category = incident.category,
                status = incident.status,
                strataId = incident.strataPlan?.id?.toString(),
                evidenceCount = incident.evidence.size,
                createdAt = incident.createdAt.toEpochMilli(),
                updatedAt = incident.updatedAt.toEpochMilli(),
            )
        }
        return DisputeDetailDto(
            id = id.toString(),
            state = state,
            tribunal = tribunal,
            disputeType = disputeType,
            status = status,
            riskScore = riskScore,
            riskVerdict = riskScore?.let { verdictFor(it) },
            riskFactors = riskFactors,
            filingDeadline = filingDeadline?.toString(),
            hearingDate = hearingDate?.toString(),
            outcome = outcome,
            notes = notes,
            strataId = strataPlan?.id?.toString(),
            strataAddress = strataPlan?.address,
            incidents = incidentDtos,
            pdfUrl = null, // pre-signed URL generated on demand via /export-pdf
            createdAt = createdAt.toEpochMilli(),
            updatedAt = updatedAt.toEpochMilli(),
        )
    }

    private fun findOrThrow(id: String, user: UserEntity): DisputeEntity {
        val uid = uuid(id) ?: throw NotFoundException("Dispute not found: $id")
        return repo.findByIdAndUserIdActive(uid, user.id!!)
            ?: throw NotFoundException("Dispute not found: $id")
    }

    private fun uuid(s: String) = runCatching { UUID.fromString(s) }.getOrNull()

    private fun verdictFor(score: Double) = when {
        score >= 0.65 -> "STRONG"
        score >= 0.40 -> "MODERATE"
        else -> "WEAK"
    }

    private fun tribunalFor(state: String) = when (state.uppercase()) {
        "NSW" -> "NCAT"
        "VIC" -> "VCAT"
        "QLD" -> "QCAT"
        "WA" -> "SAT"
        "SA" -> "SACAT"
        "TAS" -> "MagCourt"
        "ACT" -> "ACAT"
        "NT" -> "NTCAT"
        else -> "Tribunal"
    }
}
