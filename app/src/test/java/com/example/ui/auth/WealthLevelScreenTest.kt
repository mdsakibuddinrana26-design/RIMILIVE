package com.example.ui.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
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
}