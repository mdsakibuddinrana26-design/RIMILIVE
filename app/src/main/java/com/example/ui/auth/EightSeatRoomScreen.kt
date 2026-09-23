package com.example.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

internal data class EightSeatRoomState(
    val hostName: String,
    val hostPhoto: String,
    val roomType: String,
    val memberCount: Int,
    val notice: String,
    val isHost: Boolean,
    val mySeat: Int?,
    val myPhoto: String,
    val occupied: Set<Int>,
    val names: Map<Int, String>,
    val photos: Map<Int, String>,
    val memberCameras: Map<Int, Boolean>,
    val memberMics: Map<Int, Boolean>,
    val cameraOn: Boolean,
    val micOn: Boolean,
    val pkRunning: Boolean,
    val pkSeconds: Int,
    val lastMessage: String,
    val chatInput: String,
    val chatFocusRequest: Int = 0
)

internal data class EightSeatRoomActions(
    val onNotice: () -> Unit,
    val onShare: () -> Unit,
    val onLeave: () -> Unit,
    val onPk: () -> Unit,
    val onCameraSeat: (Int) -> Unit,
    val onAudioSeat: (Int) -> Unit,
    val onCameraToggle: () -> Unit,
    val onMicToggle: () -> Unit,
    val onChatChange: (String) -> Unit,
    val onSend: () -> Unit,
    val onGame: () -> Unit,
    val onGift: () -> Unit,
    val onCoin: () -> Unit,
    val onMore: () -> Unit
)

private val RoomBrown = Color(0xFF723B21)
private val RoomText = Color.White

