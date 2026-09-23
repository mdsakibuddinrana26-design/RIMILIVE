package com.example.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val GoldBorder = Color(0xFFFFD98C)
private val IdleBorder = Color(0x665CCAA7)

/** An extension of the existing dark-green Settings bottom sheet. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoomBackgroundSheet(
    selectedId: String?,
    onSelect: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SheetGreen, contentColor = SheetText, dragHandle = null
    ) {
        val day = roomBackgrounds.filterNot { it.night }
        val night = roomBackgrounds.filter { it.night }
        LazyColumn(
            Modifier.fillMaxWidth().heightIn(max = 520.dp)
                .testTag("room-background-list"),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    Text("Background", color = SheetText, fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold)
                    Text("Your view of this Party Room", color = Color(0xFFBDD9CE),
                        fontSize = 12.sp)
                }
            }
            item {
                Surface(
                    Modifier.fillMaxWidth()
                        .semantics {
                            contentDescription = "Select Original Room background"
                            selected = selectedId == null
                        }
                        .clickable { onSelect(null) },
                    color = Color(0xFF244038), shape = RoundedCornerShape(15.dp),
                    border = BorderStroke(1.dp,
                        if (selectedId == null) GoldBorder else IdleBorder)
                ) {
                    Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.width(86.dp).height(56.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Brush.verticalGradient(listOf(
                                Color(0xFFFF9B40), Color(0xFFEF702E)))))
                        Spacer(Modifier.width(12.dp))
                        Text("Original Room", color = SheetText, fontSize = 13.sp)
                    }
                }
            }
            item { CategoryLabel("Day / Normal") }
            items(2) { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    day.slice(row * 2 until row * 2 + 2).forEach { option ->
                        BackgroundTile(option, selectedId == option.id,
                            Modifier.weight(1f)) { onSelect(option.id) }
                    }
                }
            }
            item { CategoryLabel("Night") }
            items(2) { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    night.slice(row * 2 until row * 2 + 2).forEach { option ->
                        BackgroundTile(option, selectedId == option.id,
                            Modifier.weight(1f)) { onSelect(option.id) }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryLabel(title: String) {
    Text(title, color = GoldBorder, fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp, modifier = Modifier.padding(top = 5.dp))
}

@Composable
private fun BackgroundTile(
    option: RoomBackgroundOption,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier.semantics {
            contentDescription = "Select ${option.title} background"
            this.selected = selected
        }.clickable(onClick = onClick),
        color = Color(0xFF244038), shape = RoundedCornerShape(15.dp),
        border = BorderStroke(1.dp, if (selected) GoldBorder else IdleBorder)
    ) {
        Column(Modifier.padding(7.dp)) {
            Image(
                painter = painterResource(option.drawableId),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(92.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(5.dp))
            Text(option.title, color = SheetText, fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}