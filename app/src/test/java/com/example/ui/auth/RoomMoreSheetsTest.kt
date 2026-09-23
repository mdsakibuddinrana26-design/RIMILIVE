package com.example.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.isDisplayed
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
class RoomMoreSheetsTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun moreOpensSettingsWithRealMicrophoneAndCameraControls() {
        var more by mutableStateOf(true)
        var settings by mutableStateOf(false)
        var mic by mutableStateOf(false)
        var camera by mutableStateOf(false)
        rule.setContent {
            if (more) RoomMoreSheet(
                onDismiss = { more = false },
                onSettings = { more = false; settings = true },
                onLudo = {}, onGamiRace = {}, onMusic = {}, onTopUp = {},
                onMessages = {}, onBrowseRooms = {}, onJoinRoom = {},
                onEditNotice = {}, onShareRoom = {}
            )
            if (settings) RoomSettingsSheet(
                micOn = mic, cameraOn = camera, cameraAvailable = true,
                onDismiss = { settings = false },
                onMicrophone = { mic = !mic },
                onCamera = { camera = !camera },
                onUnavailable = {}
            )
        }
        listOf("Ludo", "GAMI Race", "Settings", "Music", "Top-up", "Messages",
            "Browse rooms", "Join by ID", "Edit notice", "Share room").forEach {
            assertTrue("$it is not displayed", rule.onNodeWithText(it).performScrollTo().isDisplayed())
        }
        rule.onNodeWithText("Settings").performScrollTo().performClick()
        listOf("Sticker", "Beautification", "Hide entrance effects", "Microphone",
            "Camera", "Switch camera", "Background").forEach {
            assertTrue("$it is not displayed", rule.onNodeWithText(it).performScrollTo().isDisplayed())
        }
        rule.onNodeWithText("Microphone").performClick()
        rule.onNodeWithText("Camera").performClick()
        assertTrue(mic)
        assertTrue(camera)
    }

    @Test
    fun raceAndMessagesUseTheirExistingEntryCallbacks() {
        var selected = ""
        rule.setContent {
            RoomMoreSheet(
                onDismiss = {}, onSettings = {}, onLudo = {},
                onGamiRace = { selected = "GAMI Race" },
                onMusic = {}, onTopUp = {},
                onMessages = { selected = "Messages" },
                onBrowseRooms = {}, onJoinRoom = {}, onEditNotice = {},
                onShareRoom = {}
            )
        }
        rule.onNodeWithText("GAMI Race").performClick()
        assertEquals("GAMI Race", selected)
        rule.onNodeWithText("Messages").performClick()
        assertEquals("Messages", selected)
    }
}