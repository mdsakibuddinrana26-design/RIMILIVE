package com.example.ui.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

private val InviteNight = Color(0xFF09091C)
private val InvitePanel = Color(0xFF17152E)
private val InvitePanelLight = Color(0xFF24203F)
private val InviteGold = Color(0xFFFFD98B)
private val InviteGoldDeep = Color(0xFFB97735)
private val InviteViolet = Color(0xFFB68AFF)
private val InvitePink = Color(0xFFE46BFF)
private val InviteIce = Color(0xFFBDEBFF)
private val InviteText = Color(0xFFF9F6FF)
private val InviteMuted = Color(0xFFBEB9D1)

@Composable
internal fun RimiInviteContent(
    inviteLink: String,
    qualifiedInviteCount: Long?,
    statsUnavailable: Boolean,
    leaders: List<RimiInviteLeader>,
    leaderboardLoading: Boolean,
    leaderboardUnavailable: Boolean,
    notice: String?,
    onBack: () -> Unit,
    onCopyLink: () -> Unit,
    onWhatsAppShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = InviteNight
    ) {
        Column(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(Color(0xFF15122D), InviteNight, Color(0xFF100D27))
                )
            )
        ) {
            Row(
                Modifier.fillMaxWidth().statusBarsPadding()
                    .padding(start = 5.dp, end = 16.dp, top = 4.dp, bottom = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("invite-back")) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = InviteText
                    )
                }
                Text(
                    "Invite Friends",
                    color = InviteText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize().navigationBarsPadding(),
                contentPadding = PaddingValues(start = 16.dp, top = 7.dp, end = 16.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                item {
                    InviteHero()
                }
                item {
                    InviteLinkCard(inviteLink, onWhatsAppShare, onCopyLink)
                }
                item {
                    notice?.let {
                        Text(
                            it,
                            Modifier.fillMaxWidth().testTag("invite-notice"),
                            color = InviteIce,
                            fontSize = 12.sp
                        )
                    }
                }
                item {
                    InviteStats(
                        qualifiedInviteCount = qualifiedInviteCount,
                        statsUnavailable = statsUnavailable
                    )
                }
                item {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 3.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "THE COMMUNITY",
                                color = InviteGold,
                                fontSize = 10.sp,
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Top Inviters",
                                color = InviteText,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Text(
                            "QUALIFIED",
                            color = InviteMuted,
                            fontSize = 9.sp,
                            letterSpacing = 1.3.sp,
                            modifier = Modifier.padding(bottom = 5.dp)
                        )
                    }
                }
                if (leaderboardLoading) {
                    item {
                        LeaderboardMessage("Loading verified leaderboard…")
                    }
                } else if (leaderboardUnavailable) {
                    item {
                        LeaderboardMessage("Verified leaderboard is unavailable right now.")
                    }
                } else if (leaders.isEmpty()) {
                    item {
                        LeaderboardMessage(
                            "The leaderboard will appear when verified invite results are available."
                        )
                    }
                } else {
                    itemsIndexed(leaders, key = { _, leader -> leader.id }) { index, leader ->
                        InviteLeaderRow(leader, index + 1)
                    }
                }
                item {
                    Text(
                        "Invite rewards and rankings require server-verified qualified referrals. Opening or sharing a link alone does not earn Diamonds.",
                        color = InviteMuted,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InviteHero() {
    val shape = RoundedCornerShape(27.dp)
    Box(
        Modifier.fillMaxWidth().shadow(16.dp, shape)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF392456), Color(0xFF201C43), Color(0xFF11142F))
                )
            )
            .border(1.dp, Brush.linearGradient(listOf(InviteGold, InviteViolet, InvitePink)), shape)
            .testTag("invite-hero")
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(InvitePink.copy(alpha = .14f), size.width * .49f,
                Offset(size.width * .96f, size.height * .04f))
            drawCircle(InviteGold.copy(alpha = .08f), size.width * .42f,
                Offset(size.width * .02f, size.height * 1.04f))
            drawLine(InviteGold.copy(alpha = .65f), Offset(0f, 2f),
                Offset(size.width * .72f, 2f), 2f)
            val points = listOf(
                Offset(size.width * .90f, size.height * .24f),
                Offset(size.width * .82f, size.height * .12f),
                Offset(size.width * .86f, size.height * .40f),
                Offset(size.width * .70f, size.height * .67f),
                Offset(size.width * .78f, size.height * .82f)
            )
            points.forEachIndexed { index, point ->
                val radius = if (index % 2 == 0) 2.2f else 1.5f
                drawCircle(InviteGold.copy(alpha = .75f), radius, point)
                drawLine(InviteGold.copy(alpha = .45f),
                    Offset(point.x - radius * 2f, point.y),
                    Offset(point.x + radius * 2f, point.y), 1f)
                drawLine(InviteGold.copy(alpha = .45f),
                    Offset(point.x, point.y - radius * 2f),
                    Offset(point.x, point.y + radius * 2f), 1f)
            }
        }
        Column(Modifier.fillMaxWidth().padding(horizontal = 21.dp, vertical = 21.dp)) {
            Text(
                "RIMILIVE  /  VIP COMMUNITY",
                color = InviteGold,
                fontSize = 10.sp,
                letterSpacing = 1.7.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(9.dp))
            Text(
                "Invite Friends",
                color = InviteText,
                fontSize = 31.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-.7).sp
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                InviteDiamond(29.dp)
                Spacer(Modifier.width(11.dp))
                Text(
                    buildAnnotatedString {
                        append("Earn ")
                        withStyle(SpanStyle(color = InviteGold, fontWeight = FontWeight.ExtraBold)) {
                            append("11,000 Diamonds")
                        }
                        append("\nfor every qualified invite")
                    },
                    color = InviteText,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(11.dp))
            Text(
                "Only verified, eligible new accounts count.",
                color = InviteMuted,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun InviteLinkCard(
    inviteLink: String,
    onWhatsAppShare: () -> Unit,
    onCopyLink: () -> Unit
) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
            .background(InvitePanel.copy(alpha = .97f))
            .border(1.dp, Color(0xFF5E4E82), RoundedCornerShape(22.dp))
            .padding(14.dp)
    ) {
        Text(
            "YOUR INVITE LINK",
            color = InviteMuted,
            fontSize = 10.sp,
            letterSpacing = 1.5.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0D0C21))
                .border(1.dp, Color(0xFF584678), RoundedCornerShape(14.dp))
                .padding(start = 13.dp, end = 3.dp, top = 3.dp, bottom = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                inviteLink.ifBlank { "Sign in to get your invite link" },
                Modifier.weight(1f).testTag("invite-link"),
                color = if (inviteLink.isBlank()) InviteMuted else InviteText,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(
                onClick = onCopyLink,
                enabled = inviteLink.isNotBlank(),
                modifier = Modifier.size(43.dp).testTag("copy-link-icon")
            ) {
                Icon(
                    Icons.Outlined.ContentCopy,
                    contentDescription = "Copy invite link",
                    tint = InviteIce
                )
            }
        }
        Spacer(Modifier.height(11.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Button(
                onClick = onWhatsAppShare,
                enabled = inviteLink.isNotBlank(),
                modifier = Modifier.weight(1.15f).height(50.dp).testTag("whatsapp-invite"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF138E67),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF3A4750),
                    disabledContentColor = InviteMuted
                ),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Text("◉", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(6.dp))
                Text("Invite via WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Button(
                onClick = onCopyLink,
                enabled = inviteLink.isNotBlank(),
                modifier = Modifier.weight(.85f).height(50.dp).testTag("copy-link"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7547C9),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF3A4750),
                    disabledContentColor = InviteMuted
                ),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(6.dp))
                Text("Copy Link", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun InviteStats(qualifiedInviteCount: Long?, statsUnavailable: Boolean) {
    val earned = rimiInviteDiamonds(qualifiedInviteCount)
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(19.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF28203F), Color(0xFF19162F))))
            .border(1.dp, Color(0xFF55436F), RoundedCornerShape(19.dp))
            .padding(horizontal = 13.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("QUALIFIED INVITES", color = InviteMuted, fontSize = 9.sp,
                letterSpacing = .9.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(
                qualifiedInviteCount?.let(::formatRimiInviteNumber) ?: "—",
                color = InviteText,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.testTag("qualified-invite-count")
            )
        }
        Box(Modifier.width(1.dp).height(42.dp).background(Color(0xFF55436F)))
        Column(Modifier.weight(1.18f).padding(start = 13.dp)) {
            Text("INVITE DIAMONDS", color = InviteMuted, fontSize = 9.sp,
                letterSpacing = .9.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                InviteDiamond(18.dp)
                Spacer(Modifier.width(6.dp))
                Text(
                    earned?.let(::formatRimiInviteNumber) ?: "—",
                    color = InviteGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    modifier = Modifier.testTag("invite-earned-diamonds")
                )
            }
        }
        if (statsUnavailable) {
            Text(
                "VERIFICATION PENDING",
                color = InviteMuted,
                fontSize = 8.sp,
                letterSpacing = .5.sp,
                modifier = Modifier.testTag("invite-stats-pending")
            )
        }
    }
}

