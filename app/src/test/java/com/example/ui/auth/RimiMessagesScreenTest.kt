package com.example.ui.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h640dp-xxhdpi", sdk = [36])
class RimiMessagesScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private fun setScreen() {
        composeRule.setContent {
            RimiMessagesScreen()
        }
    }

    @Test
    fun categoriesRenderInPortraitWidth() {
        setScreen()
        composeRule.onNodeWithText("Messages").assertIsDisplayed()
        composeRule.onNodeWithTag("system-messages").assertIsDisplayed()
        composeRule.onNodeWithTag("agent-messages").assertIsDisplayed()
        composeRule.onNodeWithTag("stranger-messages").assertIsDisplayed()
    }

    @Test
    fun eachCategoryOpensItsOwnUnavailableStateAndBackReturns() {
        setScreen()
        listOf(
            "system-messages" to "System Message",
            "agent-messages" to "Agent Messages",
            "stranger-messages" to "Stranger Messages"
        ).forEach { (tag, title) ->
            composeRule.onNodeWithTag(tag).performClick()
            composeRule.onNodeWithText(title).assertIsDisplayed()
            composeRule.onNodeWithText("Messages unavailable").assertIsDisplayed()
            composeRule.onNodeWithContentDescription("Back to Messages").performClick()
            composeRule.onNodeWithText("Messages").assertIsDisplayed()
        }
    }
}