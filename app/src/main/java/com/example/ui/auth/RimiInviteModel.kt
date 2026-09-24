package com.example.ui.auth

import java.text.NumberFormat
import java.util.Locale

internal const val RIMI_INVITE_REWARD_PER_QUALIFIED = 11_000L

internal data class RimiInviteLeader(
    val id: String,
    val name: String,
    val photoUrl: String,
    val qualifiedInviteCount: Long
)

/**
 * The existing referral identifier is the Firebase account UID. It is stable
 * and already used by the app as an invite code; no new client-minted identity
 * or reward credential is introduced here.
 */
internal fun rimiInviteCode(uid: String?): String = uid.orEmpty()

internal fun rimiInviteLink(uid: String?): String =
    uid?.takeIf(String::isNotBlank)?.let { "https://rimilive.com/invite/$it" }.orEmpty()

internal fun rimiInviteShareMessage(link: String): String =
    "Join me on RIMILIVE: $link"

/** Null means the server has not supplied a usable verified count or it overflowed. */
internal fun rimiInviteDiamonds(qualifiedInviteCount: Long?): Long? {
    if (qualifiedInviteCount == null || qualifiedInviteCount < 0L) return null
    return try {
        Math.multiplyExact(qualifiedInviteCount, RIMI_INVITE_REWARD_PER_QUALIFIED)
    } catch (_: ArithmeticException) {
        null
    }
}

internal fun sortRimiInviteLeaders(entries: List<RimiInviteLeader>): List<RimiInviteLeader> =
    entries.asSequence()
        .filter { it.qualifiedInviteCount > 0L && it.name.isNotBlank() }
        .sortedWith(
            compareByDescending<RimiInviteLeader> { it.qualifiedInviteCount }
                .thenBy { it.name.lowercase(Locale.ROOT) }
                .thenBy { it.id }
        )
        .toList()

internal fun formatRimiInviteNumber(value: Long): String =
    NumberFormat.getIntegerInstance(Locale.US).format(value)