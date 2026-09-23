package com.example.ui.auth

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.core.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat

private val Forest = Color(0xFF004838)
private val DeepForest = Color(0xFF00372F)
private val Mint = Color(0xFF73E6BC)
private val Gold = Color(0xFFFBD06B)
private val White = Color(0xFFF5FFF9)

internal data class RimiProfileData(
    val name: String,
    val email: String,
    val photoUrl: String = "",
    val friends: Long? = null,
    val following: Long? = null,
    val fans: Long? = null,
    val visitors: Long? = null,
    val diamonds: Long? = null,
    val coins: Long? = null
)

@Composable
internal fun RimiProfileScreen(
    user: User,
    firestore: FirebaseFirestore,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val fallback = RimiProfileData(
        name = user.fullName.ifBlank { user.username }.ifBlank { "RIMILIVE member" },
        email = user.email
    )
    var profile by remember(uid, user) { mutableStateOf(fallback) }
    DisposableEffect(uid, firestore) {
        val listener = if (uid != null) firestore.collection("users").document(uid)
            .addSnapshotListener { doc, error ->
                if (error == null && doc != null && doc.exists()) {
                    // Only read values actually stored for this signed-in account.
                    profile = fallback.copy(
                        name = doc.getString("name").orEmpty().ifBlank { fallback.name },
                        photoUrl = doc.getString("photoUrl").orEmpty(),
                        friends = doc.getLong("friendsCount"),
                        following = doc.getLong("followingCount"),
                        fans = doc.getLong("fansCount"),
                        visitors = doc.getLong("visitorsCount"),
                        diamonds = doc.getLong("diamonds"),
                        coins = doc.getLong("coins")
                    )
                }
            } else null
        onDispose { listener?.remove() }
    }
    RimiProfileContent(profile, onNavigate, onLogout, modifier)
}

