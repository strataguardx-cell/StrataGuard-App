package com.strataguard.server.repository

import com.strataguard.server.entity.EvidenceItemEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface EvidenceItemRepository : JpaRepository<EvidenceItemEntity, UUID> {

    @Query("""
        SELECT e FROM EvidenceItemEntity e
        WHERE e.user.id = :userId AND e.deletedAt IS NULL
        ORDER BY e.capturedAt DESC
    """)
    fun findByUserIdActive(@Param("userId") userId: UUID): List<EvidenceItemEntity>

    @Query("""
        SELECT e FROM EvidenceItemEntity e
        WHERE e.user.id = :userId
          AND e.strataPlan.id = :strataId
          AND e.deletedAt IS NULL
        ORDER BY e.capturedAt DESC
    """)
    fun findByUserIdAndStrataIdActive(
        @Param("userId") userId: UUID,
        @Param("strataId") strataId: UUID,
    ): List<EvidenceItemEntity>

    @Query("""
        SELECT e FROM EvidenceItemEntity e
        WHERE e.id = :id AND e.user.id = :userId AND e.deletedAt IS NULL
    """)
    fun findByIdAndUserIdActive(
        @Param("id") id: UUID,
        @Param("userId") userId: UUID,
    ): EvidenceItemEntity?
}
