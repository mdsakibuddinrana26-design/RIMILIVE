package com.example

import com.example.core.security.PasswordSecurity
import com.example.core.security.RolePermissions
import com.example.core.security.SecurityPolicy
import com.example.core.security.ServerPermission
import com.example.core.security.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthSecurityUnitTest {

    @Test
    fun testNormalUserRegistrationRoleIsStrictlyUser() {
        val assignedRole = SecurityPolicy.enforceNormalRegistrationRole()
        assertEquals(UserRole.USER, assignedRole)
    }

    @Test
    fun testNormalUserCannotAccessAdminPanelOrManageBackups() {
        val normalUserRole = UserRole.USER

        assertFalse("Normal user must not have access to admin panel", SecurityPolicy.canAccessAdminPanel(normalUserRole))
        assertFalse("Normal user must not have authority to manage backups", SecurityPolicy.canManageBackups(normalUserRole))
        assertFalse("Normal user cannot assign administrative roles", SecurityPolicy.canAssignRole(normalUserRole, UserRole.ADMIN))
    }

    @Test
    fun testOwnerHasBackupAndAdminPanelPrivileges() {
        val ownerRole = UserRole.OWNER

        assertTrue("Owner must be able to access admin panel", SecurityPolicy.canAccessAdminPanel(ownerRole))
        assertTrue("Owner must be able to manage backups", SecurityPolicy.canManageBackups(ownerRole))
        assertTrue("Owner can assign roles", SecurityPolicy.canAssignRole(ownerRole, UserRole.ADMIN))
    }

    @Test
    fun testRolePermissionsMatrix() {
        val userPerms = RolePermissions.getPermissionsForRole(UserRole.USER)
        assertFalse("User should not have admin panel permission", userPerms.contains(ServerPermission.ACCESS_ADMIN_PANEL))
        assertFalse("User should not have backup permission", userPerms.contains(ServerPermission.EXECUTE_DATABASE_BACKUP))

        val ownerPerms = RolePermissions.getPermissionsForRole(UserRole.OWNER)
        assertTrue("Owner must have backup permission", ownerPerms.contains(ServerPermission.EXECUTE_DATABASE_BACKUP))
        assertTrue("Owner must have admin panel permission", ownerPerms.contains(ServerPermission.ACCESS_ADMIN_PANEL))
    }

    @Test
    fun testPasswordHashingAndVerification() {
        val salt = PasswordSecurity.generateSalt()
        val password = "SecureGamiPassword2026!"
        val hash = PasswordSecurity.hashPassword(password, salt)

        assertTrue("Password verification must succeed for valid password", PasswordSecurity.verifyPassword(password, salt, hash))
        assertFalse("Password verification must fail for invalid password", PasswordSecurity.verifyPassword("WrongPassword", salt, hash))
    }
}
