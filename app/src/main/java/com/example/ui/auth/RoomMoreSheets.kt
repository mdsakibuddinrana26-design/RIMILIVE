package com.example.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val SheetGreen = Color(0xFF152922)
internal val SheetText = Color(0xFFF5FFF9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoomMoreSheet(
    onDismiss: () -> Unit,
    onSettings: () -> Unit,
    onLudo: () -> Unit,
    onGamiRace: () -> Unit,
    onMusic: () -> Unit,
    onTopUp: () -> Unit,
    onMessages: () -> Unit,
    onBrowseRooms: () -> Unit,
    onJoinRoom: () -> Unit,
    onEditNotice: () -> Unit,
    onShareRoom: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SheetGreen,
        contentColor = SheetText, dragHandle = null) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp)) {
            SheetTitle("Game")
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                RoomMenuTile("Ludo", Color(0xFF186557), Modifier.weight(1f), onLudo)
                RoomMenuTile("GAMI Race", Color(0xFF186557),
                    Modifier.weight(1f), onGamiRace)
            }
            Spacer(Modifier.height(23.dp))
            SheetTitle("Tools")
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RoomMenuTile("Settings", Color(0xFF186557),
                    Modifier.weight(1f), onSettings)
                RoomMenuTile("Music", Color(0xFF186557),
                    Modifier.weight(1f), onMusic)
                RoomMenuTile("Top-up", Color(0xFF186557),
                    Modifier.weight(1f), onTopUp)
                RoomMenuTile("Messages", Color(0xFF186557),
                    Modifier.weight(1f), onMessages)
            }
            Spacer(Modifier.height(19.dp))
            HorizontalDivider(color = Color(0x445CCAA7))
            Spacer(Modifier.height(12.dp))
            Text("Room actions", color = Color(0xFFB5D8C8), fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                RoomShortcut("Browse rooms", onBrowseRooms)
                RoomShortcut("Join by ID", onJoinRoom)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                RoomShortcut("Edit notice", onEditNotice)
                RoomShortcut("Share room", onShareRoom)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RoomMenuTile(
    label: String, color: Color,
    modifier: Modifier, onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier.height(98.dp)
            .shadow(3.dp, shape).clip(shape)
            .background(Brush.verticalGradient(listOf(Color(0xFF24604F), Color(0xFF183B32))))
            .border(1.dp, Color(0x667CC9A8), shape)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(Modifier.size(44.dp), shape = RoundedCornerShape(13.dp),
            color = color, border = BorderStroke(1.dp, Color(0x999BE6C5))) {
            Box(contentAlignment = Alignment.Center) {
                RoomMenuGlyph(label)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(label, color = SheetText, fontSize = 11.sp, maxLines = 1,
            fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun RoomMenuGlyph(label: String) {
    Canvas(Modifier.size(23.dp)) {
        val u = size.minDimension / 24f
        val ink = Color(0xFFFFE9B2)
        val stroke = Stroke(1.8f*u, cap = StrokeCap.Round)
        fun line(x: Float, y: Float, x2: Float, y2: Float) {
            drawLine(ink, Offset(x*u, y*u), Offset(x2*u, y2*u),
                1.8f*u, cap = StrokeCap.Round)
        }
        when (label) {
            "Ludo" -> {
                drawRoundRect(ink, Offset(3*u, 3*u), Size(18*u, 18*u),
                    CornerRadius(4*u), style = stroke)
                listOf(Offset(8*u, 8*u), Offset(16*u, 8*u),
                    Offset(8*u, 16*u), Offset(16*u, 16*u)).forEach {
                    drawCircle(ink, 1.5f*u, it)
                }
            }
            "GAMI Race" -> {
                line(5f, 3f, 5f, 21f)
                val flag = Path().apply {
                    moveTo(5*u, 4*u)
                    lineTo(20*u, 4*u)
                    lineTo(17*u, 10*u)
                    lineTo(20*u, 16*u)
                    lineTo(5*u, 16*u)
                    close()
                }
                drawPath(flag, ink, style = stroke)
                line(12f, 5f, 12f, 15f)
            }
            "Settings" -> {
                drawCircle(ink, 6.5f*u, Offset(12*u, 12*u), style = stroke)
                drawCircle(ink, 2.1f*u, Offset(12*u, 12*u), style = stroke)
                for (n in 0..7) {
                    val angle = n * Math.PI / 4
                    line((12 + 8 * kotlin.math.cos(angle)).toFloat(),
                        (12 + 8 * kotlin.math.sin(angle)).toFloat(),
                        (12 + 10 * kotlin.math.cos(angle)).toFloat(),
                        (12 + 10 * kotlin.math.sin(angle)).toFloat())
                }
            }
            "Music" -> {
                line(10f, 6f, 20f, 4f)
                line(10f, 6f, 10f, 17f)
                line(20f, 4f, 20f, 15f)
                drawCircle(ink, 3f*u, Offset(7*u, 18*u), style = stroke)
                drawCircle(ink, 3f*u, Offset(17*u, 16*u), style = stroke)
            }
            "Top-up" -> {
                val diamond = Path().apply {
                    moveTo(6*u, 5*u); lineTo(18*u, 5*u)
                    lineTo(22*u, 10*u); lineTo(12*u, 21*u)
                    lineTo(2*u, 10*u); close()
                }
                drawPath(diamond, ink, style = stroke)
                line(2f, 10f, 22f, 10f)
            }
            "Messages" -> {
                drawRoundRect(ink, Offset(2*u, 3*u), Size(20*u, 16*u),
                    CornerRadius(4*u), style = stroke)
                line(6f, 19f, 4f, 22f)
                line(4f, 22f, 11f, 19f)
                for (x in listOf(8f, 12f, 16f))
                    drawCircle(ink, u, Offset(x*u, 11*u))
            }
        }
    }
}

@Composable
private fun RoomShortcut(label: String, onClick: () -> Unit) {
    Text(label, Modifier.clickable(onClick = onClick)
        .padding(horizontal = 8.dp, vertical = 9.dp),
        color = Color(0xFFB9EAD8), fontSize = 12.sp)
}

@Composable
private fun SheetTitle(label: String) {
    Text(label, Modifier.fillMaxWidth(), color = SheetText, fontSize = 17.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoomSettingsSheet(
    micOn: Boolean,
    cameraOn: Boolean,
    cameraAvailable: Boolean,
    onDismiss: () -> Unit,
    onMicrophone: () -> Unit,
    onCamera: () -> Unit,
    onBackground: () -> Unit,
    onUnavailable: (String) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SheetGreen,
        contentColor = SheetText, dragHandle = null) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp)) {
            SheetTitle("Settings")
            Spacer(Modifier.height(12.dp))
            SettingsAction("Sticker", "☺") { onUnavailable("Stickers") }
            SettingsAction("Beautification", "✧") { onUnavailable("Beautification") }
            SettingsUnavailable("Hide entrance effects", "≋")
            SettingsSwitch("Microphone", "♬", micOn, onClick = onMicrophone)
            SettingsSwitch("Camera", "▣", cameraOn, onClick = onCamera,
                available = cameraAvailable)
            SettingsCameraDirection()
            SettingsAction("Background", "▧", onClick = onBackground)
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun SettingsAction(label: String, icon: String, onClick: () -> Unit) {
    SettingsRow(label, icon, onClick = onClick) {
        Text("›", color = Color(0xFFC5EBDA), fontSize = 25.sp)
    }
}

@Composable
private fun SettingsSwitch(
    label: String, icon: String, checked: Boolean,
    onClick: () -> Unit, available: Boolean = true
) {
    SettingsRow(label, icon,
        subtitle = if (available) null else "Unavailable",
        onClick = if (available) onClick else null) {
        // The row owns the only click target; a nested switch must not fire twice.
        Switch(checked = available && checked, onCheckedChange = null,
            enabled = available, modifier = Modifier.height(36.dp),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF19A88D),
                uncheckedTrackColor = Color(0xFF416259),
                disabledUncheckedTrackColor = Color(0xFF344D45),
                disabledUncheckedThumbColor = Color(0xFFB0C4BA)))
    }
}

@Composable
private fun SettingsUnavailable(label: String, icon: String) {
    SettingsRow(label, icon, subtitle = "Unavailable") {
        Text("—", color = Color(0xFF94ADA2), fontSize = 17.sp)
    }
}

@Composable
private fun SettingsCameraDirection() {
    SettingsRow("Switch camera", "⟳", subtitle = "Unavailable") {
        Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF29473D),
            border = BorderStroke(1.dp, Color(0xFF466C5C))) {
            Text("Front ↔ Back", Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                color = Color(0xFFAEC6B9), fontSize = 11.sp, maxLines = 1)
        }
    }
}

@Composable
private fun SettingsRow(
    label: String,
    icon: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    val click = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Row(
        Modifier.fillMaxWidth().padding(bottom = 6.dp).height(58.dp)
            .clip(shape)
            .background(Brush.horizontalGradient(listOf(Color(0xFF244239), Color(0xFF1C362E))))
            .border(1.dp, Color(0x4C82BCA1), shape)
            .then(click)
            .padding(horizontal = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(34.dp).clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF2C5747)),
            contentAlignment = Alignment.Center) {
            Text(icon, color = Color(0xFFD1F3DF), fontSize = 21.sp)
        }
        Spacer(Modifier.size(11.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = SheetText, fontSize = 14.sp,
                fontWeight = FontWeight.Medium, maxLines = 1)
            if (subtitle != null) {
                Text(subtitle, color = Color(0xFFAAC8BA), fontSize = 10.sp)
            }
        }
        trailing()
    }
}