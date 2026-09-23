package com.example.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/** Original outlined share mark sized to match the existing room header actions. */
@Composable
internal fun RoomHeaderShare(onClick: () -> Unit) {
    val tint = Color(0xFFFFE6A3)
    Surface(
        Modifier.size(39.dp).semantics { contentDescription = "Share room" }
            .clickable(onClick = onClick),
        shape = CircleShape, color = Color(0xFF125C50),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.75f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(23.dp)) {
                val u = size.minDimension / 24f
                drawRoundRect(tint, Offset(4*u, 10*u), Size(16*u, 11*u),
                    CornerRadius(3*u), style = Stroke(1.8f*u))
                fun segment(x: Float, y: Float, x2: Float, y2: Float) {
                    drawLine(tint, Offset(x*u, y*u), Offset(x2*u, y2*u),
                        2f*u, cap = StrokeCap.Round)
                }
                segment(9f, 14f, 19f, 4f)
                segment(12f, 4f, 19f, 4f)
                segment(19f, 4f, 19f, 11f)
            }
        }
    }
}