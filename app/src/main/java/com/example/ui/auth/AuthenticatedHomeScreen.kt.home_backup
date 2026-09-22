package com.example.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.User
import com.example.core.security.RolePermissions
import com.example.core.security.UserRole
import com.example.ui.components.RoleBadge
import com.example.ui.theme.GamiAdminViolet
import com.example.ui.theme.GamiBackgroundDark
import com.example.ui.theme.GamiBorderDark
import com.example.ui.theme.GamiCyanAccent
import com.example.ui.theme.GamiError
import com.example.ui.theme.GamiIndigoLight
import com.example.ui.theme.GamiIndigoPrimary
import com.example.ui.theme.GamiOwnerGold
import com.example.ui.theme.GamiSuccess
import com.example.ui.theme.GamiSurfaceDark
import com.example.ui.theme.GamiSurfaceElevated
import com.example.ui.theme.GamiTextMuted
import com.example.ui.theme.GamiTextPrimary
import com.example.ui.theme.GamiTextSecondary

@Composable
fun AuthenticatedHomeScreen(
    user: User,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val permissions = RolePermissions.getPermissionsForRole(user.role)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0B24),
                        GamiBackgroundDark,
                        Color(0xFF0A0E1A)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "GAMI ",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = GamiTextPrimary
                    )
                    Text(
                        text = "LIVE",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = GamiCyanAccent
                    )
                }

                OutlinedButton(
                    onClick = onLogoutClick,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GamiError.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = GamiError
                    ),
                    modifier = Modifier.testTag("logout_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Log Out",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Log Out", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // User Profile Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                GamiBorderDark,
                                if (user.role == UserRole.OWNER) GamiOwnerGold else GamiIndigoPrimary,
                                GamiBorderDark
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ),
                color = GamiSurfaceDark,
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(GamiSurfaceElevated)
                            .border(
                                2.dp,
                                when (user.role) {
                                    UserRole.OWNER -> GamiOwnerGold
                                    UserRole.ADMIN -> GamiAdminViolet
                                    else -> GamiIndigoPrimary
                                },
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (user.role.isAdminOrHigher) Icons.Default.AdminPanelSettings else Icons.Default.AccountCircle,
                            contentDescription = "Avatar",
                            tint = when (user.role) {
                                UserRole.OWNER -> GamiOwnerGold
                                UserRole.ADMIN -> GamiAdminViolet
                                else -> GamiIndigoLight
                            },
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = user.fullName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = GamiTextPrimary
                    )

                    Text(
                        text = "@${user.username} • ${user.email}",
                        fontSize = 13.sp,
                        color = GamiTextSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    RoleBadge(role = user.role)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Security Isolation & Architecture Blueprint Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, GamiBorderDark, RoundedCornerShape(20.dp)),
                color = GamiSurfaceElevated.copy(alpha = 0.8f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = GamiCyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SECURITY & ROLE ISOLATION",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GamiCyanAccent,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (user.role == UserRole.USER) {
                        Text(
                            text = "✅ Protected Consumer Account: Normal users are strictly restricted to consumer permissions. Access to the Admin Panel, user management, and cloud backup endpoints is blocked at the core permission gate.",
                            fontSize = 13.sp,
                            color = GamiTextSecondary,
                            lineHeight = 18.sp
                        )
                    } else {
                        Text(
                            text = "👑 Privileged System Account: Authorized with administrative oversight. Configured with access to server backup operations and future Admin Panel management.",
                            fontSize = 13.sp,
                            color = GamiOwnerGold,
                            lineHeight = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Active Server Permissions (${permissions.size}):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GamiTextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    permissions.forEach { perm ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = if (perm.name.contains("BACKUP") || perm.name.contains("ADMIN")) GamiOwnerGold else GamiSuccess,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = perm.description,
                                fontSize = 12.sp,
                                color = GamiTextMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cloud Database & Roadmap Info Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, GamiBorderDark, RoundedCornerShape(16.dp)),
                color = GamiSurfaceDark,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = GamiIndigoLight,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ARCHITECTURE FOUNDATION READY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GamiIndigoLight,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Phase 1 scope verified: Clean Login, Sign Up, and Forgot Password recovery. Streaming, calls, games, coins, and ads are strictly disabled until future phases.",
                        fontSize = 12.sp,
                        color = GamiTextMuted,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
