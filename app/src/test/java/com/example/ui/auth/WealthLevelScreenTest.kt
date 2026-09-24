package com.example.ui.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h640dp-xxhdpi", sdk = [36])
class WealthLevelScreenTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun unknownAccountingStaysUnknownAndHighestMilestoneIsReachableInPortrait() {
        rule.setContent { WealthLevelScreen(onBack = {}) }
        rule.onNodeWithTag("wealth-hero").assertIsDisplayed()
        rule.onNodeWithText("Pending\nverification").assertIsDisplayed()
        rule.onNodeWithTag("wealth-level-30").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("300,000,000,000").assertIsDisplayed()
        rule.onNodeWithText("Top Up Unavailable").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun suppliedVerifiedTotalUsesExistingLevelAndRemainingCalculation() {
        rule.setContent { WealthLevelScreen(onBack = {}, eligibleTotal = 3_000_000L) }
        rule.onAllNodesWithText("LV2").onFirst().assertIsDisplayed()
        rule.onNodeWithText("7,000,000 more to LV3").assertIsDisplayed()
        rule.onNodeWithTag("wealth-level-10").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("30,000,000,000").assertIsDisplayed()
    }

    @Test
    fun giftTabHasDistinctElevenLevelsAndDiamondsWithoutClaimingUnverifiedProgress() {
        rule.setContent { WealthLevelScreen(onBack = {}) }
        rule.onNodeWithTag("gift-level-tab").performClick()
        rule.onNodeWithTag("gift-hero").assertIsDisplayed()
        rule.onNodeWithText("Pending\nverification").assertIsDisplayed()
        rule.onAllNodesWithContentDescription("Gift diamond").assertCountEquals(11)
        rule.onNodeWithTag("gift-level-10").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("30,000,000,000").assertIsDisplayed()
    }

    @Test
    fun tabSwitchingRetainsEachTabScrollAndVerifiedGiftProgress() {
        rule.setContent {
            WealthLevelScreen(onBack = {}, eligibleTotal = 3_000_000L,
                verifiedReceivedGiftTotal = 5_000_000L)
        }
        rule.onNodeWithTag("wealth-level-30").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("gift-level-tab").performClick()
        rule.onAllNodesWithText("5,000,000").onFirst().assertIsDisplayed()
        rule.onNodeWithText(" more to LV3").assertIsDisplayed()
        rule.onAllNodesWithContentDescription("Gift diamond").assertCountEquals(13)
        rule.onNodeWithText("CURRENT").performScrollTo().assertIsDisplayed()
        rule.onAllNodesWithText("COMPLETED").onFirst().assertExists()
        rule.onAllNodesWithText("LOCKED").onFirst().assertExists()
        rule.onNodeWithTag("gift-level-10").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("recharge-level-tab").performClick()
        rule.onNodeWithTag("wealth-level-30").assertIsDisplayed()
        rule.onNodeWithTag("gift-level-tab").performClick()
        rule.onNodeWithTag("gift-level-10").assertIsDisplayed()
    }
}