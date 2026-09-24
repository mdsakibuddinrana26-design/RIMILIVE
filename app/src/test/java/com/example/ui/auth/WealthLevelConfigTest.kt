package com.example.ui.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WealthLevelConfigTest {
    @Test
    fun everyConfiguredBoundarySelectsThatLevel() {
        WealthLevelConfig.levels.forEach { expected ->
            assertEquals(expected.number,
                WealthLevelConfig.progressFor(expected.requiredEligibleTopUp).level?.number)
        }
    }

    @Test
    fun valuesBelowFirstThresholdStayAtZero() {
        assertEquals(0, WealthLevelConfig.progressFor(0).level?.number)
        assertEquals(0, WealthLevelConfig.progressFor(999_999).level?.number)
        assertEquals(1, WealthLevelConfig.progressFor(2_999_999).level?.number)
        assertEquals(2, WealthLevelConfig.progressFor(9_999_999).level?.number)
    }

    @Test
    fun unknownAccountingDoesNotBecomeLevelZero() {
        val result = WealthLevelConfig.progressFor(null)
        assertNull(result.level)
        assertEquals(0f, result.fraction)
    }

    @Test
    fun progressIsBetweenAdjacentThresholds() {
        val result = WealthLevelConfig.progressFor(5_000_000)
        assertEquals(2, result.level?.number)
        assertEquals(3, result.nextLevel?.number)
        assertEquals(5_000_000L, result.amountToNext)
        assertEquals(2f / 7f, result.fraction, 0.0001f)
    }

    @Test
    fun spendingWalletCoinsCannotChangeAuthoritativeTotal() {
        val beforeSpending = WealthLevelConfig.progressFor(3_000_000)
        val afterSpending = WealthLevelConfig.progressFor(3_000_000)
        assertEquals(beforeSpending.level, afterSpending.level)
        assertTrue(beforeSpending.currentTotal == afterSpending.currentTotal)
    }
}