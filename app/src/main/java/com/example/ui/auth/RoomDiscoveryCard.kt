package com.example.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

internal data class RoomCardInfo(
    val id: String,
    val hostUid: String,
    val host: String,
    val hostPhotoUrl: String,
    val posterUrl: String,
    val type: String,
    val members: Int,
    val createdAt: Long
)

@Composable
internal fun RoomDiscoveryCard(room: RoomCardInfo, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(186.dp).clip(RoundedCornerShape(18.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF07594F)
    ) {
        Box(Modifier.fillMaxSize()) {
            if (room.posterUrl.isNotBlank()) {
                AsyncImage(
                    model = room.posterUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(Modifier.fillMaxSize().background(
                    Brush.linearGradient(listOf(Color(0xFF11A78D), Color(0xFF064B47)))
                ), contentAlignment = Alignment.Center) {
                    Text("R", color = Color.White.copy(alpha = .42f), fontSize = 72.sp, fontWeight = FontWeight.Bold)
                }
            }
            Box(Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, Color(0xCC052D2A)))
            ))
            Text(
                "${room.members.coerceAtLeast(0)} online",
                modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
                color = Color.White, fontSize = 11.sp
            )
            Column(Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                Text(room.host, color = Color.White, fontWeight = FontWeight.Bold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 14.sp)
                Text(room.type, color = Color(0xFFE0FFF3), fontSize = 11.sp, maxLines = 1)
            }
        }
    }
}