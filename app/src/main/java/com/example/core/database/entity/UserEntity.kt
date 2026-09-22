package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.core.model.AccountStatus
import com.example.core.model.User
import com.example.core.security.UserRole

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["username"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val email: String,
    val fullName: String,
    val passwordHash: String,
    val passwordSalt: String,
    val role: String = UserRole.USER.name,
    val accountStatus: String = AccountStatus.ACTIVE.name,
    val securityQuestion: String,
    val securityAnswerHash: String,
    val activeResetCode: String? = null,
    val resetCodeExpiresAt: Long? = null,
    val isEmailVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long? = null
) {
    fun toDomainModel(): User {
        return User(
            id = id,
            username = username,
            email = email,
            fullName = fullName,
            role = UserRole.fromString(role),
            status = try { AccountStatus.valueOf(accountStatus) } catch (_: Exception) { AccountStatus.ACTIVE },
            securityQuestion = securityQuestion,
            isEmailVerified = isEmailVerified,
            createdAt = createdAt,
            lastLoginAt = lastLoginAt
        )
    }
}
