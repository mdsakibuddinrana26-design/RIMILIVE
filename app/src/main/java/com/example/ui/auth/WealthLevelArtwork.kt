package com.example.ui.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A reusable visual emblem. All decoration is drawn at a fixed 100×100 design
 * coordinate scale, so the same artwork works on the level list and a future
 * avatar-frame surface without bundling image assets.
 */
internal data class WealthEmblemStyle(
    val metal: Color,
    val light: Color,
    val shadow: Color,
    val crystal: Color,
    val details: Int,
    val wings: Boolean = false,
    val crown: Boolean = false
)

internal fun wealthEmblemStyle(level: Int?): WealthEmblemStyle = when (level) {
    null -> WealthEmblemStyle(Color(0xFF547069), Color(0xFF859C8E), Color(0xFF152E2B), Color(0xFF9AAEA5), 0)
    0 -> WealthEmblemStyle(Color(0xFF536C62), Color(0xFF789083), Color(0xFF172C29), Color(0xFF71887C), 0)
    1 -> WealthEmblemStyle(Color(0xFFAB6A43), Color(0xFFE0A576), Color(0xFF4C2F28), Color(0xFFC2865D), 1)
    2 -> WealthEmblemStyle(Color(0xFFBD7845), Color(0xFFEFC995), Color(0xFF593A2A), Color(0xFFFFD9A0), 2)
    3 -> WealthEmblemStyle(Color(0xFF859AA5), Color(0xFFE3ECEB), Color(0xFF3A5660), Color(0xFFDEF5F5), 3)
    4 -> WealthEmblemStyle(Color(0xFFB8B7A4), Color(0xFFF2D396), Color(0xFF485C5A), Color(0xFFEFF9F0), 4)
    5 -> WealthEmblemStyle(Color(0xFFD4A447), Color(0xFFFFE6A0), Color(0xFF775021), Color(0xFFFFF2C5), 5, wings = true)
    6 -> WealthEmblemStyle(Color(0xFFE0AE4C), Color(0xFFFFF1B1), Color(0xFF805226), Color(0xFFFFF8D9), 6, wings = true)
    7 -> WealthEmblemStyle(Color(0xFFB1CFD0), Color(0xFFF7FFFF), Color(0xFF527887), Color(0xFFC5FFFF), 7, wings = true)
    8 -> WealthEmblemStyle(Color(0xFF8DE1E3), Color(0xFFE8FFFF), Color(0xFF326E81), Color(0xFFFFFFFF), 8, wings = true)
    9 -> WealthEmblemStyle(Color(0xFF8DCCE9), Color(0xFFFFFFFF), Color(0xFF345A9B), Color(0xFFF3FFFF), 9, wings = true)
    10 -> WealthEmblemStyle(Color(0xFFE8BD65), Color(0xFFFFFFFF), Color(0xFF80602D), Color(0xFFD4F8FF), 10, wings = true, crown = true)
    20 -> WealthEmblemStyle(Color(0xFFEFC873), Color(0xFFFFFFFF), Color(0xFF86664B), Color(0xFF93F5E6), 20, wings = true, crown = true)
    30 -> WealthEmblemStyle(Color(0xFFFFDF97), Color(0xFFFFFFFF), Color(0xFF886699), Color(0xFFB9F9FF), 30, wings = true, crown = true)
    else -> WealthEmblemStyle(Color(0xFF547069), Color(0xFF859C8E), Color(0xFF152E2B), Color(0xFF9AAEA5), 0)
}

private fun shield(inset: Float): Path = Path().apply {
    moveTo(50f, 13f + inset)
    lineTo(80f - inset * .48f, 22f + inset * .3f)
    lineTo(76f - inset * .3f, 61f - inset * .12f)
    quadraticTo(70f, 75f, 50f, 88f - inset)
    quadraticTo(30f, 75f, 24f + inset * .3f, 61f - inset * .12f)
    lineTo(20f + inset * .48f, 22f + inset * .3f)
    close()
}

