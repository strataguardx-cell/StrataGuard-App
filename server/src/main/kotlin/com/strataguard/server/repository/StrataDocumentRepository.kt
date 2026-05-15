package com.strataguard.server.repository

import com.strataguard.server.entity.StrataDocumentEntity
import com.strataguard.server.entity.StrataPlanEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface StrataDocumentRepository : JpaRepository<StrataDocumentEntity, UUID> {
    fun findByStrataPlanOrderByCreatedAtDesc(plan: StrataPlanEntity): List<StrataDocumentEntity>
}
