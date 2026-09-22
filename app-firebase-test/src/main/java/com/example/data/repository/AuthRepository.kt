package com.example.data.repository

import com.example.core.model.User
import kotlinx.coroutines.flow.Flow

data class PasswordResetSession(
    val identifier: String,
    val email: String,
    val securityQuestion: String,
    val demoRecoveryCode: String
)

interface AuthRepository {
    val currentUserFlow: Flow<User?>

    suspend fun seedInitialDataIfEmpty()

    suspend fun login(identifier: String, password: String): Result<User>

    // Passwordless & Federated Authentication
    suspend fun signInWithGoogle(idToken: String? = null, email: String? = null, name: String? = null): Result<User>

    suspend fun signInWithGoogleCredential(idToken: String, email: String? = null, name: String? = null): Result<User>

    suspend fun signInWithFacebook(
        token: String? = null,
        email: String? = null,
        name: String? = null,
        firebaseUid: String? = null
    ): Result<User>

    suspend fun sendEmailOtpOrLink(email: String): Result<String>

    suspend fun verifyEmailOtpOrLink(
        email: String,
        code: String,
        username: String? = null,
        fullName: String? = null
    ): Result<User>

    suspend fun signUp(
        username: String,
        email: String,
        fullName: String,
        password: String = "",
        securityQuestion: String = "",
        securityAnswer: String = ""
    ): Result<User>

    suspend fun requestPasswordReset(identifier: String): Result<PasswordResetSession>

    suspend fun verifyResetCode(identifier: String, code: String): Result<Boolean>

    suspend fun verifySecurityAnswer(identifier: String, answer: String): Result<Boolean>

    suspend fun resetPassword(identifier: String, newPassword: String): Result<Unit>

    suspend fun logout()
}
