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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

private val WealthBackground = Color(0xFF031E19)
private val WealthPanel = Color(0xFF0A3028)
private val WealthPanel2 = Color(0xFF104236)
private val WealthMint = Color(0xFF8AF0C2)
private val WealthGold = Color(0xFFF3CB72)
private val WealthText = Color(0xFFF0FFF8)
private val WealthMuted = Color(0xFF9BB9AC)

private fun wealthAmount(value: Long): String =
    NumberFormat.getNumberInstance(Locale.US).format(value)

@Composable
internal fun WealthLevelScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    eligibleTotal: Long? = null
) {
    val progress = WealthLevelConfig.progressFor(eligibleTotal)
    Surface(modifier = modifier.fillMaxSize(), color = WealthBackground) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = WealthText)
                }
                Text("Wealth Level", color = WealthText, fontSize = 21.sp,
                    fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(listOf(WealthPanel2, WealthPanel)))
                    .padding(20.dp)
            ) {
                Text("RIMILIVE WEALTH", color = WealthMint, fontSize = 12.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WealthBadge(progress.level?.number)
                    Spacer(Modifier.size(16.dp))
                    Column {
                        Text(progress.level?.let { "LV${it.number}" } ?: "Pending verification",
                            color = WealthText, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                        Text(
                            if (progress.currentTotal == null)
                                "Verified eligible top-up accounting is unavailable"
                            else "Eligible lifetime top-up: ${wealthAmount(progress.currentTotal)}",
                            color = WealthMuted, fontSize = 13.sp
                        )
                    }
                }
                Spacer(Modifier.height(22.dp))
                if (progress.currentTotal == null) {
                    Text("Progress will appear after verified accounting is available.",
                        color = WealthGold, fontSize = 13.sp)
                } else {
                    LinearProgressIndicator(
                        progress = progress.fraction,
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = WealthMint, trackColor = Color(0xFF1B5143)
                    )
                    Spacer(Modifier.height(9.dp))
                    Text(
                        progress.nextLevel?.let {
                            "${wealthAmount(progress.amountToNext ?: 0)} more to LV${it.number}"
                        } ?: "Highest configured level reached",
                        color = WealthMuted, fontSize = 13.sp
                    )
                }
            }
            Spacer(Modifier.height(22.dp))
            SectionTitle("Level Rule")
            Text("Verified eligible lifetime top-up · wallet coins are not used",
                color = WealthMuted, fontSize = 12.sp)
            Spacer(Modifier.height(9.dp))
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    .background(WealthPanel).padding(vertical = 7.dp)
            ) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("LEVEL", color = WealthMint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("REQUIRED ELIGIBLE TOP-UP", color = WealthMint, fontSize = 11.sp,
                        fontWeight = FontWeight.Bold)
                }
                WealthLevelConfig.levels.forEach { level ->
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            WealthBadge(level.number, compact = true)
                            Text("LV${level.number}", color = WealthText,
                                modifier = Modifier.padding(start = 10.dp), fontWeight = FontWeight.SemiBold)
                        }
                        Text(wealthAmount(level.requiredEligibleTopUp), color = WealthText,
                            fontSize = 13.sp)
                    }
                }
            }
            Spacer(Modifier.height(22.dp))
            SectionTitle("Privileges")
            Text("Features remain unavailable until the underlying service is ready.",
                color = WealthMuted, fontSize = 12.sp)
            Spacer(Modifier.height(9.dp))
            WealthLevelConfig.privileges.forEach { privilege ->
                PrivilegeRow(privilege, progress.level?.number)
            }
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = WealthPanel2,
                    disabledContentColor = WealthMuted
                )
            ) { Text("Top Up Unavailable") }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = WealthText, fontSize = 19.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun WealthBadge(level: Int?, compact: Boolean = false) {
    Box(
        modifier = Modifier.size(if (compact) 30.dp else 62.dp).clip(CircleShape)
            .background(Brush.linearGradient(listOf(WealthGold, Color(0xFFB77A32))))
            .border(2.dp, WealthGold, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(level?.let { "L$it" } ?: "?", color = Color(0xFF382310),
            fontSize = if (compact) 9.sp else 16.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun PrivilegeRow(privilege: WealthLevelConfig.Privilege, currentLevel: Int?) {
    val locked = currentLevel == null || currentLevel < privilege.requiredLevel
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp).clip(RoundedCornerShape(14.dp))
            .background(WealthPanel).padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(if (locked) Icons.Default.Lock else Icons.Default.Schedule,
            contentDescription = null, tint = if (locked) WealthMuted else WealthGold)
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(privilege.title, color = WealthText, fontWeight = FontWeight.SemiBold)
            Text(if (locked) "Unlocks at LV${privilege.requiredLevel}" else "Coming Soon",
                color = WealthMuted, fontSize = 12.sp)
        }
        Text(if (locked) "Locked" else "Coming Soon", color = WealthGold, fontSize = 12.sp)
    }
}