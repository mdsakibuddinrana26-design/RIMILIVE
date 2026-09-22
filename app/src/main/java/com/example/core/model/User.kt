package com.example.core.model

import com.example.core.security.UserRole

enum class AccountStatus {
    ACTIVE,
    SUSPENDED,
    PENDING_VERIFICATION
}

data class User(
    val id: Long = 0,
    val username: String,
    val email: String,
    val fullName: String,
    val role: UserRole = UserRole.USER,
    val status: AccountStatus = AccountStatus.ACTIVE,
    val securityQuestion: String,
    val isEmailVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long? = null
)
