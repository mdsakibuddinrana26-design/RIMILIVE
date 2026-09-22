package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.core.model.AuditEventType
import com.example.core.model.SecurityAuditLog
import com.example.core.security.UserRole

@Entity(tableName = "security_audit_logs")
data class SecurityAuditEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventType: String,
    val targetIdentifier: String,
    val roleLevel: String,
    val detail: String,
    val timestamp: Long = System.currentTimeMillis(),
    val ipAddressOrClient: String = "Android-Client-App"
) {
    fun toDomainModel(): SecurityAuditLog {
        return SecurityAuditLog(
            id = id,
            eventType = try { AuditEventType.valueOf(eventType) } catch (_: Exception) { AuditEventType.SECURITY_PRIVILEGE_CHECK },
            targetIdentifier = targetIdentifier,
            roleLevel = UserRole.fromString(roleLevel),
            detail = detail,
            timestamp = timestamp,
            ipAddressOrClient = ipAddressOrClient
        )
    }
}