@Composable
internal fun WealthEmblem(level: Int?, compact: Boolean = false, modifier: Modifier = Modifier) {
    val style = wealthEmblemStyle(level)
    val emblemSize = if (compact) 49.dp else 106.dp
    Box(
        modifier = modifier.size(emblemSize)
            .shadow(if (style.details >= 8) 8.dp else 3.dp, CircleShape, clip = false),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.matchParentSize()) {
            val scaleX = size.width / 100f
            val scaleY = size.height / 100f
            withTransform({ scale(scaleX, scaleY, Offset.Zero) }) {
                if (style.details >= 8) {
                    drawCircle(style.crystal.copy(alpha = .12f), 43f, Offset(50f, 47f))
                }
                if (style.wings) {
                    val left = Path().apply {
                        moveTo(24f, 35f); lineTo(4f, 27f); lineTo(13f, 42f)
                        lineTo(1f, 48f); lineTo(21f, 56f); close()
                    }
                    drawPath(left, Brush.linearGradient(listOf(style.light, style.metal, style.shadow)))
                    val right = Path().apply {
                        moveTo(76f, 35f); lineTo(96f, 27f); lineTo(87f, 42f)
                        lineTo(99f, 48f); lineTo(79f, 56f); close()
                    }
                    drawPath(right, Brush.linearGradient(listOf(style.shadow, style.metal, style.light)))
                    if (style.details >= 7) {
                        drawLine(style.light.copy(alpha = .85f), Offset(6f, 29f), Offset(24f, 40f), 1.3f)
                        drawLine(style.light.copy(alpha = .85f), Offset(94f, 29f), Offset(76f, 40f), 1.3f)
                    }
                }
                val outer = shield(0f)
                drawPath(outer, Brush.linearGradient(listOf(style.light, style.metal, style.shadow),
                    start = Offset(15f, 7f), end = Offset(83f, 85f)))
                drawPath(shield(5f), Brush.linearGradient(listOf(Color(0xFF153D37), style.shadow, Color(0xFF071C1C)),
                    start = Offset(23f, 20f), end = Offset(76f, 84f)))
                drawPath(shield(5f), style.light.copy(alpha = .86f), style = Stroke(width = 1.4f))
                if (style.details >= 2) {
                    drawPath(shield(10f), style.metal.copy(alpha = .82f), style = Stroke(width = if (style.details >= 6) 2f else 1f))
                }
                // Facet lines and an upward-pointing crystal make the precious
                // metal levels more intricate without turning the emblem into an icon.
                if (style.details >= 3) {
                    drawLine(style.light.copy(alpha = .45f), Offset(27f, 31f), Offset(43f, 26f), 1.2f)
                    drawLine(style.light.copy(alpha = .45f), Offset(73f, 31f), Offset(57f, 26f), 1.2f)
                }
                if (style.details >= 4) {
                    val jewel = Path().apply {
                        moveTo(50f, 26f); lineTo(56f, 32f); lineTo(50f, 38f)
                        lineTo(44f, 32f); close()
                    }
                    drawPath(jewel, Brush.linearGradient(listOf(style.light, style.crystal, style.metal)))
                }
                if (style.details >= 6) {
                    drawLine(style.light.copy(alpha = .8f), Offset(33f, 63f), Offset(50f, 77f), 1.5f)
                    drawLine(style.light.copy(alpha = .8f), Offset(67f, 63f), Offset(50f, 77f), 1.5f)
                }
                if (style.details >= 8) {
                    drawCircle(style.crystal, 2.2f, Offset(20f, 18f))
                    drawCircle(style.crystal, 2.2f, Offset(80f, 18f))
                }
                if (style.details >= 9) {
                    drawLine(style.crystal.copy(alpha = .8f), Offset(50f, 4f), Offset(50f, 11f), 1.4f)
                    drawLine(style.crystal.copy(alpha = .8f), Offset(46f, 8f), Offset(54f, 8f), 1.4f)
                }
                if (style.crown) {
                    val crown = Path().apply {
                        moveTo(31f, 17f); lineTo(27f, 4f); lineTo(41f, 11f)
                        lineTo(50f, 0f); lineTo(59f, 11f); lineTo(73f, 4f)
                        lineTo(69f, 17f); close()
                    }
                    drawPath(crown, Brush.linearGradient(listOf(style.light, style.metal, style.shadow)))
                    drawLine(style.light, Offset(30f, 17f), Offset(70f, 17f), 2f)
                    if (style.details >= 20) {
                        drawCircle(style.crystal, 2.5f, Offset(50f, 7f))
                        drawCircle(style.crystal, 1.8f, Offset(31f, 8f))
                        drawCircle(style.crystal, 1.8f, Offset(69f, 8f))
                    }
                    if (style.details >= 30) {
                        val crest = Path().apply {
                            moveTo(50f, 1f); lineTo(57f, 8f); lineTo(50f, 15f)
                            lineTo(43f, 8f); close()
                        }
                        drawPath(crest, Brush.linearGradient(listOf(Color.White, style.crystal, style.metal)))
                        drawLine(style.light, Offset(2f, 62f), Offset(20f, 68f), 2f)
                        drawLine(style.light, Offset(98f, 62f), Offset(80f, 68f), 2f)
                        drawCircle(style.crystal, 3f, Offset(50f, 82f))
                        drawCircle(style.light, 2f, Offset(8f, 58f))
                        drawCircle(style.light, 2f, Offset(92f, 58f))
                    }
                }
            }
        }
        Text(
            text = level?.toString() ?: "·",
            color = if (style.details >= 7) Color.White else style.light,
            fontWeight = FontWeight.Black,
            fontSize = if (compact) 12.sp else 24.sp
        )
    }
}