package com.example.core.model

import com.example.core.security.UserRole

enum class AuditEventType {
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    SIGN_UP_USER,
    PASSWORD_RESET_REQUESTED,
    PASSWORD_RESET_SUCCESS,
    PASSWORD_RESET_FAILED,
    SECURITY_PRIVILEGE_CHECK,
    BACKUP_SCHEDULED
}

data class SecurityAuditLog(
    val id: Long = 0,
    val eventType: AuditEventType,
    val targetIdentifier: String,
    val roleLevel: UserRole,
    val detail: String,
    val timestamp: Long = System.currentTimeMillis(),
    val ipAddressOrClient: String = "Client-Android-App"
)

/**
 * Blueprint model for cloud database backups and disaster recovery.
 */
data class BackupMetadata(
    val backupId: String,
    val initiatedByRole: UserRole,
    val snapshotTimestamp: Long,
    val checksum: String,
    val cloudStorageUri: String?,
    val status: String = "READY_FOR_RESTORE"
)
