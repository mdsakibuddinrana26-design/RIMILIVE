package com.example.ui.auth

import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.test.onNodeWithText
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
class HomeNavigationControlsTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun tabsAndCreateRoomHaveDistinctSingleTapDestinations() {
        var selected by mutableStateOf("Party")
        val routes = mutableListOf<String>()
        var createRequests = 0
        rule.setContent {
            Column(Modifier.fillMaxSize()) {
                HomeSectionTabs(selected) { tab -> selected = tab; routes += tab }
                CreateRoomEntryButton { createRequests++ }
            }
        }
        listOf("Follow", "Party", "Chat", "Top").forEach { tab ->
            rule.onNodeWithContentDescription("$tab tab").performClick()
            assertEquals(tab, selected)
            rule.onNodeWithContentDescription("$tab tab")
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, true))
            assertEquals(0, createRequests)
        }
        assertEquals(listOf("Follow", "Party", "Chat", "Top"), routes)
        rule.onNodeWithText("🎮  Create a Room").performClick()
        assertEquals(1, createRequests)
        assertEquals(listOf("Follow", "Party", "Chat", "Top"), routes)
    }
}