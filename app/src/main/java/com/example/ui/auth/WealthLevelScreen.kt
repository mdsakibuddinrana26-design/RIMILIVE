package com.example.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

private val WealthBackground = Color(0xFF061D1B)
private val WealthPanel = Color(0xFF0E302C)
private val WealthPanel2 = Color(0xFF154238)
private val WealthMint = Color(0xFF91E2BE)
private val WealthGold = Color(0xFFF0CA83)
private val WealthText = Color(0xFFF4F8F2)
private val WealthMuted = Color(0xFFA4BEB3)

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
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = WealthText)
                }
                Text("WEALTH  /  LEVEL", color = WealthText, fontSize = 18.sp,
                    letterSpacing = 1.4.sp,
                    fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            WealthHero(progress)
            Spacer(Modifier.height(27.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    Text("THE ASCENT", color = WealthGold, fontSize = 11.sp,
                        letterSpacing = 2.2.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    SectionTitle("Level Rule")
                }
                Text("${WealthLevelConfig.levels.size} LEVELS", color = WealthMuted, fontSize = 10.sp,
                    letterSpacing = 1.5.sp, modifier = Modifier.padding(bottom = 5.dp))
            }
            Text("Verified eligible lifetime top-up · wallet coins are not used",
                color = WealthMuted, fontSize = 12.sp, lineHeight = 17.sp)
            Spacer(Modifier.height(14.dp))
            WealthLevelConfig.levels.forEach { level ->
                WealthStatusRow(level, progress.level?.number)
            }
            Spacer(Modifier.height(25.dp))
            SectionTitle("Privileges")
            Text("Features remain unavailable until the underlying service is ready.",
                color = WealthMuted, fontSize = 12.sp,
                modifier = Modifier.padding(top = 5.dp, bottom = 9.dp))
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
private fun WealthHero(progress: WealthProgress) {
    val shape = RoundedCornerShape(27.dp)
    Box(
        modifier = Modifier.fillMaxWidth().clip(shape)
            .background(Brush.linearGradient(listOf(Color(0xFF1C5344), Color(0xFF102F2D), Color(0xFF0A2926))))
            .border(1.dp, WealthGold.copy(alpha = .44f), shape)
            .testTag("wealth-hero")
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(WealthMint.copy(alpha = .075f), size.width * .55f,
                center = Offset(size.width * .95f, -size.height * .2f))
            drawLine(WealthGold.copy(alpha = .55f),
                Offset(size.width * .06f, 1f), Offset(size.width * .47f, 1f), 2f)
        }
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text("RIMILIVE   /   WEALTH STATUS", color = WealthGold, fontSize = 10.sp,
                letterSpacing = 1.8.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(17.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                WealthEmblem(progress.level?.number)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(progress.level?.let { "LV${it.number}" } ?: "Pending\nverification",
                        color = WealthText, fontSize = if (progress.level == null) 20.sp else 31.sp,
                        lineHeight = 24.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (progress.currentTotal == null)
                            "Verified eligible top-up accounting is unavailable"
                        else "Eligible lifetime top-up: ${wealthAmount(progress.currentTotal)}",
                        color = WealthMuted, fontSize = 12.sp, lineHeight = 17.sp
                    )
                }
            }
            Spacer(Modifier.height(19.dp))
            if (progress.currentTotal == null) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(WealthGold.copy(alpha = .28f)))
                Spacer(Modifier.height(14.dp))
                Text("Progress will appear after verified accounting is available.",
                    color = WealthGold, fontSize = 12.sp, lineHeight = 17.sp)
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("LV${progress.level?.number}", color = WealthMint, fontSize = 11.sp,
                        fontWeight = FontWeight.Bold)
                    Text(progress.nextLevel?.let { "LV${it.number}" } ?: "MAX",
                        color = WealthGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                BoxWithConstraints(
                    Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF34544A))
                ) {
                    Box(
                        Modifier.width(maxWidth * progress.fraction).height(10.dp)
                            .background(Brush.horizontalGradient(listOf(Color(0xFF3DAD83), WealthMint, WealthGold)))
                    )
                }
                Spacer(Modifier.height(9.dp))
                Text(
                    progress.nextLevel?.let { "${wealthAmount(progress.amountToNext ?: 0)} more to LV${it.number}" }
                        ?: "Highest configured level reached",
                    color = WealthText, fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun WealthStatusRow(level: WealthLevel, currentLevel: Int?) {
    val style = wealthEmblemStyle(level.number)
    val milestone = when (level.number) {
        10 -> "ELITE MILESTONE"
        20 -> "ROYAL MILESTONE"
        30 -> "APEX MILESTONE"
        else -> null
    }
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(shape)
            .background(Brush.horizontalGradient(
                listOf(
                    style.metal.copy(alpha = if (milestone != null) .30f else .19f),
                    WealthPanel,
                    Color(0xFF0A2826)
                )
            ))
            .border(1.dp, style.metal.copy(alpha = if (milestone != null) .65f else .27f), shape)
            .testTag("wealth-level-${level.number}")
            .padding(horizontal = 11.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WealthEmblem(level.number, compact = true)
        Column(Modifier.weight(1f).padding(start = 8.dp)) {
            Text("LV${level.number}", color = WealthText, fontSize = 17.sp,
                fontWeight = FontWeight.Bold)
            Text(milestone ?: if (currentLevel != null && level.number <= currentLevel) "ACHIEVED" else "WEALTH LEVEL",
                color = style.light, fontSize = 9.sp, fontWeight = FontWeight.SemiBold,
                letterSpacing = .5.sp, maxLines = 1)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("ELIGIBLE TOP-UP", color = WealthMuted, fontSize = 9.sp,
                letterSpacing = .5.sp, textAlign = TextAlign.End)
            Spacer(Modifier.height(3.dp))
            WealthCoinAmount(level.requiredEligibleTopUp, style.light)
            if (currentLevel != null && level.number > currentLevel) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null,
                        tint = WealthMuted, modifier = Modifier.size(10.dp))
                    Text(" LOCKED", color = WealthMuted, fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
private fun WealthCoinAmount(amount: Long, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(15.dp)) {
            drawCircle(Brush.linearGradient(listOf(Color(0xFFFFF0B6), WealthGold, Color(0xFF9D7536))))
            drawCircle(Color(0xFF775625), radius = size.minDimension * .33f, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2f))
            drawLine(Color(0xFF775625), Offset(size.width * .5f, size.height * .32f),
                Offset(size.width * .5f, size.height * .68f), 1.1f)
        }
        Spacer(Modifier.width(5.dp))
        Text(wealthAmount(amount), color = if (amount == 0L) WealthText else tint,
            fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = WealthText, fontSize = 21.sp, fontWeight = FontWeight.Bold)
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