package com.example.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/** Matches the Party discovery card's 186dp portrait, 18dp corners and 12dp grid gap. */
@Composable
private fun DirectoryPortraitCard(
    imageUrl: String,
    fallback: String,
    description: String,
    modifier: Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        modifier = modifier.height(186.dp).clip(RoundedCornerShape(18.dp))
            .semantics { contentDescription = description }
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF07594F)
    ) {
        Box(Modifier.fillMaxSize()) {
            if (imageUrl.isNotBlank()) {
                AsyncImage(imageUrl, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(
                    Color(0xFF11A78D), Color(0xFF064B47)
                ))), contentAlignment = Alignment.Center) {
                    Text(fallback.take(1).uppercase().ifEmpty { "?" },
                        color = Color.White.copy(alpha = .42f),
                        fontSize = 72.sp, fontWeight = FontWeight.Bold)
                }
            }
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(
                Color.Transparent, Color.Transparent, Color(0xCC052D2A)
            ))))
            content()
        }
    }
}

@Composable
internal fun CallDirectoryPanel(
    rooms: List<RoomCardInfo>,
    onCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hosts = rooms.filter { it.hostUid.isNotBlank() }.distinctBy { it.hostUid }
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Chat / Call", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Hosts in live rooms • Calls require a voice service",
                color = Color(0xFFE0FFF3), fontSize = 12.sp)
        }
        if (hosts.isEmpty()) item {
            Text("No live room hosts to show. Calling is not available yet.",
                color = Color.White, modifier = Modifier.padding(vertical = 22.dp))
        }
        items(hosts.chunked(2)) { pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                pair.forEach { room ->
                    DirectoryPortraitCard(
                        imageUrl = room.hostPhotoUrl,
                        fallback = room.host,
                        description = "Chat host ${room.host}, room ${room.id}",
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("${room.members.coerceAtLeast(0)} in room",
                            Modifier.align(Alignment.TopEnd).padding(10.dp),
                            color = Color.White, fontSize = 11.sp)
                        Column(Modifier.align(Alignment.BottomStart).fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 9.dp)) {
                            Text(room.host, color = Color.White, fontWeight = FontWeight.Bold,
                                fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("Hosting ${room.type} • room ${room.id}",
                                color = Color(0xFFE0FFF3), fontSize = 10.sp, maxLines = 1,
                                overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.height(5.dp))
                            Button(
                                onClick = onCall,
                                modifier = Modifier.height(34.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                            ) { Text("Call", fontSize = 12.sp) }
                        }
                    }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
internal fun TopRoomsPanel(
    rooms: List<RoomCardInfo>,
    onOpenRooms: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ranked = rooms.sortedWith(compareByDescending<RoomCardInfo> { it.members }.thenBy { it.id })
        .take(20).withIndex().toList()
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Top", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Most active live rooms • Player rankings require a ranking service",
                color = Color(0xFFE0FFF3), fontSize = 12.sp)
        }
        if (ranked.isEmpty()) item {
            Text("No live rooms to rank yet.", color = Color.White,
                modifier = Modifier.padding(vertical = 22.dp))
        }
        items(ranked.chunked(2)) { pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                pair.forEach { (index, room) ->
                    DirectoryPortraitCard(
                        imageUrl = room.posterUrl.ifBlank { room.hostPhotoUrl },
                        fallback = "R",
                        description = "Rank ${index + 1}, room ${room.id}",
                        modifier = Modifier.weight(1f),
                        onClick = onOpenRooms
                    ) {
                        Text("#${index + 1}", Modifier.align(Alignment.TopStart).padding(10.dp),
                            color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("${room.members.coerceAtLeast(0)} in room",
                            Modifier.align(Alignment.TopEnd).padding(10.dp),
                            color = Color.White, fontSize = 11.sp)
                        Column(Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(12.dp)) {
                            Text(room.host, color = Color.White, fontWeight = FontWeight.Bold,
                                maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 14.sp)
                            Text(room.type, color = Color(0xFFE0FFF3), fontSize = 11.sp,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}