@Composable
internal fun EightSeatRoomScreen(
    state: EightSeatRoomState,
    actions: EightSeatRoomActions,
    imeInsets: WindowInsets = WindowInsets.ime,
    background: RoomBackgroundOption? = null
) {
    RoomImeWindowPolicy()
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFFF9B40), Color(0xFFEF702E))))
            .navigationBarsPadding()
    ) {
        RoomBackgroundLayer(background)
        // Fixed-sized seat rows leave the remaining height to the chat region,
        // rather than stretching seats or pushing the controls off-screen.
        val tight = maxHeight < 610.dp
        val cameraHeight = (maxHeight * 0.31f).coerceIn(114.dp, 236.dp)
        Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = if (tight) 5.dp else 9.dp)) {
            Row(Modifier.fillMaxWidth().height(52.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    color = Color(0xB459372A), shape = RoundedCornerShape(26.dp)
                ) {
                    Row(Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Avatar(state.hostPhoto, state.hostName, 37.dp)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(state.hostName, color = RoomText, fontSize = 13.sp,
                                fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(state.roomType, color = Color(0xFFFFE2BF), fontSize = 10.sp,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                Spacer(Modifier.width(6.dp))
                Surface(
                    modifier = Modifier.size(39.dp),
                    shape = CircleShape, color = Color(0x995B392A)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("${state.memberCount}", color = RoomText, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.width(5.dp))
                RoomHeaderShare(actions.onShare)
                Spacer(Modifier.width(5.dp))
                RoomIcon("×", "Leave room", actions.onLeave)
            }
            Spacer(Modifier.height(7.dp))
            Surface(
                modifier = Modifier.fillMaxWidth().height(32.dp)
                    .semantics { contentDescription = "Notice Board: ${state.notice}" }
                    .clickable(onClick = actions.onNotice),
                color = Color(0x995B392A), shape = RoundedCornerShape(16.dp)
            ) {
                Box(Modifier.padding(horizontal = 11.dp), contentAlignment = Alignment.CenterStart) {
                    Text("🔊  Notice Board", color = RoomText, fontSize = 12.sp, maxLines = 1)
                }
            }
            Spacer(Modifier.height(if (tight) 7.dp else 12.dp))

            Row(
                Modifier.fillMaxWidth().height(cameraHeight),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                CameraSeat(
                    seat = 1, state = state, modifier = Modifier.weight(1.25f).fillMaxHeight(),
                    onClick = { actions.onCameraSeat(1) }, isHostCard = true
                )
                Column(
                    Modifier.weight(.84f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    (2..3).forEach { seat ->
                        CameraSeat(
                            seat = seat, state = state, modifier = Modifier.fillMaxWidth().weight(1f),
                            onClick = { actions.onCameraSeat(seat) }
                        )
                    }
                }
            }
            Spacer(Modifier.height(if (tight) 6.dp else 11.dp))
            Row(
                Modifier.fillMaxWidth().height(70.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                (4..8).forEach { seat ->
                    val occupied = seat == state.mySeat || seat in state.occupied
                    val name = if (seat == state.mySeat) "You" else state.names[seat].orEmpty()
                    val photo = if (seat == state.mySeat) state.myPhoto else state.photos[seat].orEmpty()
                    Column(
                        Modifier.weight(1f).fillMaxHeight()
                            .semantics { contentDescription = if (occupied) "Audio seat $seat, $name" else "Empty audio seat $seat, invite" }
                            .clickable { actions.onAudioSeat(seat) },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (occupied) {
                            Avatar(photo, name, 45.dp)
                        } else {
                            Surface(Modifier.size(45.dp), shape = CircleShape, color = Color(0x8858392B)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("+", color = RoomText, fontSize = 24.sp)
                                }
                            }
                        }
                        Spacer(Modifier.height(3.dp))
                        Text(
                            if (occupied) name else "Invite",
                            color = RoomText, fontSize = 10.sp, maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            RoomLowerSectionInfo(state, actions, tight, cameraSeats = 1..3)
            Spacer(Modifier.height(44.dp))
        }
        RoomControlBar(state, actions,
            Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .windowInsetsPadding(imeInsets)
                .padding(start = 12.dp, end = 12.dp,
                    bottom = if (tight) 5.dp else 9.dp))
    }
}

@Composable
private fun Avatar(photo: String, name: String, size: androidx.compose.ui.unit.Dp) {
    Surface(Modifier.size(size), shape = CircleShape, color = Color(0xFFDE9862)) {
        if (photo.isNotBlank()) {
            AsyncImage(photo, "Profile photo of $name", Modifier.fillMaxSize().clip(CircleShape),
                contentScale = ContentScale.Crop)
        } else {
            Box(contentAlignment = Alignment.Center) {
                Text(name.take(1).uppercase().ifEmpty { "?" }, color = RoomText,
                    fontWeight = FontWeight.Bold, fontSize = 19.sp)
            }
        }
    }
}

@Composable
private fun CameraSeat(
    seat: Int, state: EightSeatRoomState, modifier: Modifier,
    onClick: () -> Unit, isHostCard: Boolean = false
) {
    val occupied = isHostCard || seat == state.mySeat || seat in state.occupied
    val name = if (isHostCard) state.hostName else if (seat == state.mySeat) "You"
        else state.names[seat].orEmpty().ifBlank { "Guest" }
    val photo = if (isHostCard) state.hostPhoto else if (seat == state.mySeat)
        state.myPhoto else state.photos[seat].orEmpty()
    Surface(
        modifier = modifier
            .semantics { contentDescription = if (occupied) "Camera seat $seat, $name" else "Empty camera seat $seat, invite" }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(13.dp),
        color = Color(0x994C382C)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (occupied) Avatar(photo, name, if (isHostCard) 58.dp else 46.dp)
                else Surface(Modifier.size(47.dp), shape = CircleShape, color = Color(0x8858392B)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("+", color = RoomText, fontSize = 26.sp)
                    }
                }
                Spacer(Modifier.height(5.dp))
                Text(if (occupied) name else "Invite", color = RoomText,
                    fontSize = if (isHostCard) 12.sp else 11.sp, maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
                if (occupied) {
                    val cameraOn = if (seat == state.mySeat) state.cameraOn
                        else state.memberCameras[seat] == true
                    val micOn = if (seat == state.mySeat) state.micOn
                        else state.memberMics[seat] != false
                    Text("${if (cameraOn) "📷" else "📷̸"}  ${if (micOn) "🎙" else "🔇"}",
                        color = Color(0xFFFFE7CC), fontSize = 11.sp, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun RoomIcon(
    glyph: String, label: String, onClick: () -> Unit,
    enabled: Boolean = true, active: Boolean = false
) {
    Surface(
        modifier = Modifier.size(39.dp)
            .semantics { contentDescription = label }
            .clickable(enabled = enabled, onClick = onClick),
        shape = CircleShape,
        color = if (active) Color(0xFF0A947C) else Color(0x995B392A)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(glyph, color = if (enabled) RoomText else Color(0x99FFFFFF),
                fontSize = if (glyph == "PK") 12.sp else 18.sp)
        }
    }
}