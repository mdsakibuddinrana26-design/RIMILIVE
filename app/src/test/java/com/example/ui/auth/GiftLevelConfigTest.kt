package com.example.ui.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GiftLevelConfigTest {
    @Test fun exactlyElevenProvidedGiftThresholds() {
        assertEquals(
            listOf(0L, 1_000_000L, 3_000_000L, 10_000_000L, 30_000_000L,
                100_000_000L, 300_000_000L, 1_000_000_000L, 3_000_000_000L,
                10_000_000_000L, 30_000_000_000L),
            GiftLevelConfig.levels.map { it.requiredReceivedValue }
        )
        assertEquals((0..10).toList(), GiftLevelConfig.levels.map { it.number })
    }

    @Test fun everyBoundaryUsesThatGiftLevel() {
        GiftLevelConfig.levels.forEach { expected ->
            assertEquals(expected.number,
                GiftLevelConfig.progressFor(expected.requiredReceivedValue).level?.number)
        }
    }

    @Test fun missingOrInvalidVerifiedReceiptTotalNeverBecomesLevelZero() {
        assertNull(GiftLevelConfig.progressFor(null).level)
        assertNull(GiftLevelConfig.progressFor(-1).level)
        assertEquals(0, GiftLevelConfig.progressFor(0).level?.number)
    }

    @Test fun progressIsBetweenAdjacentReceivedGiftThresholds() {
        val progress = GiftLevelConfig.progressFor(5_000_000L)
        assertEquals(2, progress.level?.number)
        assertEquals(3, progress.nextLevel?.number)
        assertEquals(5_000_000L, progress.amountToNext)
        assertEquals(2f / 7f, progress.fraction, .0001f)
    }

    @Test fun highestConfiguredGiftLevelIsFinal() {
        val progress = GiftLevelConfig.progressFor(30_000_000_000L)
        assertEquals(10, progress.level?.number)
        assertNull(progress.nextLevel)
        assertTrue(progress.fraction == 1f)
    }
}