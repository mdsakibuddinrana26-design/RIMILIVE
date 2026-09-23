package com.example.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val giftCategories = listOf(
    "Popular", "Lucky", "Luxury", "VIP", "Romantic", "Festival"
)

private data class GiftPreview(val name: String, val icon: String)
// These are the names already shown by the old Gift entry. No prices are known yet.
private val popularGiftPreviews = listOf(
    GiftPreview("Rose", "✿"),
    GiftPreview("Heart", "♥"),
    GiftPreview("Star", "★"),
    GiftPreview("Crown", "♛")
)

private val GiftGold = Color(0xFFFFD98C)
private val GiftMuted = Color(0xFFB9D5CB)
private val GiftPanel = Color(0xFF244038)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoomGiftSheet(
    receiverSeat: Int,
    coinBalance: Long?,
    onDismiss: () -> Unit
) {
    var category by remember { mutableStateOf(giftCategories.first()) }
    var gift by remember { mutableStateOf<String?>(null) }
    var quantity by remember { mutableIntStateOf(1) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SheetGreen,
        contentColor = SheetText,
        dragHandle = null
    ) {
        Column(
            Modifier.fillMaxWidth().fillMaxHeight(0.78f).navigationBarsPadding()
        ) {
            Row(
                Modifier.fillMaxWidth().padding(start = 18.dp, end = 14.dp, top = 16.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Send Gift", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = SheetText)
                    Text(if (receiverSeat == 1) "To: Host" else "To: Seat $receiverSeat",
                        fontSize = 12.sp, color = GiftMuted)
                }
                Box(Modifier.size(44.dp).semantics {
                    contentDescription = "Close Gift panel"
                }.clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                    Text("×", fontSize = 26.sp, color = SheetText)
                }
            }
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                giftCategories.forEach { name ->
                    val active = category == name
                    Surface(
                        Modifier.semantics {
                            contentDescription = "$name category"
                            selected = active
                        }.clickable {
                            category = name
                            gift = null
                        },
                        shape = RoundedCornerShape(20.dp),
                        color = if (active) Color(0xFF178F72) else GiftPanel,
                        border = BorderStroke(1.dp, if (active) GiftGold else Color(0x665CCAA7))
                    ) {
                        Text(name, Modifier.padding(horizontal = 15.dp, vertical = 10.dp),
                            color = if (active) Color.White else GiftMuted,
                            fontSize = 13.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            if (category == "Popular") {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(popularGiftPreviews) { item ->
                        val active = gift == item.name
                        Surface(
                            Modifier.semantics {
                                contentDescription = "Select ${item.name} gift"
                                selected = active
                            }.clickable { gift = item.name },
                            shape = RoundedCornerShape(14.dp),
                            color = GiftPanel,
                            border = BorderStroke(1.dp,
                                if (active) GiftGold else Color(0x665CCAA7))
                        ) {
                            Column(Modifier.fillMaxWidth().padding(vertical = 13.dp, horizontal = 5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(item.icon, fontSize = 31.sp, color = GiftGold)
                                Spacer(Modifier.height(5.dp))
                                Text(item.name, fontSize = 12.sp, color = SheetText,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("Price pending", fontSize = 10.sp, color = GiftMuted,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            } else {
                Box(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center) {
                    Text("No $category gifts available yet", color = GiftMuted,
                        fontSize = 14.sp)
                }
            }
            Column(Modifier.fillMaxWidth().background(Color(0xFF1C352C))
                .padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 10.dp)) {
                Text("Coins: ${coinBalance?.takeIf { it >= 0 }?.toString() ?: "Unavailable"}",
                    color = GiftGold, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(9.dp))
                Row(Modifier.fillMaxWidth().height(44.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    listOf(1, 10, 99, 999).forEach { count ->
                        val active = quantity == count
                        Surface(
                            Modifier.weight(1f).fillMaxHeight().semantics {
                                contentDescription = "Quantity $count"
                                selected = active
                            }.clickable { quantity = count },
                            shape = RoundedCornerShape(11.dp),
                            color = if (active) Color(0xFF178F72) else GiftPanel,
                            border = BorderStroke(1.dp, if (active) GiftGold else Color(0x665CCAA7))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("$count", fontSize = 13.sp, color = SheetText)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(9.dp))
                Button(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = Color(0xFF376354),
                        disabledContentColor = Color(0xFFDFE8DE)
                    )
                ) {
                    Text("Send", fontWeight = FontWeight.Bold)
                }
                Text("Sending is unavailable until server-verified prices and coin payments are connected.",
                    modifier = Modifier.padding(top = 5.dp), color = GiftMuted, fontSize = 10.sp,
                    lineHeight = 13.sp)
            }
        }
    }
}