package com.strataguard.server.repository

import com.strataguard.server.entity.StrataPlanEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface StrataPlanRepository : JpaRepository<StrataPlanEntity, UUID> {

    fun findByPlanNumberIgnoreCase(planNumber: String): StrataPlanEntity?

    fun findByStateAndSuburbIgnoreCaseOrderBySuburbAsc(state: String, suburb: String): List<StrataPlanEntity>

    @Query("""
        SELECT s FROM StrataPlanEntity s
        WHERE s.state = :state
          AND (
            LOWER(s.suburb) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(s.address) LIKE LOWER(CONCAT('%', :query, '%'))
          )
        ORDER BY s.suburb ASC
    """)
    fun searchByAddressOrSuburb(
        @Param("state") state: String,
        @Param("query") query: String,
    ): List<StrataPlanEntity>

    @Query("""
        SELECT s FROM StrataPlanEntity s
        WHERE LOWER(s.planNumber) LIKE LOWER(CONCAT(:prefix, '%'))
        ORDER BY s.planNumber ASC
    """)
    fun findByPlanNumberPrefix(@Param("prefix") prefix: String): List<StrataPlanEntity>
}
