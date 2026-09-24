package com.example.ui.auth

/**
 * The only source of truth for Wealth progression.  The total passed to these
 * functions must be a server-verified, lifetime eligible purchase total; a
 * wallet balance is deliberately not accepted here.
 */
internal data class WealthLevel(
    val number: Int,
    val requiredEligibleTopUp: Long
)

internal data class WealthProgress(
    val level: WealthLevel?,
    val currentTotal: Long?,
    val nextLevel: WealthLevel?,
    val amountToNext: Long?,
    val fraction: Float
)

internal object WealthLevelConfig {
    val levels: List<WealthLevel> = listOf(
        WealthLevel(0, 0L),
        WealthLevel(1, 1_000_000L),
        WealthLevel(2, 3_000_000L),
        WealthLevel(3, 10_000_000L),
        WealthLevel(4, 30_000_000L),
        WealthLevel(5, 100_000_000L),
        WealthLevel(6, 300_000_000L),
        WealthLevel(7, 1_000_000_000L),
        WealthLevel(8, 3_000_000_000L),
        WealthLevel(9, 10_000_000_000L),
        WealthLevel(10, 30_000_000_000L),
        WealthLevel(20, 100_000_000_000L),
        WealthLevel(30, 300_000_000_000L)
    )

    internal enum class Availability { COMING_SOON, AVAILABLE }

    internal data class Privilege(
        val title: String,
        val requiredLevel: Int,
        val availability: Availability = Availability.COMING_SOON
    )

    val privileges: List<Privilege> = listOf(
        Privilege("Information Background", 1),
        Privilege("View Visitors", 2),
        Privilege("Party Room Background", 3),
        Privilege("Entrance Effect", 5),
        Privilege("Privilege Gift", 10),
        Privilege("Party Room Privilege", 20),
        Privilege("Entry Bar", 20),
        Privilege("Portrait Frame", 30)
    )

    fun progressFor(eligibleTotal: Long?): WealthProgress {
        if (eligibleTotal == null || eligibleTotal < 0L) {
            return WealthProgress(null, null, null, null, 0f)
        }
        val current = levels.lastOrNull { it.requiredEligibleTopUp <= eligibleTotal }
            ?: levels.first()
        val next = levels.firstOrNull { it.requiredEligibleTopUp > eligibleTotal }
        if (next == null) return WealthProgress(current, eligibleTotal, null, null, 1f)
        val range = next.requiredEligibleTopUp - current.requiredEligibleTopUp
        val completed = eligibleTotal - current.requiredEligibleTopUp
        val fraction = (completed.toDouble() / range.toDouble()).toFloat().coerceIn(0f, 1f)
        return WealthProgress(current, eligibleTotal, next,
            next.requiredEligibleTopUp - eligibleTotal, fraction)
    }
}