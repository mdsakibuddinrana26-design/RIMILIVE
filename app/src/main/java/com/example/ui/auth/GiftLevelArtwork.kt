package com.example.ui.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class GiftMetal(val rim: Color, val shine: Color, val core: Color)

private fun giftMetal(level: Int?): GiftMetal = when (level) {
    0, null -> GiftMetal(Color(0xFF5B8184), Color(0xFF96B2AD), Color(0xFF183D43))
    1 -> GiftMetal(Color(0xFF669DA0), Color(0xFFA9CDD1), Color(0xFF194D5A))
    2 -> GiftMetal(Color(0xFF71ACB4), Color(0xFFB6E3E3), Color(0xFF1A5269))
    3 -> GiftMetal(Color(0xFF79BACA), Color(0xFFD2F1F0), Color(0xFF275472))
    4 -> GiftMetal(Color(0xFF8FC8D9), Color(0xFFE1F5EF), Color(0xFF35537B))
    5 -> GiftMetal(Color(0xFFACC6E8), Color(0xFFF1F5FF), Color(0xFF495389))
    6 -> GiftMetal(Color(0xFFB2C9F3), Color.White, Color(0xFF575A9D))
    7 -> GiftMetal(Color(0xFFB9D8FC), Color.White, Color(0xFF545DA8))
    8 -> GiftMetal(Color(0xFFC8DFFF), Color.White, Color(0xFF6054B3))
    9 -> GiftMetal(Color(0xFFD7D4FF), Color.White, Color(0xFF704CB8))
    10 -> GiftMetal(Color(0xFFF1D898), Color.White, Color(0xFF6756B7))
    else -> GiftMetal(Color(0xFF5B8184), Color(0xFF96B2AD), Color(0xFF183D43))
}

@Composable
internal fun GiftDiamondIcon(modifier: Modifier = Modifier) {
    Canvas(modifier.size(17.dp).semantics { contentDescription = "Gift diamond" }) {
        val w = size.width
        val h = size.height
        val gem = Path().apply {
            moveTo(w * .19f, h * .17f); lineTo(w * .8f, h * .17f)
            lineTo(w * .99f, h * .43f); lineTo(w * .5f, h * .93f)
            lineTo(w * .01f, h * .43f); close()
        }
        drawPath(gem, Brush.linearGradient(listOf(Color(0xFFFFFFFF), Color(0xFF6FE0E6), Color(0xFF7287D8))))
        drawPath(gem, Color(0xFFDEF9FF), style = Stroke(width = 1.1f))
        drawLine(Color.White.copy(alpha = .9f), Offset(w * .19f, h * .17f), Offset(w * .5f, h * .93f), 1f)
        drawLine(Color(0xFF275E97), Offset(w * .8f, h * .17f), Offset(w * .5f, h * .93f), 1f)
        drawLine(Color.White, Offset(w * .01f, h * .43f), Offset(w * .99f, h * .43f), 1f)
    }
}

/**
 * Gift emblems use a faceted crystal vessel, not the Recharge shield.
 * Additional facets, side rays and a crown appear gradually as levels rise.
 */
@Composable
internal fun GiftEmblem(level: Int?, compact: Boolean = false) {
    val metal = giftMetal(level)
    val rank = level ?: 0
    Box(
        Modifier.size(if (compact) 49.dp else 106.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.matchParentSize()) {
            val sx = size.width / 100f
            val sy = size.height / 100f
            withTransform({ scale(sx, sy, Offset.Zero) }) {
                if (rank >= 7) drawCircle(metal.rim.copy(alpha = .16f), 46f, Offset(50f, 51f))
                if (rank >= 5) {
                    drawLine(metal.shine, Offset(15f, 44f), Offset(1f, 35f), 2f)
                    drawLine(metal.rim, Offset(15f, 52f), Offset(2f, 54f), 2f)
                    drawLine(metal.shine, Offset(85f, 44f), Offset(99f, 35f), 2f)
                    drawLine(metal.rim, Offset(85f, 52f), Offset(98f, 54f), 2f)
                }
                val outer = Path().apply {
                    moveTo(50f, 10f); lineTo(84f, 29f); lineTo(81f, 67f)
                    lineTo(50f, 91f); lineTo(19f, 67f); lineTo(16f, 29f); close()
                }
                drawPath(outer, Brush.linearGradient(listOf(metal.shine, metal.rim, metal.core)))
                val inner = Path().apply {
                    moveTo(50f, 17f); lineTo(77f, 33f); lineTo(74f, 63f)
                    lineTo(50f, 82f); lineTo(26f, 63f); lineTo(23f, 33f); close()
                }
                drawPath(inner, Brush.linearGradient(listOf(Color(0xFF143F4A), metal.core, Color(0xFF0A243A))))
                drawPath(inner, metal.shine.copy(alpha = .8f), style = Stroke(width = if (rank >= 6) 2f else 1f))
                if (rank >= 2) {
                    drawLine(metal.shine.copy(alpha = .7f), Offset(23f, 33f), Offset(50f, 18f), 1.3f)
                    drawLine(metal.rim.copy(alpha = .8f), Offset(77f, 33f), Offset(50f, 18f), 1.3f)
                }
                if (rank >= 3) {
                    drawLine(metal.shine.copy(alpha = .6f), Offset(26f, 63f), Offset(50f, 82f), 1.2f)
                    drawLine(metal.shine.copy(alpha = .6f), Offset(74f, 63f), Offset(50f, 82f), 1.2f)
                }
                if (rank >= 4) {
                    val gem = Path().apply {
                        moveTo(50f, 22f); lineTo(58f, 31f); lineTo(50f, 40f)
                        lineTo(42f, 31f); close()
                    }
                    drawPath(gem, Brush.linearGradient(listOf(Color.White, metal.shine, metal.rim)))
                }
                if (rank >= 6) {
                    drawLine(metal.shine, Offset(32f, 40f), Offset(40f, 47f), 1.4f)
                    drawLine(metal.shine, Offset(68f, 40f), Offset(60f, 47f), 1.4f)
                }
                if (rank >= 7) {
                    drawCircle(metal.shine, 2f, Offset(12f, 18f))
                    drawCircle(metal.shine, 2f, Offset(88f, 18f))
                }
                if (rank >= 8) {
                    drawLine(metal.shine, Offset(50f, 2f), Offset(50f, 11f), 1.4f)
                    drawLine(metal.shine, Offset(45f, 6f), Offset(55f, 6f), 1.4f)
                }
                if (rank >= 9) {
                    drawCircle(Color.White, 2.2f, Offset(50f, 86f))
                    drawLine(metal.shine, Offset(6f, 69f), Offset(20f, 72f), 1.5f)
                    drawLine(metal.shine, Offset(94f, 69f), Offset(80f, 72f), 1.5f)
                }
                if (rank >= 10) {
                    val crown = Path().apply {
                        moveTo(31f, 17f); lineTo(27f, 5f); lineTo(40f, 11f)
                        lineTo(50f, 0f); lineTo(60f, 11f); lineTo(73f, 5f)
                        lineTo(69f, 17f); close()
                    }
                    drawPath(crown, Brush.linearGradient(listOf(Color.White, metal.shine, metal.rim)))
                    drawCircle(Color(0xFF9AF0F4), 2.5f, Offset(50f, 6f))
                }
            }
        }
        Text(level?.toString() ?: "·", color = Color.White, fontWeight = FontWeight.Black,
            fontSize = if (compact) 12.sp else 24.sp)
    }
}