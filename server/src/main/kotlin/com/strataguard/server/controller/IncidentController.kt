package com.strataguard.server.controller

import com.strataguard.server.controller.dto.*
import com.strataguard.server.security.FirebasePrincipal
import com.strataguard.server.service.IncidentService
import com.strataguard.server.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/incidents")
class IncidentController(
    private val incidentService: IncidentService,
    private val userService: UserService,
) {

    @GetMapping
    fun list(
        @RequestParam(required = false) strataId: String?,
        @AuthenticationPrincipal principal: FirebasePrincipal,
    ): List<IncidentDto> {
        val user = userService.findOrCreate(principal)
        return incidentService.list(user, strataId)
    }

    @GetMapping("/{id}")
    fun getDetail(
        @PathVariable id: String,
        @AuthenticationPrincipal principal: FirebasePrincipal,
    ): IncidentDetailDto {
        val user = userService.findOrCreate(principal)
        return incidentService.getDetail(id, user)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Valid @RequestBody request: CreateIncidentRequest,
        @AuthenticationPrincipal principal: FirebasePrincipal,
    ): IncidentDto {
        val user = userService.findOrCreate(principal)
        return incidentService.create(request, user)
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: String,
        @RequestBody request: UpdateIncidentRequest,
        @AuthenticationPrincipal principal: FirebasePrincipal,
    ): IncidentDto {
        val user = userService.findOrCreate(principal)
        return incidentService.update(id, request, user)
    }

    @PostMapping("/{id}/evidence")
    fun linkEvidence(
        @PathVariable id: String,
        @Valid @RequestBody request: LinkEvidenceRequest,
        @AuthenticationPrincipal principal: FirebasePrincipal,
    ): IncidentDetailDto {
        val user = userService.findOrCreate(principal)
        return incidentService.linkEvidence(id, request.evidenceId, user)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable id: String,
        @AuthenticationPrincipal principal: FirebasePrincipal,
    ) {
        val user = userService.findOrCreate(principal)
        incidentService.softDelete(id, user)
    }
}
