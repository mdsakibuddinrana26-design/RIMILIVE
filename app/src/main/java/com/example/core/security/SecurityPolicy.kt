package com.example.core.security

/**
 * Enforces security gates across the application.
 *
 * Mandate:
 * Normal users must NEVER receive admin privileges.
 * Public sign-up flows always hardcode role to [UserRole.USER].
 */
object SecurityPolicy {

    /**
     * Guarantees that client-initiated sign-up requests can only ever produce a standard USER.
     */
    fun enforceNormalRegistrationRole(): UserRole {
        return UserRole.USER
    }

    /**
     * Checks if a user has access to future Admin Panel interfaces.
     */
    fun canAccessAdminPanel(role: UserRole): Boolean {
        return role == UserRole.ADMIN || role == UserRole.OWNER
    }

    /**
     * Checks if a user can trigger database backups.
     */
    fun canManageBackups(role: UserRole): Boolean {
        return role == UserRole.OWNER
    }

    /**
     * Checks if a caller has authority to elevate another user's role.
     * Only the Owner account has authority to configure administrative privileges.
     */
    fun canAssignRole(callerRole: UserRole, targetRole: UserRole): Boolean {
        if (callerRole != UserRole.OWNER) return false
        if (targetRole == UserRole.OWNER) return false // Owner cannot be created via standard endpoint
        return true
    }
}
