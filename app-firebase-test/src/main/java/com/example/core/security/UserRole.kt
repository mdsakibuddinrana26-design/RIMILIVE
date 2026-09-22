package com.example.core.security

/**
 * Role hierarchy for GAMI LIVE.
 *
 * CRITICAL SECURITY INVARIANT:
 * Normal user self-registration ALWAYS produces [USER].
 * Admin/Owner privileges can never be requested, granted, or escalated via normal user flow.
 */
enum class UserRole(val level: Int, val label: String) {
    USER(1, "Standard User"),
    MODERATOR(2, "Moderator"),
    ADMIN(3, "Platform Administrator"),
    OWNER(4, "System Owner");

    val isAdminOrHigher: Boolean
        get() = this == ADMIN || this == OWNER

    val isOwner: Boolean
        get() = this == OWNER

    companion object {
        fun fromString(value: String?): UserRole {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: USER
        }
    }
}
