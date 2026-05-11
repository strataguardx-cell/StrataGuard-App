package com.strataguard.server.repository

import com.strataguard.server.entity.DisputeEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface DisputeRepository : JpaRepository<DisputeEntity, UUID> {

    @Query("""
        SELECT d FROM DisputeEntity d
        WHERE d.user.id = :userId AND d.deletedAt IS NULL
        ORDER BY d.createdAt DESC
    """)
    fun findByUserIdActive(@Param("userId") userId: UUID): List<DisputeEntity>

    @Query("""
        SELECT d FROM DisputeEntity d
        LEFT JOIN FETCH d.incidents
        WHERE d.id = :id AND d.user.id = :userId AND d.deletedAt IS NULL
    """)
    fun findByIdAndUserIdActive(
        @Param("id") id: UUID,
        @Param("userId") userId: UUID,
    ): DisputeEntity?
}
