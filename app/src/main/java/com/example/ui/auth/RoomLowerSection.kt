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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.shadow
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
    LaunchedEffect(state.chatFocusRequest) {
        if (state.chatFocusRequest > 0) {
            inputFocus.requestFocus()
            keyboard?.show()
        }
    }

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
        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            Modifier.weight(1f).fillMaxHeight().shadow(3.dp, RoundedCornerShape(24.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF063F40), Color(0xFF147C72))),
                    RoundedCornerShape(24.dp))
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(30.dp).clickable {
                inputFocus.requestFocus()
                keyboard?.show()
            }.semantics { contentDescription = "Message / Chat" },
                contentAlignment = Alignment.Center) {
                Text("✉", color = Color(0xFF91FFE2), fontSize = 19.sp)
            }
            BasicTextField(
                value = state.chatInput,
                onValueChange = actions.onChatChange,
                modifier = Modifier.weight(1f).fillMaxHeight()
                    .focusRequester(inputFocus)
                    .semantics { contentDescription = "SMS input" }
                    .padding(start = 2.dp, end = 6.dp, top = 11.dp, bottom = 9.dp),
                textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { actions.onSend() }),
                decorationBox = { inner ->
                    Box {
                        if (state.chatInput.isEmpty()) {
                            Text("Enter something...", color = Color(0xDFFFFFFF),
                                fontSize = 11.sp, maxLines = 1,
                                overflow = TextOverflow.Ellipsis)
                        }
                        inner()
                    }
                }
            )
        }
        BottomRoomIcon("PK", "PK controls", actions.onPk,
            enabled = state.isHost, color = Color(0xFF8745C3), accent = Color(0xFFD9A6FF))
        BottomRoomIcon("✦", "Games", actions.onGame,
            color = Color(0xFF168DB2), accent = Color(0xFF9EF4FF))
        BottomRoomIcon("🎁", "Gifts", actions.onGift,
            color = Color(0xFFD03D8D), accent = Color(0xFFFFAFD5))
        BottomRoomIcon("•••", "More room options", actions.onMore,
            color = Color(0xFFEC9B28), accent = Color(0xFFFFDB86))
    }
}

@Composable
private fun BottomRoomIcon(
    glyph: String, label: String, onClick: () -> Unit,
    enabled: Boolean = true, color: Color, accent: Color
) {
    Surface(Modifier.size(40.dp).shadow(4.dp, RoundedCornerShape(13.dp))
        .semantics { contentDescription = label }
        .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(13.dp), color = color,
        border = androidx.compose.foundation.BorderStroke(1.dp, accent)) {
        Box(Modifier.background(Brush.verticalGradient(listOf(accent.copy(alpha = 0.35f),
            color, color))), contentAlignment = Alignment.Center) {
            Text(glyph, color = if (enabled) Color.White else Color(0x99FFFFFF),
                fontSize = if (glyph == "PK") 13.sp else 18.sp,
                fontWeight = FontWeight.Bold)
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