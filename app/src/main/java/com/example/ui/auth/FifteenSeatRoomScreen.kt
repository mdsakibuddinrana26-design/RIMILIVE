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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

private val LightText = Color.White
private val DarkPanel = Color(0x995B392A)

/** The approved eight-seat screen stays unchanged; this is its compact 15-seat counterpart. */
@Composable
internal fun FifteenSeatRoomScreen(
    state: EightSeatRoomState,
    actions: EightSeatRoomActions,
    pkScoreLine: String?
) {
    BoxWithConstraints(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFFF9B40), Color(0xFFEF702E))))
            .navigationBarsPadding()
            .imePadding()
    ) {
        val tight = maxHeight < 610.dp
        val cameraHeight = (maxHeight * 0.27f).coerceIn(102.dp, 180.dp)
        val audioRowHeight = if (tight) 50.dp else 55.dp
        Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = if (tight) 5.dp else 9.dp)) {
            Row(Modifier.fillMaxWidth().height(52.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(Modifier.weight(1f).fillMaxHeight(), color = DarkPanel,
                    shape = RoundedCornerShape(26.dp)) {
                    Row(Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        FifteenAvatar(state.hostPhoto, state.hostName, 37.dp)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(state.hostName, color = LightText, fontSize = 13.sp,
                                fontWeight = FontWeight.Bold, maxLines = 1,
                                overflow = TextOverflow.Ellipsis)
                            Text(state.roomType, color = Color(0xFFFFE2BF), fontSize = 10.sp,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                Spacer(Modifier.width(6.dp))
                Surface(Modifier.size(39.dp), shape = CircleShape, color = DarkPanel) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("${state.memberCount}", color = LightText, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.width(5.dp))
                FifteenIcon("↗", "Share room", actions.onShare)
                Spacer(Modifier.width(5.dp))
                FifteenIcon("×", "Leave room", actions.onLeave)
            }
            Spacer(Modifier.height(7.dp))
            Row(Modifier.fillMaxWidth().height(32.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Surface(
                    Modifier.weight(1f).fillMaxHeight()
                        .semantics { contentDescription = "Notice Board: ${state.notice}" }
                        .clickable(onClick = actions.onNotice),
                    color = DarkPanel, shape = RoundedCornerShape(16.dp)
                ) {
                    Box(Modifier.padding(horizontal = 11.dp), contentAlignment = Alignment.CenterStart) {
                        Text("🔊  Notice Board", color = LightText, fontSize = 12.sp, maxLines = 1)
                    }
                }
                Surface(Modifier.height(32.dp).clickable(enabled = state.isHost, onClick = actions.onPk),
                    color = DarkPanel, shape = RoundedCornerShape(16.dp)) {
                    Box(Modifier.padding(horizontal = 13.dp), contentAlignment = Alignment.Center) {
                        Text(if (state.pkRunning) "PK  %02d:%02d".format(
                            state.pkSeconds / 60, state.pkSeconds % 60
                        ) else "PK", color = LightText, fontSize = 11.sp)
                    }
                }
            }
            Spacer(Modifier.height(if (tight) 7.dp else 12.dp))

            // One host card and a compact two-by-two grid of the other four camera seats.
            Row(Modifier.fillMaxWidth().height(cameraHeight),
                horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                FifteenCameraSeat(1, state, Modifier.weight(1.05f).fillMaxHeight(),
                    onClick = { actions.onCameraSeat(1) }, host = true)
                Column(Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    (0..1).forEach { row ->
                        Row(Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            (0..1).forEach { col ->
                                val seat = 2 + row * 2 + col
                                FifteenCameraSeat(seat, state, Modifier.weight(1f).fillMaxHeight(),
                                    onClick = { actions.onCameraSeat(seat) })
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(if (tight) 6.dp else 11.dp))

            // Exactly ten circular audio seats, kept together below the camera board.
            (0..1).forEach { row ->
                Row(Modifier.fillMaxWidth().height(audioRowHeight),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    (0..4).forEach { column ->
                        val seat = 6 + row * 5 + column
                        val occupied = seat == state.mySeat || seat in state.occupied
                        val name = if (seat == state.mySeat) "You"
                            else state.names[seat].orEmpty().ifBlank { "Guest" }
                        val photo = if (seat == state.mySeat) state.myPhoto
                            else state.photos[seat].orEmpty()
                        Column(
                            Modifier.weight(1f).fillMaxHeight()
                                .semantics {
                                    contentDescription = if (occupied) "Audio seat $seat, $name"
                                    else "Empty audio seat $seat, invite"
                                }
                                .clickable { actions.onAudioSeat(seat) },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (occupied) {
                                FifteenAvatar(photo, name, 33.dp)
                            } else {
                                Surface(Modifier.size(33.dp), shape = CircleShape,
                                    color = Color(0x8858392B)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("+", color = LightText, fontSize = 21.sp)
                                    }
                                }
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(if (occupied) name else "Invite", color = LightText, fontSize = 10.sp,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                if (row == 0) Spacer(Modifier.height(if (tight) 2.dp else 5.dp))
            }

            Spacer(Modifier.weight(1f))
            Surface(Modifier.fillMaxWidth().height(if (tight) 38.dp else 68.dp),
                shape = RoundedCornerShape(13.dp), color = DarkPanel) {
                Column(Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalArrangement = Arrangement.Center) {
                    if (pkScoreLine != null) {
                        Text(pkScoreLine, color = Color(0xFFFFD88A), fontSize = 10.sp, maxLines = 1)
                    }
                    Text(state.lastMessage, color = Color(0xFFFFF1D9), fontSize = 11.sp,
                        lineHeight = 14.sp, maxLines = if (tight) 2 else 4,
                        overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(Modifier.height(7.dp))
            Row(Modifier.fillMaxWidth().height(43.dp), verticalAlignment = Alignment.CenterVertically) {
                BasicTextField(
                    value = state.chatInput, onValueChange = actions.onChatChange,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                        .background(Color(0xAA5B392A), RoundedCornerShape(25.dp))
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    textStyle = TextStyle(color = LightText, fontSize = 12.sp),
                    singleLine = true,
                    decorationBox = { inner ->
                        Box {
                            if (state.chatInput.isEmpty()) {
                                Text("Enter something...", color = Color(0xCCFFFFFF), fontSize = 12.sp)
                            }
                            inner()
                        }
                    }
                )
                Spacer(Modifier.width(6.dp))
                FifteenIcon("➤", "Send message", actions.onSend)
            }
            Spacer(Modifier.height(5.dp))
            Row(Modifier.fillMaxWidth().height(43.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                FifteenIcon("PK", "PK controls", actions.onPk, enabled = state.isHost)
                FifteenIcon("🎮", "Games", actions.onGame)
                FifteenIcon("🎁", "Gifts", actions.onGift)
                FifteenIcon("🪙", "Coins", actions.onCoin)
                FifteenIcon("•••", "More room options", actions.onMore)
                FifteenIcon("📷", if (state.cameraOn) "Turn camera off" else "Turn camera on",
                    actions.onCameraToggle,
                    enabled = state.mySeat?.let { it in 1..5 } == true, active = state.cameraOn)
                FifteenIcon("🎙", if (state.micOn) "Mute microphone" else "Unmute microphone",
                    actions.onMicToggle, active = state.micOn)
            }
        }
    }
}

@Composable
private fun FifteenAvatar(photo: String, name: String, size: Dp) {
    Surface(Modifier.size(size), shape = CircleShape, color = Color(0xFFDE9862)) {
        if (photo.isNotBlank()) {
            AsyncImage(photo, "Profile photo of $name",
                Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
        } else {
            Box(contentAlignment = Alignment.Center) {
                Text(name.take(1).uppercase().ifEmpty { "?" }, color = LightText,
                    fontWeight = FontWeight.Bold, fontSize = if (size < 40.dp) 15.sp else 19.sp)
            }
        }
    }
}

@Composable
private fun FifteenCameraSeat(
    seat: Int, state: EightSeatRoomState, modifier: Modifier,
    onClick: () -> Unit, host: Boolean = false
) {
    val occupied = host || seat == state.mySeat || seat in state.occupied
    val name = if (host) state.hostName else if (seat == state.mySeat) "You"
        else state.names[seat].orEmpty().ifBlank { "Guest" }
    val photo = if (host) state.hostPhoto else if (seat == state.mySeat)
        state.myPhoto else state.photos[seat].orEmpty()
    Surface(modifier.semantics {
        contentDescription = if (occupied) "Camera seat $seat, $name"
        else "Empty camera seat $seat, invite"
    }.clickable(onClick = onClick), shape = RoundedCornerShape(12.dp),
        color = Color(0x994C382C)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (occupied) FifteenAvatar(photo, name, if (host) 48.dp else 29.dp)
                else Surface(Modifier.size(30.dp), shape = CircleShape,
                    color = Color(0x8858392B)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("+", color = LightText, fontSize = 21.sp)
                    }
                }
                Spacer(Modifier.height(if (host) 5.dp else 2.dp))
                Text(if (occupied) name else "Invite", color = LightText,
                    fontSize = if (host) 12.sp else 10.sp, maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
                if (occupied) {
                    val cameraOn = if (seat == state.mySeat) state.cameraOn
                        else state.memberCameras[seat] == true
                    val micOn = if (seat == state.mySeat) state.micOn
                        else state.memberMics[seat] != false
                    Text("${if (cameraOn) "📷" else "📷̸"}  ${if (micOn) "🎙" else "🔇"}",
                        color = Color(0xFFFFE7CC), fontSize = 10.sp, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun FifteenIcon(
    glyph: String, label: String, onClick: () -> Unit,
    enabled: Boolean = true, active: Boolean = false
) {
    Surface(Modifier.size(39.dp).semantics { contentDescription = label }
        .clickable(enabled = enabled, onClick = onClick),
        shape = CircleShape,
        color = if (active) Color(0xFF0A947C) else DarkPanel) {
        Box(contentAlignment = Alignment.Center) {
            Text(glyph, color = if (enabled) LightText else Color(0x99FFFFFF),
                fontSize = if (glyph == "PK") 12.sp else 18.sp)
        }
    }
}