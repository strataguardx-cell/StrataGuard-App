package com.strataguard.server.repository

import com.strataguard.server.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserRepository : JpaRepository<UserEntity, UUID> {
    fun findByFirebaseUid(firebaseUid: String): UserEntity?
}
