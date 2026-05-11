package com.strataguard.app.data.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

class FirebaseAuthRepository : AuthRepository {

    private val auth = Firebase.auth

    override val isLoggedIn get() = auth.currentUser != null
    override val currentUserId get() = auth.currentUser?.uid
    override val currentUserEmail get() = auth.currentUser?.email
    override val currentUserDisplayName get() = auth.currentUser?.displayName

    override suspend fun signIn(email: String, password: String): AuthResult<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email.trim(), password)
    }.fold(
        onSuccess = { AuthResult.Success(Unit) },
        onFailure = { AuthResult.Error(it.toUserMessage()) },
    )

    override suspend fun register(
        email: String,
        password: String,
        displayName: String,
    ): AuthResult<Unit> = runCatching {
        auth.createUserWithEmailAndPassword(email.trim(), password)
        auth.currentUser?.updateProfile(displayName = displayName.trim())
    }.fold(
        onSuccess = { AuthResult.Success(Unit) },
        onFailure = { AuthResult.Error(it.toUserMessage()) },
    )

    override suspend fun sendPasswordReset(email: String): AuthResult<Unit> = runCatching {
        auth.sendPasswordResetEmail(email.trim())
    }.fold(
        onSuccess = { AuthResult.Success(Unit) },
        onFailure = { AuthResult.Error(it.toUserMessage()) },
    )

    override suspend fun signOut() {
        auth.signOut()
    }
}

private fun Throwable.toUserMessage(): String {
    val msg = message ?: return "Something went wrong. Please try again."
    return when {
        "badly formatted" in msg || "INVALID_EMAIL" in msg ->
            "That email address doesn't look right."
        "no user record" in msg || "USER_NOT_FOUND" in msg ->
            "No account found with that email."
        "password is invalid" in msg || "INVALID_PASSWORD" in msg ||
        "INVALID_LOGIN_CREDENTIALS" in msg ->
            "Incorrect email or password."
        "email address is already in use" in msg || "EMAIL_EXISTS" in msg ->
            "An account with that email already exists."
        "password" in msg && "6 characters" in msg ->
            "Password must be at least 6 characters."
        "network" in msg || "NETWORK" in msg ->
            "No internet connection. Check your network and try again."
        "too many requests" in msg || "TOO_MANY_ATTEMPTS" in msg ->
            "Too many attempts. Please wait a moment and try again."
        else -> "Something went wrong. Please try again."
    }
}