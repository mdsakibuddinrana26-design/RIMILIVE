package com.example.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Shared lower room section; camera/audio seats and the header remain in their existing screens. */
@Composable
internal fun RoomLowerSection(
    state: EightSeatRoomState,
    actions: EightSeatRoomActions,
    tight: Boolean,
    cameraSeats: IntRange,
    pkScoreLine: String? = null
) {
    val inputFocus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    Surface(
        Modifier.fillMaxWidth().height(
            if (tight) if (pkScoreLine != null) 57.dp else 48.dp
            else if (pkScoreLine != null) 68.dp else 58.dp
        ),
        shape = RoundedCornerShape(15.dp), color = Color(0x995B392A)
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            verticalArrangement = Arrangement.Center) {
            Text("Welcome to RIMILIVE!", color = Color(0xFFBDE9D6),
                fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1)
            Text("Be kind, respect others and keep this room safe.",
                color = Color(0xFFFFE5CE), fontSize = 10.sp, maxLines = 1,
                overflow = TextOverflow.Ellipsis)
            if (pkScoreLine != null) {
                Text(pkScoreLine, color = Color(0xFFFFD88A),
                    fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
    Spacer(Modifier.height(if (tight) 5.dp else 7.dp))
    Row(Modifier.fillMaxWidth().height(34.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(Modifier.weight(1f).fillMaxHeight(),
            shape = RoundedCornerShape(16.dp), color = Color(0x995B392A)) {
            Box(Modifier.padding(horizontal = 10.dp), contentAlignment = Alignment.CenterStart) {
                Text(state.lastMessage, color = Color(0xFFFFF1D9),
                    fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Spacer(Modifier.width(4.dp))
        AuxiliaryRoomIcon(
            "🪙", "Coins", onClick = actions.onCoin
        )
        Spacer(Modifier.width(4.dp))
        AuxiliaryRoomIcon(
            "📷", if (state.cameraOn) "Turn camera off" else "Turn camera on",
            onClick = actions.onCameraToggle,
            enabled = state.mySeat?.let { it in cameraSeats } == true,
            active = state.cameraOn
        )
        Spacer(Modifier.width(4.dp))
        AuxiliaryRoomIcon(
            "🎙", if (state.micOn) "Mute microphone" else "Unmute microphone",
            onClick = actions.onMicToggle, active = state.micOn
        )
    }
    Spacer(Modifier.height(if (tight) 5.dp else 7.dp))
    Row(Modifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        BasicTextField(
            value = state.chatInput,
            onValueChange = actions.onChatChange,
            modifier = Modifier.weight(1f).fillMaxHeight()
                .focusRequester(inputFocus)
                .semantics { contentDescription = "SMS input" }
                .background(Color(0xAA5B392A), RoundedCornerShape(24.dp))
                .padding(horizontal = 11.dp, vertical = 11.dp),
            textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { actions.onSend() }),
            decorationBox = { inner ->
                Box {
                    if (state.chatInput.isEmpty()) {
                        Text("Enter something...", color = Color(0xCCFFFFFF),
                            fontSize = 12.sp, maxLines = 1)
                    }
                    inner()
                }
            }
        )
        BottomRoomIcon("PK", "PK controls", actions.onPk,
            enabled = state.isHost, color = Color(0xFF633C99))
        BottomRoomIcon("💬", "Message / Chat", {
            inputFocus.requestFocus()
            keyboard?.show()
        }, color = Color(0xFF10A98E))
        BottomRoomIcon("🎮", "Games", actions.onGame, color = Color(0xFF25895C))
        BottomRoomIcon("🎁", "Gifts", actions.onGift, color = Color(0xFFD74C91))
        BottomRoomIcon("•••", "More room options", actions.onMore,
            color = Color(0xFF723B21))
    }
}

@Composable
private fun BottomRoomIcon(
    glyph: String, label: String, onClick: () -> Unit,
    enabled: Boolean = true, color: Color
) {
    Surface(Modifier.size(36.dp).semantics { contentDescription = label }
        .clickable(enabled = enabled, onClick = onClick),
        shape = CircleShape, color = color) {
        Box(contentAlignment = Alignment.Center) {
            Text(glyph, color = if (enabled) Color.White else Color(0x99FFFFFF),
                fontSize = if (glyph == "PK") 13.sp else 18.sp,
                fontWeight = if (glyph == "PK") FontWeight.Bold else FontWeight.Normal)
        }
    }
}

@Composable
private fun AuxiliaryRoomIcon(
    glyph: String, label: String, onClick: () -> Unit,
    enabled: Boolean = true, active: Boolean = false
) {
    Surface(Modifier.size(32.dp).semantics { contentDescription = label }
        .clickable(enabled = enabled, onClick = onClick),
        shape = CircleShape,
        color = if (active) Color(0xFF0A947C) else Color(0x995B392A)) {
        Box(contentAlignment = Alignment.Center) {
            Text(glyph, color = if (enabled) Color.White else Color(0x99FFFFFF), fontSize = 15.sp)
        }
    }
}