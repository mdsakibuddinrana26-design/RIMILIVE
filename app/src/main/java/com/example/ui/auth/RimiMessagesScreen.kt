package com.example.ui.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val RimiDeepGreen = Color(0xFF062D27)
private val RimiGreen = Color(0xFF0D5C4D)
private val RimiMint = Color(0xFFB8E8D8)
private val RimiText = Color(0xFFEAF7F2)
private val RimiMuted = Color(0xFFA7C5BC)

private data class MessageCategory(
    val title: String,
    val description: String,
    val tag: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val messageCategories = listOf(
    MessageCategory(
        "System Message",
        "Updates and notices from RIMILIVE",
        "system-messages",
        Icons.Default.MailOutline
    ),
    MessageCategory(
        "Agent Messages",
        "Messages from your RIMILIVE agency",
        "agent-messages",
        Icons.Default.SupportAgent
    ),
    MessageCategory(
        "Stranger Messages",
        "Messages from people you do not follow",
        "stranger-messages",
        Icons.Default.Forum
    )
)

/**
 * The messages surface intentionally reads as unavailable until a message
 * collection is connected. It does not manufacture conversations or users.
 */
@Composable
internal fun RimiMessagesScreen(modifier: Modifier = Modifier) {
    var openedCategory by remember { mutableStateOf<MessageCategory?>(null) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = RimiDeepGreen
    ) {
        if (openedCategory == null) {
            MessagesIndex(
                onCategoryClick = { openedCategory = it }
            )
        } else {
            val category = openedCategory!!
            BackHandler { openedCategory = null }
            CategoryUnavailable(
                category = category,
                onBack = { openedCategory = null }
            )
        }
    }
}

@Composable
private fun MessagesIndex(onCategoryClick: (MessageCategory) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 18.dp, top = 28.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Messages",
                    color = RimiText,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Stay connected with RIMILIVE",
                    color = RimiMuted,
                    fontSize = 13.sp
                )
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("message-category-list"),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messageCategories, key = { it.tag }) { category ->
                MessageCategoryRow(category = category, onClick = { onCategoryClick(category) })
            }
            item {
                Text(
                    text = "Your conversations",
                    color = RimiMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 8.dp, top = 18.dp, bottom = 2.dp)
                )
                Text(
                    text = "No conversation data is available yet.",
                    color = RimiMuted,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun MessageCategoryRow(category: MessageCategory, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(category.tag)
            .clickable(onClick = onClick)
            .background(RimiGreen, RoundedCornerShape(18.dp))
            .padding(horizontal = 18.dp, vertical = 17.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(RimiMint.copy(alpha = 0.16f), RoundedCornerShape(15.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(category.icon, contentDescription = null, tint = RimiMint, modifier = Modifier.size(24.dp))
        }
        Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
            Text(category.title, color = RimiText, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(3.dp))
            Text(category.description, color = RimiMuted, fontSize = 12.sp)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = "Open ${category.title}", tint = RimiMint)
    }
}

@Composable
private fun CategoryUnavailable(category: MessageCategory, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, end = 20.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("messages-back")) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Messages",
                    tint = RimiText
                )
            }
            Text(category.title, color = RimiText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(category.icon, contentDescription = null, tint = RimiMint, modifier = Modifier.size(42.dp))
            Spacer(Modifier.height(18.dp))
            Text(
                text = "Messages unavailable",
                color = RimiText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "There are no ${category.title.lowercase()} to show yet. This area will display real messages when the messaging service is available.",
                color = RimiMuted,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}