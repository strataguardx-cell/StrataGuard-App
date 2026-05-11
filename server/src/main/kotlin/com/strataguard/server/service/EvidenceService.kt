package com.strataguard.server.service

import com.strataguard.server.controller.dto.EvidenceItemDto
import com.strataguard.server.controller.dto.UploadEvidenceRequest
import com.strataguard.server.entity.EvidenceItemEntity
import com.strataguard.server.entity.UserEntity
import com.strataguard.server.exception.NotFoundException
import com.strataguard.server.repository.EvidenceItemRepository
import com.strataguard.server.repository.StrataPlanRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Service
@Transactional(readOnly = true)
class EvidenceService(
    private val repo: EvidenceItemRepository,
    private val strataRepo: StrataPlanRepository,
    private val s3: S3Client,
    private val presigner: S3Presigner,
    @Value("\${aws.s3.bucket}") private val bucket: String,
) {

    @Transactional
    fun upload(
        file: MultipartFile,
        request: UploadEvidenceRequest,
        user: UserEntity,
    ): EvidenceItemDto {
        val s3Key = "evidence/${user.id}/${UUID.randomUUID()}.jpg"

        s3.putObject(
            PutObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .contentType(file.contentType ?: "image/jpeg")
                .contentLength(file.size)
                .build(),
            RequestBody.fromBytes(file.bytes),
        )

        val strata = request.strataId?.let {
            runCatching { strataRepo.findById(UUID.fromString(it)).orElse(null) }.getOrNull()
                ?: strataRepo.findByPlanNumberIgnoreCase(it)
        }

        val aiVerdict = request.aiDetectionVerdict
        val entity = EvidenceItemEntity(
            user = user,
            strataPlan = strata,
            type = "photo",
            title = request.title,
            description = request.description,
            s3Key = s3Key,
            capturedAt = Instant.ofEpochMilli(request.capturedAt),
            latitude = request.latitude,
            longitude = request.longitude,
            aiDetectionStatus = if (aiVerdict != null) "completed" else "pending",
            aiDetectionScore = request.aiDetectionScore,
            aiDetectionVerdict = aiVerdict,
            aiDetectionFlags = request.aiDetectionFlags,
            aiDetectionModel = if (aiVerdict != null) "on-device-exif-v1" else null,
            aiDetectedAt = if (aiVerdict != null) Instant.now() else null,
        )
        return repo.save(entity).toDto()
    }

    fun list(user: UserEntity, strataId: String?): List<EvidenceItemDto> {
        return if (strataId != null) {
            val sid = runCatching { UUID.fromString(strataId) }.getOrElse {
                return emptyList()
            }
            repo.findByUserIdAndStrataIdActive(user.id!!, sid)
        } else {
            repo.findByUserIdActive(user.id!!)
        }.map { it.toDto() }
    }

    fun getDetail(id: String, user: UserEntity): EvidenceItemDto {
        val uid = runCatching { UUID.fromString(id) }.getOrElse {
            throw NotFoundException("Evidence not found: $id")
        }
        return repo.findByIdAndUserIdActive(uid, user.id!!)?.toDto()
            ?: throw NotFoundException("Evidence not found: $id")
    }

    @Transactional
    fun softDelete(id: String, user: UserEntity) {
        val uid = runCatching { UUID.fromString(id) }.getOrElse {
            throw NotFoundException("Evidence not found: $id")
        }
        val entity = repo.findByIdAndUserIdActive(uid, user.id!!)
            ?: throw NotFoundException("Evidence not found: $id")
        entity.s3Key?.let { key ->
            runCatching {
                s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build())
            }
        }
        entity.deletedAt = Instant.now()
        repo.save(entity)
    }

    private fun EvidenceItemEntity.toDto(): EvidenceItemDto {
        val url = s3Key?.let { key ->
            runCatching {
                presigner.presignGetObject(
                    GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofHours(1))
                        .getObjectRequest(GetObjectRequest.builder().bucket(bucket).key(key).build())
                        .build()
                ).url().toString()
            }.getOrNull()
        }
        return EvidenceItemDto(
            id = id.toString(),
            type = type,
            title = title,
            description = description,
            s3Url = url,
            capturedAt = capturedAt.toEpochMilli(),
            latitude = latitude,
            longitude = longitude,
            aiDetectionStatus = aiDetectionStatus,
            aiDetectionScore = aiDetectionScore,
            aiDetectionVerdict = aiDetectionVerdict,
            aiDetectionFlags = aiDetectionFlags,
            aiDetectionModel = aiDetectionModel,
            createdAt = createdAt.toEpochMilli(),
        )
    }
}
