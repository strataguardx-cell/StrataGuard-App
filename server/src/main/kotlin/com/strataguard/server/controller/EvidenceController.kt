package com.strataguard.server.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.strataguard.server.controller.dto.EvidenceItemDto
import com.strataguard.server.controller.dto.UploadEvidenceRequest
import com.strataguard.server.security.FirebasePrincipal
import com.strataguard.server.service.EvidenceService
import com.strataguard.server.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/evidence")
class EvidenceController(
    private val evidenceService: EvidenceService,
    private val userService: UserService,
    private val objectMapper: ObjectMapper,
) {

    /**
     * POST /api/v1/evidence
     * Multipart: field "file" (image) + field "metadata" (JSON of UploadEvidenceRequest).
     * The app sends on-device EXIF analysis results in the metadata so the server
     * stores Layer 1 results immediately without a round-trip.
     */
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun upload(
        @RequestPart("file") file: MultipartFile,
        @RequestPart("metadata") metadataJson: String,
        @AuthenticationPrincipal principal: FirebasePrincipal,
    ): EvidenceItemDto {
        val request = objectMapper.readValue(metadataJson, UploadEvidenceRequest::class.java)
        val user = userService.findOrCreate(principal)
        return evidenceService.upload(file, request, user)
    }

    @GetMapping
    fun list(
        @RequestParam(required = false) strataId: String?,
        @AuthenticationPrincipal principal: FirebasePrincipal,
    ): List<EvidenceItemDto> {
        val user = userService.findOrCreate(principal)
        return evidenceService.list(user, strataId)
    }

    @GetMapping("/{id}")
    fun getDetail(
        @PathVariable id: String,
        @AuthenticationPrincipal principal: FirebasePrincipal,
    ): EvidenceItemDto {
        val user = userService.findOrCreate(principal)
        return evidenceService.getDetail(id, user)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable id: String,
        @AuthenticationPrincipal principal: FirebasePrincipal,
    ) {
        val user = userService.findOrCreate(principal)
        evidenceService.softDelete(id, user)
    }
}
