package com.strataguard.server.service

import com.strataguard.server.entity.UserEntity
import com.strataguard.server.repository.UserRepository
import com.strataguard.server.security.FirebasePrincipal
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(private val repo: UserRepository) {

    @Transactional
    fun findOrCreate(principal: FirebasePrincipal): UserEntity {
        return repo.findByFirebaseUid(principal.uid) ?: repo.save(
            UserEntity(
                firebaseUid = principal.uid,
                email = principal.email ?: "${principal.uid}@unknown.com",
            )
        )
    }
}
