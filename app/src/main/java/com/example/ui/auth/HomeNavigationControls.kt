package com.example.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Only individual tabs are clickable; the surrounding row cannot route a tap. */
@Composable
internal fun HomeSectionTabs(selectedTab: String, onSelect: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.SpaceEvenly) {
        listOf("Follow", "Party", "Chat", "Top").forEach { tab ->
            Column(
                Modifier.semantics {
                    contentDescription = "$tab tab"
                    selected = selectedTab == tab
                }.clickable { onSelect(tab) }.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(tab, fontSize = 17.sp,
                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedTab == tab) Color.White else Color(0xFFD4EEE7))
                if (selectedTab == tab) {
                    Box(Modifier.padding(top = 5.dp).width(32.dp).height(3.dp)
                        .background(Color.White, RoundedCornerShape(18.dp)))
                }
            }
        }
    }
}

@Composable
internal fun CreateRoomEntryButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 8.dp)
            .height(54.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF057C67))
    ) {
        Text("🎮  Create a Room", fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}