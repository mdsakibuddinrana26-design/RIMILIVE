package com.example.ui.auth

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

private enum class InviteLoadState { Loading, Ready, Unavailable }

@Composable
internal fun RimiInviteScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val firestore = remember { FirebaseFirestore.getInstance() }
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val inviteLink = remember(uid) { rimiInviteLink(uid) }

    var qualifiedInviteCount by remember(uid) { mutableStateOf<Long?>(null) }
    var statsState by remember(uid) { mutableStateOf(InviteLoadState.Loading) }
    var leaders by remember(uid) { mutableStateOf(emptyList<RimiInviteLeader>()) }
    var leaderboardState by remember(uid) { mutableStateOf(InviteLoadState.Loading) }
    var notice by remember(uid) { mutableStateOf<String?>(null) }

    DisposableEffect(uid, firestore) {
        if (uid.isNullOrBlank()) {
            statsState = InviteLoadState.Unavailable
            leaderboardState = InviteLoadState.Unavailable
            onDispose { }
        } else {
            val userListener = firestore.collection("users").document(uid)
                .addSnapshotListener { document, error ->
                    val count = if (error == null && document?.exists() == true) {
                        document.getLong("qualifiedInviteCount")
                    } else null
                    qualifiedInviteCount = count?.takeIf { it >= 0L }
                    statsState = if (qualifiedInviteCount != null) {
                        InviteLoadState.Ready
                    } else {
                        InviteLoadState.Unavailable
                    }
                }
            val leaderboardListener = firestore.collection("inviteLeaderboard")
                .orderBy("qualifiedInviteCount", Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        leaders = emptyList()
                        leaderboardState = InviteLoadState.Unavailable
                    } else {
                        leaders = sortRimiInviteLeaders(
                            snapshot?.documents.orEmpty().mapNotNull { document ->
                                val count = document.getLong("qualifiedInviteCount")
                                val name = document.getString("name").orEmpty()
                                if (count == null || count < 0L || name.isBlank()) null
                                else RimiInviteLeader(
                                    id = document.id,
                                    name = name,
                                    photoUrl = document.getString("photoUrl").orEmpty(),
                                    qualifiedInviteCount = count
                                )
                            }
                        )
                        leaderboardState = InviteLoadState.Ready
                    }
                }
            onDispose {
                userListener.remove()
                leaderboardListener.remove()
            }
        }
    }

    RimiInviteContent(
        inviteLink = inviteLink,
        qualifiedInviteCount = qualifiedInviteCount,
        statsUnavailable = statsState != InviteLoadState.Ready,
        leaders = leaders,
        leaderboardLoading = leaderboardState == InviteLoadState.Loading,
        leaderboardUnavailable = leaderboardState == InviteLoadState.Unavailable,
        notice = notice,
        onBack = onBack,
        onCopyLink = {
            if (inviteLink.isNotBlank()) {
                clipboard.setText(AnnotatedString(inviteLink))
                notice = "Link copied"
            } else {
                notice = "Sign in to get your invite link."
            }
        },
        onWhatsAppShare = {
            if (inviteLink.isBlank()) {
                notice = "Sign in to get your invite link."
            } else {
                val message = rimiInviteShareMessage(inviteLink)
                val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                    setPackage("com.whatsapp")
                }
                try {
                    context.startActivity(whatsappIntent)
                    notice = null
                } catch (_: ActivityNotFoundException) {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, message)
                    }
                    try {
                        context.startActivity(
                            Intent.createChooser(shareIntent, "Share RIMILIVE invite")
                        )
                        notice = null
                    } catch (_: ActivityNotFoundException) {
                        notice = "No sharing app is available."
                    }
                }
            }
        },
        modifier = modifier
    )
}