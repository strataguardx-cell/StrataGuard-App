package com.strataguard.server.repository

import com.strataguard.server.entity.IncidentEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface IncidentRepository : JpaRepository<IncidentEntity, UUID> {

    @Query("""
        SELECT i FROM IncidentEntity i
        WHERE i.user.id = :userId AND i.deletedAt IS NULL
        ORDER BY i.incidentDate DESC
    """)
    fun findByUserIdActive(@Param("userId") userId: UUID): List<IncidentEntity>

    @Query("""
        SELECT i FROM IncidentEntity i
        WHERE i.user.id = :userId
          AND i.strataPlan.id = :strataId
          AND i.deletedAt IS NULL
        ORDER BY i.incidentDate DESC
    """)
    fun findByUserIdAndStrataIdActive(
        @Param("userId") userId: UUID,
        @Param("strataId") strataId: UUID,
    ): List<IncidentEntity>

    @Query("""
        SELECT i FROM IncidentEntity i
        LEFT JOIN FETCH i.evidence
        WHERE i.id = :id AND i.user.id = :userId AND i.deletedAt IS NULL
    """)
    fun findByIdAndUserIdActive(
        @Param("id") id: UUID,
        @Param("userId") userId: UUID,
    ): IncidentEntity?
}
