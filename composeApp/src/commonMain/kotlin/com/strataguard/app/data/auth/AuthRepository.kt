package com.strataguard.app.data.auth

interface AuthRepository {
    val isLoggedIn: Boolean
    val currentUserId: String?
    val currentUserEmail: String?
    val currentUserDisplayName: String?

    suspend fun signIn(email: String, password: String): AuthResult<Unit>
    suspend fun register(email: String, password: String, displayName: String): AuthResult<Unit>
    suspend fun sendPasswordReset(email: String): AuthResult<Unit>
    suspend fun signOut()
}