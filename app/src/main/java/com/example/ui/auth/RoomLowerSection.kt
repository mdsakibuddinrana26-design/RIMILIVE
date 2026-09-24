package com.example.ui.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Request IME insets only while a room is showing; restore other screens' window policy. */
@Composable
internal fun RoomImeWindowPolicy() {
    val context = LocalContext.current
    val activity = remember(context) {
        var current: Context? = context
        while (current is ContextWrapper && current !is Activity) {
            current = current.baseContext
        }
        current as? Activity
    }
    DisposableEffect(activity) {
        val window = activity?.window
        val previous = window?.attributes?.softInputMode
        if (window != null && previous != null) {
            window.setSoftInputMode(
                (previous and WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST.inv()) or
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            )
        }
        onDispose { if (previous != null) window?.setSoftInputMode(previous) }
    }
}

/** Static room information stays in the room layout when the keyboard opens. */
@Composable
internal fun RoomLowerSectionInfo(
    state: EightSeatRoomState,
    actions: EightSeatRoomActions,
    tight: Boolean,
    cameraSeats: IntRange,
    pkScoreLine: String? = null
) {
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
}

/** Only this dock follows the IME; the fixed header and seats never remeasure for it. */
@Composable
internal fun RoomControlBar(
    state: EightSeatRoomState,
    actions: EightSeatRoomActions,
    modifier: Modifier = Modifier
) {
    val inputFocus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(state.chatFocusRequest) {
        if (state.chatFocusRequest > 0) {
            inputFocus.requestFocus()
            keyboard?.show()
        }
    }
    Row(modifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            Modifier.weight(1f).fillMaxHeight().shadow(2.dp, RoundedCornerShape(22.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF074D44), Color(0xFF106B5B))),
                    RoundedCornerShape(22.dp))
                .border(1.dp, Color(0xB3D6CA88), RoundedCornerShape(22.dp))
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(26.dp).clickable {
                inputFocus.requestFocus()
                keyboard?.show()
            }.semantics { contentDescription = "Message / Chat" },
                contentAlignment = Alignment.Center) {
                RoomControlGlyph("chat", Color(0xFFA4F7DE))
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
                            Text("SMS · Message...", color = Color(0xDFFFFFFF),
                                fontSize = 11.sp, maxLines = 1,
                                overflow = TextOverflow.Ellipsis)
                        }
                        inner()
                    }
                }
            )
        }
        BottomRoomIcon("PK", "PK controls", actions.onPk,
            enabled = state.isHost, color = Color(0xFF125548), accent = Color(0xFFFFDC82),
            countdown = if (state.pkRunning) "%02d:%02d".format(
                state.pkSeconds / 60, state.pkSeconds % 60) else null)
        BottomRoomIcon("game", "Games", actions.onGame,
            color = Color(0xFF125548), accent = Color(0xFFB9EBD7))
        BottomRoomIcon("gift", "Gifts", actions.onGift,
            color = Color(0xFF125548), accent = Color(0xFFFFD9A0))
        BottomRoomIcon("more", "More room options", actions.onMore,
            color = Color(0xFF125548), accent = Color(0xFFB9EBD7))
    }
}

@Composable
private fun BottomRoomIcon(
    glyph: String, label: String, onClick: () -> Unit,
    enabled: Boolean = true, color: Color, accent: Color, countdown: String? = null
) {
    Surface(Modifier.size(44.dp).shadow(1.dp, RoundedCornerShape(14.dp))
        .semantics { contentDescription = label }
        .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(14.dp), color = color,
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.9f))) {
        Box(Modifier.background(Brush.verticalGradient(listOf(accent.copy(alpha = 0.16f),
            color, color.copy(alpha = 0.94f)))), contentAlignment = Alignment.Center) {
            if (glyph == "PK") Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                Text("PK", color = if (enabled) Color.White else Color(0x99FFFFFF),
                    fontSize = if (countdown == null) 13.sp else 11.sp,
                    lineHeight = 12.sp, fontWeight = FontWeight.Bold)
                if (countdown != null) Text(countdown,
                    color = if (enabled) Color(0xFFFFDC82) else Color(0x99FFFFFF),
                    fontSize = 8.sp, lineHeight = 9.sp)
            }
            else RoomControlGlyph(glyph, if (enabled) Color.White else Color(0x99FFFFFF))
        }
    }
}

/** Small original line icons shared by the input and the four controls. */
@Composable
private fun RoomControlGlyph(kind: String, color: Color) {
    Canvas(Modifier.size(22.dp)) {
        val u = size.minDimension / 24f
        val stroke = Stroke(width = 2f * u, cap = androidx.compose.ui.graphics.StrokeCap.Round,
            join = androidx.compose.ui.graphics.StrokeJoin.Round)
        fun line(x1: Float, y1: Float, x2: Float, y2: Float) {
            drawLine(color, Offset(x1 * u, y1 * u), Offset(x2 * u, y2 * u), 2f * u,
                cap = androidx.compose.ui.graphics.StrokeCap.Round)
        }
        when (kind) {
            "chat" -> {
                drawRoundRect(color, Offset(2*u, 3*u), Size(20*u, 16*u),
                    CornerRadius(4*u), style = stroke)
                line(6f, 19f, 4f, 22f)
                line(4f, 22f, 11f, 19f)
                for (x in listOf(8f, 12f, 16f)) drawCircle(color, u, Offset(x*u, 11*u))
            }
            "game" -> {
                drawRoundRect(color, Offset(2*u, 7*u), Size(20*u, 12*u),
                    CornerRadius(5*u), style = stroke)
                line(7f, 13f, 12f, 13f)
                line(9.5f, 10.5f, 9.5f, 15.5f)
                drawCircle(color, 1.2f*u, Offset(16*u, 12*u))
                drawCircle(color, 1.2f*u, Offset(19*u, 15*u))
            }
            "gift" -> {
                drawRoundRect(color, Offset(5*u, 10*u), Size(14*u, 12*u),
                    CornerRadius(2*u), style = stroke)
                drawRoundRect(color, Offset(3*u, 8*u), Size(18*u, 4*u),
                    CornerRadius(1*u), style = stroke)
                line(12f, 9f, 12f, 21f)
                val bow = Path().apply {
                    moveTo(12*u, 8*u)
                    cubicTo(2*u, 9*u, 5*u, 1*u, 10*u, 4*u)
                    lineTo(12*u, 8*u)
                    cubicTo(22*u, 9*u, 19*u, 1*u, 14*u, 4*u)
                    close()
                }
                drawPath(bow, color, style = stroke)
            }
            "more" -> for (x in listOf(5f, 12f, 19f))
                drawCircle(color, 1.8f*u, Offset(x*u, 12*u))
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