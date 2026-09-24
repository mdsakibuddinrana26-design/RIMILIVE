package com.example.ui.auth

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DateFormat

private val InviteForest = Color(0xFF003D31)
private val InviteGreen = Color(0xFF0B6B4F)
private val InviteMint = Color(0xFF9AE8C8)
private val InviteInk = Color(0xFF16362D)

internal data class RimiReferral(
    val id: String,
    val inviteeUid: String,
    val createdAtLabel: String
)

/**
 * Invite identity is the Firebase UID. It is stable and authoritative; no
 * username, device identifier, generated token, or unpublished URL is used.
 */
internal fun rimiInviteCode(uid: String?): String = uid.orEmpty()

internal fun canRegisterRimiReferral(inviteeUid: String?, inviterCode: String): Boolean =
    !inviteeUid.isNullOrBlank() && inviterCode.isNotBlank() && inviteeUid != inviterCode

@Composable
internal fun RimiInviteScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val uid = auth.currentUser?.uid
    var referrals by remember(uid) { mutableStateOf<List<RimiReferral>?>(null) }
    var existingInviter by remember(uid) { mutableStateOf<String?>(null) }
    var registrationLoaded by remember(uid) { mutableStateOf(false) }
    var registrationUnavailable by remember(uid) { mutableStateOf(false) }
    var inviterCode by remember { mutableStateOf("") }
    var notice by remember { mutableStateOf<String?>(null) }
    var isRegistering by remember { mutableStateOf(false) }

    DisposableEffect(uid, firestore) {
        val listener = if (!uid.isNullOrBlank()) {
            firestore.collection("referrals")
                .whereEqualTo("inviterUid", uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        referrals = null
                    } else {
                        referrals = snapshot?.documents.orEmpty().map { doc ->
                            RimiReferral(
                                id = doc.id,
                                inviteeUid = doc.getString("inviteeUid").orEmpty(),
                                createdAtLabel = doc.getTimestamp("createdAt")?.toDate()?.let {
                                    DateFormat.getDateInstance(DateFormat.MEDIUM).format(it)
                                }.orEmpty()
                            )
                        }
                    }
                }
        } else null
        onDispose { listener?.remove() }
    }
    DisposableEffect(uid, firestore) {
        val listener = if (!uid.isNullOrBlank()) {
            firestore.collection("referrals").document(uid)
                .addSnapshotListener { doc, error ->
                    registrationLoaded = true
                    registrationUnavailable = error != null
                    existingInviter = if (error == null) doc?.getString("inviterUid") else null
                }
        } else null
        onDispose { listener?.remove() }
    }

    val code = rimiInviteCode(uid)
    Surface(modifier = modifier.fillMaxSize(), color = Color(0xFFF4FBF7)) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().background(InviteForest).padding(top = 26.dp, bottom = 16.dp, start = 8.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("Invite a Friend", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = InviteForest),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(Modifier.padding(22.dp)) {
                            Text("Bring your circle to RIMILIVE", color = InviteMint, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            Text("Share your personal invite code with someone you trust.", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 30.sp)
                            Spacer(Modifier.height(18.dp))
                            Text("YOUR INVITE CODE", color = InviteMint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(if (code.isBlank()) "Sign in to get your code" else code, color = Color.White, fontSize = 14.sp)
                            Spacer(Modifier.height(15.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    enabled = code.isNotBlank(),
                                    onClick = {
                                        clipboard.setText(AnnotatedString(code))
                                        notice = "Invite code copied."
                                    }
                                ) {
                                    Icon(Icons.Outlined.ContentCopy, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Copy code")
                                }
                                Button(
                                    enabled = code.isNotBlank(),
                                    onClick = {
                                        val share = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, "Join me on RIMILIVE. Use my invite code: $code")
                                        }
                                        context.startActivity(Intent.createChooser(share, "Share RIMILIVE invite"))
                                    }
                                ) {
                                    Icon(Icons.Outlined.Share, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Share")
                                }
                            }
                        }
                    }
                }
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
                        Column(Modifier.padding(18.dp)) {
                            Text("Have an invite code?", color = InviteInk, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text("Register a friend's invite code once. Your own code cannot be used.",
                                color = Color(0xFF557267), fontSize = 13.sp,
                                modifier = Modifier.padding(top = 5.dp))
                            when {
                                existingInviter != null ->
                                    Text("Your inviter is already registered.",
                                        color = InviteGreen, modifier = Modifier.padding(top = 12.dp))
                                registrationUnavailable ->
                                    Text("Invite registration is unavailable right now.",
                                        color = InviteInk, modifier = Modifier.padding(top = 12.dp))
                                else -> {
                                    OutlinedTextField(
                                        value = inviterCode,
                                        onValueChange = { inviterCode = it.trim() },
                                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                        label = { Text("Invite code") },
                                        singleLine = true
                                    )
                                    Button(
                                        enabled = registrationLoaded && !isRegistering &&
                                            canRegisterRimiReferral(uid, inviterCode),
                                        modifier = Modifier.padding(top = 10.dp),
                                        onClick = {
                                            isRegistering = true
                                            val record = hashMapOf<String, Any>(
                                                "inviterUid" to inviterCode,
                                                "inviteeUid" to uid.orEmpty(),
                                                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                                            )
                                            // Document ID is the invitee UID: one immutable record per account.
                                            firestore.collection("referrals").document(uid.orEmpty()).set(record)
                                                .addOnSuccessListener {
                                                    notice = "Referral registered."
                                                    inviterCode = ""
                                                    isRegistering = false
                                                }
                                                .addOnFailureListener {
                                                    notice = "Referral could not be registered. Check the code and try again."
                                                    isRegistering = false
                                                }
                                        }
                                    ) {
                                        Icon(Icons.Outlined.PersonAdd, contentDescription = null)
                                        Spacer(Modifier.width(6.dp))
                                        Text("Register invite")
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    Text("My Referrals  ${referrals?.size ?: "—"}",
                        color = InviteInk, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Only accounts with an immutable referral record are shown.", color = Color(0xFF557267), fontSize = 13.sp)
                }
                if (referrals == null) {
                    item {
                        Text("Referral records are unavailable right now.",
                            color = InviteInk, modifier = Modifier.padding(vertical = 20.dp))
                    }
                } else if (referrals.isNullOrEmpty()) {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
                            Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("No referrals yet", color = InviteInk, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                Text("Share your code to invite someone to RIMILIVE.", color = Color(0xFF557267), modifier = Modifier.padding(top = 5.dp))
                            }
                        }
                    }
                } else {
                    items(referrals.orEmpty(), key = { it.id }) { referral ->
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
                            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                                 Text("Referred account", color = InviteInk, fontWeight = FontWeight.SemiBold)
                                 if (referral.createdAtLabel.isNotBlank()) {
                                     Text("Joined ${referral.createdAtLabel}",
                                         color = Color(0xFF557267), fontSize = 13.sp)
                                 }
                            }
                        }
                    }
                }
                item {
                    Text("Referral rules", color = InviteInk, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Text("An account can register one invite code. Code entry does not verify who shared it. No coins or diamonds are awarded; any future reward must be verified by a server.",
                        color = Color(0xFF557267), fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 24.dp))
                    if (notice != null) Text(notice!!, color = InviteGreen, fontSize = 13.sp)
                }
            }
        }
    }
}