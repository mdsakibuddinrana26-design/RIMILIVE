package com.example.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
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
class RoomGameMenuTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun eachVisibleGameTargetRoutesOnlyToItsOwnAction() {
        var open by mutableStateOf(true)
        var stray = 0
        val actions = mutableListOf<String>()
        rule.setContent {
            Box(Modifier.fillMaxSize().clickable { stray++ }) {
                if (open) RoomGameMenu(
                    onDismiss = { open = false },
                    onPkGame = { actions += "PK Game"; open = false },
                    onLudo = { actions += "Ludo"; open = false },
                    onGamiRace = { actions += "GAMI Race"; open = false }
                )
            }
        }
        rule.onNodeWithText("Games").performTouchInput { click(center) }
        assertEquals(emptyList<String>(), actions)
        assertEquals(0, stray)
        listOf("Ludo", "GAMI Race", "PK Game").forEach { label ->
            rule.onNodeWithText(label).assertIsDisplayed().performClick()
            assertEquals(label, actions.last())
            assertEquals(0, stray)
            rule.runOnIdle { open = true }
        }
        assertEquals(listOf("Ludo", "GAMI Race", "PK Game"), actions)
        rule.onNodeWithText("Close").performClick()
        assertEquals(3, actions.size)
        assertEquals(0, stray)
    }
}