@Composable
internal fun RimiProfileContent(
    profile: RimiProfileData,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var openMenu by remember { mutableStateOf<String?>(null) }
    Box(
        modifier.fillMaxWidth().background(
            Brush.verticalGradient(
                listOf(Color(0xFF00755B), Forest, Color(0xFF00694D), DeepForest)
            )
        )
    ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(88.dp)
                        .border(3.dp, Gold, CircleShape)
                        .padding(5.dp)
                        .border(1.dp, Mint, CircleShape)
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (profile.photoUrl.isNotBlank()) {
                        AsyncImage(
                            profile.photoUrl, "Your profile photo",
                            Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Box(Modifier.fillMaxSize().background(Forest, CircleShape),
                            contentAlignment = Alignment.Center) {
                            Text(profile.name.take(1).uppercase(), color = Gold,
                                fontSize = 33.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.width(15.dp))
                Column(Modifier.weight(1f)) {
                    Text(profile.name, color = White, fontSize = 21.sp,
                        fontWeight = FontWeight.Bold, maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(5.dp))
                    Text(profile.email.ifBlank { "RIMILIVE member" }, color = Mint,
                        fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text("›", Modifier.clickable { openMenu = "My Profile" }.padding(5.dp),
                    color = White, fontSize = 31.sp)
            }
            Spacer(Modifier.height(17.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf(
                    Triple("Friends", profile.friends, "👥"),
                    Triple("Following", profile.following, "💞"),
                    Triple("Fans", profile.fans, "👑"),
                    Triple("Visitors", profile.visitors, "👁")
                ).forEach { (label, count, icon) ->
                    Column(Modifier.weight(1f).clickable {
                        if (label == "Following") onNavigate("Follow")
                        else openMenu = label
                    }, horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(icon, color = if (label == "Fans") Gold else Mint, fontSize = 25.sp)
                        Text(formatCount(count), color = White, fontSize = 17.sp,
                            fontWeight = FontWeight.Bold)
                        Text(label, color = Color(0xFFD5F4E8), fontSize = 11.sp)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("My Wallet", color = White, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                WalletCard("Diamonds", "◆", profile.diamonds, true,
                    Modifier.weight(1f)) { openMenu = "Diamonds" }
                WalletCard("Coins", "◉", profile.coins, false,
                    Modifier.weight(1f)) { openMenu = "Coins" }
            }
            Spacer(Modifier.height(12.dp))
            Surface(
                color = Color(0xB3B5F1D8), shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("◖", color = Forest, fontSize = 19.sp)
                    Spacer(Modifier.width(10.dp))
                    Text("No recent activity", color = DeepForest, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(13.dp))
            val menu = listOf(
                Triple("Invite", "👥", Mint),
                Triple("Sign-in PK", "👑", Gold),
                Triple("My Level", "✦", Color(0xFFE8B9FF)),
                Triple("My Mission", "🎯", Gold),
                Triple("My Profile", "👤", Mint),
                Triple("Store", "🛒", Color(0xFFFFB0CE)),
                Triple("FAQ", "?", Color(0xFF9CE4FF)),
                Triple("Settings", "⚙", Color(0xFFE0E9F0))
            )
            menu.forEach { (title, symbol, tint) ->
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                        .clickable {
                            when (title) {
                                "Invite" -> {
                                    val share = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, "Join me on RIMILIVE!")
                                    }
                                    context.startActivity(Intent.createChooser(share, "Invite to RIMILIVE"))
                                }
                                else -> openMenu = title
                            }
                        },
                    color = Color(0xBE003F34),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(0.5.dp, Color(0x4482E8C0))
                ) {
                    Row(
                        Modifier.height(47.dp).padding(horizontal = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(32.dp).background(Color(0xFF006A55), CircleShape),
                            contentAlignment = Alignment.Center) {
                            Text(symbol, color = tint, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(14.dp))
                        Text(title, Modifier.weight(1f), color = White, fontSize = 14.sp)
                        Text("›", color = White, fontSize = 25.sp)
                    }
                }
            }
        }
    }
    openMenu?.let { title ->
        AlertDialog(
            onDismissRequest = { openMenu = null },
            title = { Text(title) },
            text = {
                Text(when (title) {
                    "My Profile" -> "${profile.name}\n${profile.email}"
                    "Settings" -> "Account settings"
                    "Diamonds", "Coins" -> "Balance details are not available yet."
                    else -> "$title is not available yet."
                })
            },
            confirmButton = {
                if (title == "Settings") TextButton(onClick = {
                    openMenu = null
                    onLogout()
                }) { Text("Sign out") }
                else TextButton(onClick = { openMenu = null }) { Text("OK") }
            },
            dismissButton = if (title == "Settings") {
                { TextButton(onClick = { openMenu = null }) { Text("Cancel") } }
            } else null
        )
    }
}

private fun formatCount(value: Long?): String =
    value?.let { NumberFormat.getIntegerInstance().format(it) } ?: "—"

@Composable
private fun WalletCard(
    label: String, symbol: String, amount: Long?, diamond: Boolean,
    modifier: Modifier, onClick: () -> Unit
) {
    Surface(
        modifier = modifier.height(108.dp).clickable(onClick = onClick),
        color = if (diamond) Color(0xFF008D80) else Color(0xFFB77A28),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(2.dp, Gold)
    ) {
        Column(Modifier.background(
            Brush.verticalGradient(
                if (diamond) listOf(Color(0xFF23C5B7), Color(0xFF006B65))
                else listOf(Color(0xFFF1BD61), Color(0xFF9A581E))
            )
        ), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Text(symbol, color = if (diamond) Color(0xFFBBF3FF) else Gold,
                fontSize = 33.sp, fontWeight = FontWeight.Bold)
            Text(formatCount(amount), color = White, fontSize = 18.sp,
                fontWeight = FontWeight.Bold)
            Text(label + "  ›", Modifier.background(White, CircleShape)
                .padding(horizontal = 16.dp, vertical = 2.dp),
                color = DeepForest, fontSize = 12.sp)
        }
    }
}