@Composable
private fun InviteLeaderRow(leader: RimiInviteLeader, rank: Int) {
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD166)
        2 -> Color(0xFFD4DBE9)
        3 -> Color(0xFFDB9B73)
        else -> Color(0xFF9D92BA)
    }
    val shape = RoundedCornerShape(19.dp)
    Row(
        Modifier.fillMaxWidth().clip(shape)
            .background(
                Brush.horizontalGradient(
                    listOf(rankColor.copy(alpha = if (rank <= 3) .20f else .11f),
                        InvitePanel, Color(0xFF111126))
                )
            )
            .border(1.dp, rankColor.copy(alpha = if (rank <= 3) .7f else .28f), shape)
            .padding(horizontal = 11.dp, vertical = 11.dp)
            .testTag("invite-leader-$rank"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(34.dp).clip(CircleShape)
                .background(Brush.linearGradient(listOf(rankColor, InviteGoldDeep))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (rank == 1) "♛" else rank.toString(),
                color = Color(0xFF171226),
                fontWeight = FontWeight.Black,
                fontSize = if (rank == 1) 18.sp else 14.sp
            )
        }
        Spacer(Modifier.width(10.dp))
        Box(
            Modifier.size(43.dp).clip(CircleShape)
                .background(InvitePanelLight)
                .border(1.5.dp, rankColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (leader.photoUrl.isNotBlank()) {
                AsyncImage(
                    model = leader.photoUrl,
                    contentDescription = "${leader.name} profile photo",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    leader.name.take(1).uppercase(),
                    color = rankColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Column(Modifier.weight(1f).padding(start = 9.dp)) {
            Text(
                leader.name,
                color = InviteText,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "QUALIFIED INVITES",
                color = InviteMuted,
                fontSize = 8.sp,
                letterSpacing = .4.sp
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                formatRimiInviteNumber(leader.qualifiedInviteCount),
                color = InviteText,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                InviteDiamond(13.dp)
                Spacer(Modifier.width(3.dp))
                Text(
                    rimiInviteDiamonds(leader.qualifiedInviteCount)
                        ?.let(::formatRimiInviteNumber) ?: "—",
                    color = InviteGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun LeaderboardMessage(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("invite-leaderboard-empty"),
        color = InvitePanel.copy(alpha = .76f),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3D345B))
    ) {
        Text(
            message,
            Modifier.padding(horizontal = 18.dp, vertical = 23.dp),
            color = InviteMuted,
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun InviteDiamond(size: androidx.compose.ui.unit.Dp) {
    Canvas(
        Modifier.size(size).semantics { contentDescription = "Diamond" }
            .testTag("invite-diamond")
    ) {
        val path = Path().apply {
            moveTo(this@Canvas.size.width * .5f, 0f)
            lineTo(this@Canvas.size.width * .94f, this@Canvas.size.height * .36f)
            lineTo(this@Canvas.size.width * .5f, this@Canvas.size.height)
            lineTo(this@Canvas.size.width * .06f, this@Canvas.size.height * .36f)
            close()
        }
        drawPath(
            path,
            Brush.linearGradient(listOf(Color(0xFFFAFFFF), InviteIce, InviteViolet, InvitePink))
        )
        drawPath(path, InviteGold.copy(alpha = .8f), style = Stroke(width = 1.dp.toPx()))
        drawLine(Color.White.copy(alpha = .82f), Offset(size.width * .06f, size.height * .36f),
            Offset(size.width * .94f, size.height * .36f), 1.dp.toPx())
        drawLine(Color.White.copy(alpha = .56f), Offset(size.width * .5f, size.height * .03f),
            Offset(size.width * .38f, size.height * .36f), 1.dp.toPx())
        drawLine(Color.White.copy(alpha = .5f), Offset(size.width * .5f, size.height * .03f),
            Offset(size.width * .65f, size.height * .36f), 1.dp.toPx())
    }
}