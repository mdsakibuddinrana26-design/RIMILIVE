package com.example.ui.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

private val GiftSurface = Color(0xFF102C37)
private val GiftText = Color(0xFFF2FAFF)
private val GiftMuted = Color(0xFFB4C9D2)
private val GiftIce = Color(0xFFAFEAF1)
private val GiftRoyal = Color(0xFFC6BBFC)

private fun giftAmount(value: Long): String = NumberFormat.getNumberInstance(Locale.US).format(value)

private fun giftAccent(level: Int): Color = when {
    level >= 10 -> Color(0xFFF2D999)
    level >= 8 -> Color(0xFFD6C9FF)
    level >= 5 -> Color(0xFFC4D4F7)
    level >= 3 -> Color(0xFFA4DFE8)
    else -> Color(0xFF8BBFC5)
}

@Composable
internal fun GiftLevelContent(progress: GiftLevelProgress, modifier: Modifier = Modifier) {
    Column(modifier) {
        Spacer(Modifier.height(7.dp))
        GiftLevelHero(progress)
        Spacer(Modifier.height(26.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(1f)) {
                Text("THE GIFT ASCENT", color = GiftIce, fontSize = 11.sp,
                    letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("Level Rule", color = GiftText, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            }
            Text("${GiftLevelConfig.levels.size} LEVELS", color = GiftMuted, fontSize = 10.sp,
                letterSpacing = 1.5.sp, modifier = Modifier.padding(bottom = 5.dp))
        }
        Text("Only eligible, successfully received gifts count. Sent gifts and wallet Diamonds do not.",
            color = GiftMuted, fontSize = 12.sp, lineHeight = 18.sp)
        Spacer(Modifier.height(14.dp))
        GiftLevelConfig.levels.forEach { level ->
            GiftLevelRow(level, progress.level?.number)
        }
        Spacer(Modifier.height(20.dp))
        Text("Gift status requires a verified server record of received gifts. No gift progress is shown without it.",
            color = GiftMuted, fontSize = 12.sp, lineHeight = 18.sp,
            modifier = Modifier.padding(bottom = 24.dp))
    }
}

@Composable
private fun GiftLevelHero(progress: GiftLevelProgress) {
    val shape = RoundedCornerShape(27.dp)
    Box(
        Modifier.fillMaxWidth().clip(shape)
            .background(Brush.linearGradient(listOf(Color(0xFF234D5B), Color(0xFF203E58), Color(0xFF122D39))))
            .border(1.dp, GiftIce.copy(alpha = .55f), shape)
            .testTag("gift-hero")
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(GiftRoyal.copy(alpha = .09f), size.width * .53f,
                Offset(size.width * .96f, -size.height * .1f))
            drawLine(GiftIce.copy(alpha = .8f), Offset(size.width * .05f, 1f),
                Offset(size.width * .5f, 1f), 2f)
        }
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text("RIMILIVE   /   GIFT STATUS", color = GiftIce, fontSize = 10.sp,
                letterSpacing = 1.8.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(17.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                GiftEmblem(progress.level?.number)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(progress.level?.let { "LV${it.number}" } ?: "Pending\nverification",
                        color = GiftText, fontSize = if (progress.level == null) 20.sp else 31.sp,
                        lineHeight = 25.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(7.dp))
                    if (progress.verifiedReceivedTotal == null) {
                        Text("Verified received-gift accounting is unavailable",
                            color = GiftMuted, fontSize = 12.sp, lineHeight = 17.sp)
                    } else {
                        Text("Verified received gifts", color = GiftMuted, fontSize = 11.sp)
                        GiftAmount(progress.verifiedReceivedTotal, GiftText)
                    }
                }
            }
            Spacer(Modifier.height(19.dp))
            if (progress.verifiedReceivedTotal == null) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(GiftIce.copy(alpha = .28f)))
                Spacer(Modifier.height(14.dp))
                Text("Progress will appear when eligible received gifts can be verified.",
                    color = GiftIce, fontSize = 12.sp, lineHeight = 17.sp)
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("LV${progress.level?.number}", color = GiftIce, fontSize = 11.sp,
                        fontWeight = FontWeight.Bold)
                    Text(progress.nextLevel?.let { "LV${it.number}" } ?: "MAX",
                        color = GiftRoyal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                BoxWithConstraints(Modifier.fillMaxWidth().height(10.dp)
                    .clip(RoundedCornerShape(8.dp)).background(Color(0xFF3D5262))) {
                    Box(Modifier.width(maxWidth * progress.fraction).height(10.dp)
                        .background(Brush.horizontalGradient(
                            listOf(Color(0xFF5DB8C8), GiftIce, GiftRoyal))))
                }
                Spacer(Modifier.height(9.dp))
                if (progress.nextLevel != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GiftAmount(progress.amountToNext ?: 0L, GiftText)
                        Text(" more to LV${progress.nextLevel.number}", color = GiftText, fontSize = 12.sp)
                    }
                } else {
                    Text("Highest configured Gift Level reached", color = GiftText, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun GiftLevelRow(level: GiftLevel, currentLevel: Int?) {
    val accent = giftAccent(level.number)
    val current = currentLevel == level.number
    val completed = currentLevel != null && level.number < currentLevel
    val locked = currentLevel != null && level.number > currentLevel
    val shape = RoundedCornerShape(18.dp)
    Row(
        Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(shape)
            .background(Brush.horizontalGradient(
                listOf(accent.copy(alpha = if (current) .33f else if (locked) .12f else .22f),
                    GiftSurface, Color(0xFF112A31))))
            .border(1.dp, accent.copy(alpha = if (current) .85f else .36f), shape)
            .testTag("gift-level-${level.number}")
            .padding(horizontal = 11.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GiftEmblem(level.number, compact = true)
        Column(Modifier.weight(1f).padding(start = 8.dp)) {
            Text("LV${level.number}", color = GiftText, fontSize = 17.sp,
                fontWeight = FontWeight.Bold)
            Text(when {
                current -> "CURRENT"
                completed -> "COMPLETED"
                locked -> "LOCKED"
                level.number == 5 -> "VIP MILESTONE"
                level.number == 10 -> "ULTIMATE GIFT"
                else -> "GIFT LEVEL"
            }, color = accent, fontSize = 9.sp, fontWeight = FontWeight.SemiBold,
                letterSpacing = .4.sp, maxLines = 1)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("RECEIVED GIFT VALUE", color = GiftMuted, fontSize = 9.sp,
                letterSpacing = .3.sp)
            Spacer(Modifier.height(3.dp))
            GiftAmount(level.requiredReceivedValue, accent)
            if (locked) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null,
                        tint = GiftMuted, modifier = Modifier.height(10.dp))
                    Text(" LOCKED", color = GiftMuted, fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
private fun GiftAmount(value: Long, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        GiftDiamondIcon()
        Spacer(Modifier.width(5.dp))
        Text(giftAmount(value), color = tint, fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}