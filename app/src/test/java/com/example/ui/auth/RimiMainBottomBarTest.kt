package com.example.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h640dp-xxhdpi", sdk = [36])
class RimiMainBottomBarTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun iconsRemainInPlaceAsSelectionChangesAndDestinationsStillWork() {
        var tab by mutableStateOf("Party")
        var destination = ""
        rule.setContent {
            Column(Modifier.fillMaxSize()) {
                Spacer(Modifier.weight(1f))
                RimiMainBottomBar(tab) { destination = it; tab = it }
            }
        }
        val labels = listOf("Home", "Explore", "Post", "Messages", "Profile")
        fun bounds() = labels.map {
            rule.onNodeWithContentDescription(it).fetchSemanticsNode().boundsInRoot
        }
        val initial = bounds()
        rule.onNodeWithContentDescription("Home")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, true))
        rule.runOnIdle { tab = "Follow" }
        assertEquals(initial, bounds())
        rule.runOnIdle { tab = "Top" }
        assertEquals(initial, bounds())
        listOf("Profile" to "Profile", "Messages" to "Message",
            "Post" to "Post", "Explore" to "Explore",
            "Home" to "Party").forEach { (label, target) ->
            rule.onNodeWithContentDescription(label).performClick()
            assertEquals(target, destination)
            assertEquals(initial, bounds())
            rule.onNodeWithContentDescription(label)
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, true))
        }
    }
}