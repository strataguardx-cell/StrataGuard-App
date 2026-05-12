package com.strataguard.server.controller

import com.strataguard.server.controller.dto.*
import com.strataguard.server.security.FirebasePrincipal
import com.strataguard.server.service.DisputeService
import com.strataguard.server.service.PdfExportService
import com.strataguard.server.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/disputes")
class DisputeController(
    private val disputeService: DisputeService,
    private val pdfExportService: PdfExportService,
    private val userService: UserService,
) {

    @PostMapping("/generate-pdf")
    fun generatePdf(@RequestBody request: GeneratePdfRequest): ResponseEntity<ByteArray> {
        val bytes = pdfExportService.buildFromRequest(request)
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"evidence-pack.pdf\"")
            .body(bytes)
    }

    @GetMapping
    fun list(@AuthenticationPrincipal principal: FirebasePrincipal): List<DisputeDto> {
        val user = userService.findOrCreate(principal)
        return disputeService.list(user)
    }

    @GetMapping("/{id}")
    fun getDetail(
        @PathVariable id: String,
        @AuthenticationPrincipal principal: FirebasePrincipal,
    ): DisputeDetailDto {
        val user = userService.findOrCreate(principal)
        return disputeService.getDetail(id, user)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Valid @RequestBody request: CreateDisputeRequest,
        @AuthenticationPrincipal principal: FirebasePrincipal,
    ): DisputeDto {
        val user = userService.findOrCreate(principal)
        return disputeService.create(request, user)
    }

    @PostMapping("/{id}/assess")
    fun assess(
        @PathVariable id: String,
        @AuthenticationPrincipal principal: FirebasePrincipal,
    ): RiskAssessmentDto {
        val user = userService.findOrCreate(principal)
        return disputeService.assess(id, user)
    }

    @PostMapping("/{id}/export-pdf")
    fun exportPdf(
        @PathVariable id: String,
        @AuthenticationPrincipal principal: FirebasePrincipal,
    ): ExportPdfResponse {
        val user = userService.findOrCreate(principal)
        return disputeService.exportPdf(id, user)
    }

    @PostMapping("/{id}/incidents")
    fun linkIncident(
        @PathVariable id: String,
        @Valid @RequestBody request: LinkIncidentRequest,
        @AuthenticationPrincipal principal: FirebasePrincipal,
    ): DisputeDetailDto {
        val user = userService.findOrCreate(principal)
        return disputeService.linkIncident(id, request.incidentId, user)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable id: String,
        @AuthenticationPrincipal principal: FirebasePrincipal,
    ) {
        val user = userService.findOrCreate(principal)
        disputeService.softDelete(id, user)
    }
}
