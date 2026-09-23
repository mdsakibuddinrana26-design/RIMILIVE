package com.example.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h640dp-xxhdpi", sdk = [36])
class RimiProfileScreenTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun profileShowsActualAccountInfoAndFiveOrderedNavigationItems() {
        var destination = ""
        rule.setContent {
            Column(Modifier.fillMaxSize()) {
                RimiProfileContent(
                    RimiProfileData(
                        name = "Current member", email = "member@example.test",
                        diamonds = 42, coins = 13
                    ), onNavigate = { destination = it }, onLogout = {},
                    modifier = Modifier.weight(1f)
                )
                RimiMainBottomBar(selectedTab = "Profile", onNavigate = { destination = it })
            }
        }
        rule.onNodeWithText("Current member").assertIsDisplayed()
        rule.onNodeWithText("member@example.test").assertIsDisplayed()
        rule.onNodeWithText("42").assertIsDisplayed()
        rule.onNodeWithText("13").assertIsDisplayed()
        val labels = listOf("Home", "Explore", "Post", "Messages", "Profile")
        val bounds = labels.map { rule.onAllNodesWithText(it).onLast().assertIsDisplayed()
            .fetchSemanticsNode().boundsInRoot }
        bounds.zipWithNext().forEach { (left, right) ->
            assertTrue("Profile navigation order is incorrect", left.right <= right.left)
        }
        rule.onNodeWithText("Messages").performClick()
        assertEquals("Message", destination)
        rule.onNodeWithText("Profile").performClick()
        assertEquals("Profile", destination)
        rule.onNodeWithText("Settings").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Sign-in PK").performScrollTo().assertIsDisplayed()
    }
}