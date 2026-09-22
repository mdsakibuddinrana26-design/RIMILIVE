package com.example.core.security

/**
 * Server-side permissions for GAMI LIVE.
 * Designed to seamlessly bridge with Cloud Database (e.g. Firebase Firestore / Cloud Functions)
 * and enforce strict access control boundaries.
 */
enum class ServerPermission(val description: String) {
    // Normal User Scope
    VIEW_PUBLIC_CHANNELS("Can browse public channels and profiles"),
    PARTICIPATE_COMMUNITY("Can interact within authorized community areas"),
    MANAGE_OWN_PROFILE("Can update personal display info and security credentials"),

    // Staff / Moderator Scope
    FLAG_CONTENT("Can submit high-priority content moderation flags"),
    TIMEOUT_USER("Can issue temporary chat timeouts"),

    // Administrator Scope
    ACCESS_ADMIN_PANEL("Can open and view the secure Admin Panel"),
    MANAGE_ACCOUNTS("Can review and inspect user account statuses"),
    VIEW_SECURITY_AUDIT_LOGS("Can view security and authentication audit logs"),

    // Owner / Super Admin Scope
    GRANT_STAFF_ROLES("Can designate moderator or admin roles"),
    EXECUTE_DATABASE_BACKUP("Can trigger server-side cloud backups"),
    RESTORE_DATABASE_BACKUP("Can perform system disaster recovery from backup"),
    CONFIGURE_SECURITY_POLICY("Can modify global server security rules")
}

object RolePermissions {
    private val USER_PERMISSIONS = setOf(
        ServerPermission.VIEW_PUBLIC_CHANNELS,
        ServerPermission.PARTICIPATE_COMMUNITY,
        ServerPermission.MANAGE_OWN_PROFILE
    )

    private val MODERATOR_PERMISSIONS = USER_PERMISSIONS + setOf(
        ServerPermission.FLAG_CONTENT,
        ServerPermission.TIMEOUT_USER
    )

    private val ADMIN_PERMISSIONS = MODERATOR_PERMISSIONS + setOf(
        ServerPermission.ACCESS_ADMIN_PANEL,
        ServerPermission.MANAGE_ACCOUNTS,
        ServerPermission.VIEW_SECURITY_AUDIT_LOGS
    )

    private val OWNER_PERMISSIONS = ServerPermission.entries.toSet()

    fun getPermissionsForRole(role: UserRole): Set<ServerPermission> {
        return when (role) {
            UserRole.USER -> USER_PERMISSIONS
            UserRole.MODERATOR -> MODERATOR_PERMISSIONS
            UserRole.ADMIN -> ADMIN_PERMISSIONS
            UserRole.OWNER -> OWNER_PERMISSIONS
        }
    }
}
