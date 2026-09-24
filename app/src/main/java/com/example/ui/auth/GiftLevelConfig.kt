package com.example.ui.auth

/**
 * Gift status is separate from Recharge. A value may be passed here only after
 * a trusted service confirms and aggregates eligible gifts RECEIVED by this
 * account. Neither sent gifts nor the spendable Diamond wallet are inputs.
 */
internal data class GiftLevel(val number: Int, val requiredReceivedValue: Long)

internal data class GiftLevelProgress(
    val level: GiftLevel?,
    val verifiedReceivedTotal: Long?,
    val nextLevel: GiftLevel?,
    val amountToNext: Long?,
    val fraction: Float
)

internal object GiftLevelConfig {
    val levels: List<GiftLevel> = listOf(
        GiftLevel(0, 0L),
        GiftLevel(1, 1_000_000L),
        GiftLevel(2, 3_000_000L),
        GiftLevel(3, 10_000_000L),
        GiftLevel(4, 30_000_000L),
        GiftLevel(5, 100_000_000L),
        GiftLevel(6, 300_000_000L),
        GiftLevel(7, 1_000_000_000L),
        GiftLevel(8, 3_000_000_000L),
        GiftLevel(9, 10_000_000_000L),
        GiftLevel(10, 30_000_000_000L)
    )

    fun progressFor(verifiedReceivedTotal: Long?): GiftLevelProgress {
        if (verifiedReceivedTotal == null || verifiedReceivedTotal < 0L) {
            return GiftLevelProgress(null, null, null, null, 0f)
        }
        val current = levels.last { it.requiredReceivedValue <= verifiedReceivedTotal }
        val next = levels.firstOrNull { it.requiredReceivedValue > verifiedReceivedTotal }
        if (next == null) return GiftLevelProgress(current, verifiedReceivedTotal, null, null, 1f)
        val range = next.requiredReceivedValue - current.requiredReceivedValue
        return GiftLevelProgress(
            current, verifiedReceivedTotal, next,
            next.requiredReceivedValue - verifiedReceivedTotal,
            ((verifiedReceivedTotal - current.requiredReceivedValue).toDouble() / range.toDouble())
                .toFloat().coerceIn(0f, 1f)
        )
    }
}