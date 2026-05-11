package com.strataguard.server.controller

import com.strataguard.server.controller.dto.CreateStrataPlanRequest
import com.strataguard.server.controller.dto.StrataPlanDetailDto
import com.strataguard.server.controller.dto.StrataPlanSummaryDto
import com.strataguard.server.security.FirebasePrincipal
import com.strataguard.server.service.StrataPlanService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/strata")
class StrataPlanController(private val service: StrataPlanService) {

    @GetMapping("/search")
    fun search(
        @RequestParam q: String,
        @RequestParam(defaultValue = "NSW") state: String,
    ): List<StrataPlanSummaryDto> = service.search(q, state)

    @GetMapping("/{planId}")
    fun getDetail(@PathVariable planId: String): StrataPlanDetailDto =
        service.getDetail(planId)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Valid @RequestBody request: CreateStrataPlanRequest,
        @AuthenticationPrincipal principal: FirebasePrincipal?,
    ): StrataPlanDetailDto = service.create(request, principal?.uid)
}
