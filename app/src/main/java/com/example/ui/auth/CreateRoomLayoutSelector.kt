package com.example.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas

/** Create Room offers one choice for each supported room capacity. */
internal fun roomLayoutForPreview(preview: Int): String {
    require(preview in 0..1)
    return if (preview == 0) "8-seat" else "15-seat"
}

@Composable
internal fun CreateRoomLayoutSelector(
    selectedPreview: Int,
    onSelect: (Int) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(2) { preview ->
            val isSelected = selectedPreview == preview
            Surface(
                modifier = Modifier
                    .size(43.dp)
                    .semantics {
                        contentDescription = if (preview == 0) "Eight-seat room layout" else "Fifteen-seat room layout"
                        selected = isSelected
                    }
                    .clickable { onSelect(preview) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) Color(0x99507970) else Color(0x665B7771),
                border = if (isSelected) BorderStroke(1.5.dp, Color(0xFF18C5A2)) else null
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    // A common 28-unit canvas keeps all four glyphs visually consistent.
                    val scale = size.minDimension / 43f
                    val inset = (size.minDimension - 28f * scale) / 2f
                    fun tile(x: Float, y: Float, w: Float, h: Float) {
                        drawRect(
                            Color.White,
                            topLeft = Offset(inset + x * scale, inset + y * scale),
                            size = Size(w * scale, h * scale)
                        )
                    }
                    when (preview) {
                        0 -> {
                            tile(2f, 3f, 11f, 10f)
                            tile(15f, 3f, 11f, 10f)
                            tile(2f, 15f, 11f, 10f)
                            tile(15f, 15f, 11f, 10f)
                        }
                        else -> for (row in 0..2) for (col in 0..2) {
                            tile(2f + col * 9f, 2f + row * 9f, 7f, 7f)
                        }
                    }
                }
            }
        }
    }
}