package com.example.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val NavGold = Color(0xFFFBD06B)
private val NavMint = Color(0xFFB9F3DC)
private val destinations = listOf(
    "Home" to "Party", "Explore" to "Explore", "Post" to "Post",
    "Messages" to "Message", "Profile" to "Profile"
)

/** Shared main-app navigation; only selection changes between destinations. */
@Composable
internal fun RimiMainBottomBar(selectedTab: String, onNavigate: (String) -> Unit) {
    val active = if (selectedTab in listOf("Follow", "Party", "Chat", "Top")) "Home"
        else if (selectedTab == "Message") "Messages" else selectedTab
    Row(
        Modifier.fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(Color(0xFF003D33), Color(0xFF004B3C))))
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        destinations.forEach { (label, destination) ->
            val isSelected = active == label
            Column(
                Modifier.weight(1f)
                    .semantics { contentDescription = label; selected = isSelected }
                    .clickable { onNavigate(destination) }
                    .padding(vertical = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(38.dp),
                    color = if (isSelected) Color(0xFF087B67) else Color(0xFF075544),
                    shape = CircleShape,
                    border = BorderStroke(1.dp,
                        if (isSelected) NavGold else Color(0x6673E6BC))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        RimiNavGlyph(label, if (isSelected) NavGold else NavMint)
                    }
                }
                Text(label, color = if (isSelected) Color.White else Color(0xFFCBEBE0),
                    fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.SemiBold
                        else FontWeight.Normal, maxLines = 1)
            }
        }
    }
}

@Composable
private fun RimiNavGlyph(kind: String, color: Color) {
    Canvas(Modifier.size(22.dp)) {
        val unit = size.minDimension / 24f
        val stroke = Stroke(1.9f * unit, cap = StrokeCap.Round, join = StrokeJoin.Round)
        fun line(x: Float, y: Float, endX: Float, endY: Float) {
            drawLine(color, Offset(x * unit, y * unit), Offset(endX * unit, endY * unit),
                1.9f * unit, cap = StrokeCap.Round)
        }
        when (kind) {
            "Home" -> {
                val house = Path().apply {
                    moveTo(3*unit, 11*unit)
                    lineTo(12*unit, 3*unit)
                    lineTo(21*unit, 11*unit)
                    lineTo(21*unit, 21*unit)
                    lineTo(3*unit, 21*unit)
                    close()
                }
                drawPath(house, color, style = stroke)
                line(10f, 21f, 10f, 15f)
                line(14f, 15f, 14f, 21f)
            }
            "Explore" -> {
                drawCircle(color, 6.5f*unit, Offset(10.5f*unit, 10.5f*unit), style = stroke)
                line(15.5f, 15.5f, 21f, 21f)
            }
            "Post" -> {
                line(12f, 4f, 12f, 20f)
                line(4f, 12f, 20f, 12f)
            }
            "Messages" -> {
                val bubble = Path().apply {
                    moveTo(5*unit, 4*unit)
                    lineTo(19*unit, 4*unit)
                    quadraticTo(22*unit, 4*unit, 22*unit, 8*unit)
                    lineTo(22*unit, 15*unit)
                    quadraticTo(22*unit, 18*unit, 18*unit, 18*unit)
                    lineTo(9*unit, 18*unit)
                    lineTo(3*unit, 22*unit)
                    lineTo(4*unit, 17*unit)
                    quadraticTo(2*unit, 16*unit, 2*unit, 13*unit)
                    lineTo(2*unit, 8*unit)
                    quadraticTo(2*unit, 4*unit, 5*unit, 4*unit)
                    close()
                }
                drawPath(bubble, color, style = stroke)
                for (x in listOf(8f, 12f, 16f))
                    drawCircle(color, unit, Offset(x*unit, 11*unit))
            }
            "Profile" -> {
                drawCircle(color, 4f*unit, Offset(12*unit, 8*unit), style = stroke)
                val shoulders = Path().apply {
                    moveTo(4*unit, 21*unit)
                    cubicTo(4*unit, 13*unit, 20*unit, 13*unit, 20*unit, 21*unit)
                }
                drawPath(shoulders, color, style = stroke)
            }
        }
    }
}