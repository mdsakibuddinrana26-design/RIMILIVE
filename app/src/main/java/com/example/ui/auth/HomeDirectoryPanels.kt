package com.example.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

private val Ink = Color(0xFF184F46)

@Composable
internal fun CallDirectoryPanel(
    rooms: List<RoomCardInfo>,
    onCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hosts = rooms.filter { it.hostUid.isNotBlank() }.distinctBy { it.hostUid }
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("Chat / Call", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Hosts in live rooms • Calls require a voice service",
                color = Color(0xFFE0FFF3), fontSize = 12.sp)
        }
        if (hosts.isEmpty()) item {
            Text("No live room hosts to show. Calling is not available yet.",
                color = Color.White, modifier = Modifier.padding(vertical = 22.dp))
        }
        items(hosts, key = { it.hostUid }) { room ->
            Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFFF7FFFC)) {
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(Modifier.size(54.dp), shape = CircleShape, color = Color(0xFFD4F3E8)) {
                        if (room.hostPhotoUrl.isNotBlank()) {
                            AsyncImage(room.hostPhotoUrl, "Host avatar", Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop)
                        } else {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(room.host.take(1).uppercase(), color = Ink, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(room.host, color = Ink, fontWeight = FontWeight.Bold, maxLines = 1,
                            overflow = TextOverflow.Ellipsis)
                        Text("Hosting ${room.type} • room ${room.id}", color = Color(0xFF567E75),
                            fontSize = 11.sp, maxLines = 1)
                    }
                    Button(onClick = onCall, contentPadding = PaddingValues(horizontal = 12.dp)) {
                        Text("Call", fontSize = 12.sp)
                    }
                }
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
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("Top", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Most active live rooms • Player rankings require a ranking service",
                color = Color(0xFFE0FFF3), fontSize = 12.sp)
        }
        if (rooms.isEmpty()) item {
            Text("No live rooms to rank yet.", color = Color.White,
                modifier = Modifier.padding(vertical = 22.dp))
        }
        items(rooms.sortedWith(compareByDescending<RoomCardInfo> { it.members }.thenBy { it.id })
            .take(20).withIndex().toList(), key = { it.value.id }) { (index, room) ->
            Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenRooms),
                shape = RoundedCornerShape(16.dp), color = Color(0xFFF7FFFC)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("${index + 1}", modifier = Modifier.width(32.dp), color = Color(0xFF0A987A),
                        fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Surface(Modifier.size(48.dp), shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFD4F3E8)) {
                        if (room.posterUrl.isNotBlank()) {
                            AsyncImage(room.posterUrl, "Room poster", Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop)
                        } else {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("R", color = Ink, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(room.host, color = Ink, fontWeight = FontWeight.Bold,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(room.type, color = Color(0xFF567E75), fontSize = 12.sp)
                    }
                    Text("${room.members} in room", color = Ink, fontSize = 11.sp)
                }
            }
        }
    }
}