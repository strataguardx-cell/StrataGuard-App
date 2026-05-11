package com.strataguard.server.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users")
data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "firebase_uid", unique = true, nullable = false, length = 128)
    val firebaseUid: String,

    @Column(nullable = false)
    val email: String,

    @Column(name = "display_name")
    val displayName: String? = null,

    @Column(nullable = false, length = 3)
    val state: String = "NSW",

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant = Instant.now(),

    @Column(name = "deleted_at")
    val deletedAt: Instant? = null,
)
