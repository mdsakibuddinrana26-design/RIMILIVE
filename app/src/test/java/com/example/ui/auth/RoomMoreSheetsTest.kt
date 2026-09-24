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
import androidx.compose.ui.test.isRoot
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
        var backgrounds by mutableStateOf(false)
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
                onBackground = { settings = false; backgrounds = true },
                onUnavailable = {}
            )
            if (backgrounds) RoomBackgroundSheet(
                selectedId = null,
                onSelect = { backgrounds = false },
                onDismiss = { backgrounds = false }
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
        rule.onNodeWithText("Background").performScrollTo().performClick()
        rule.onNodeWithText("Sunrise Sea").assertIsDisplayed()
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

    @Test
    fun everyMoreTileHasItsOwnTouchTarget() {
        val taps = mutableListOf<String>()
        rule.setContent {
            RoomMoreSheet(
                onDismiss = {}, onSettings = { taps += "Settings" },
                onLudo = { taps += "Ludo" }, onGamiRace = { taps += "GAMI Race" },
                onMusic = { taps += "Music" }, onTopUp = { taps += "Top-up" },
                onMessages = { taps += "Messages" },
                onBrowseRooms = { taps += "Browse rooms" },
                onJoinRoom = { taps += "Join by ID" },
                onEditNotice = { taps += "Edit notice" },
                onShareRoom = { taps += "Share room" }
            )
        }
        val labels = listOf("Ludo", "GAMI Race", "Settings", "Music", "Top-up",
            "Messages", "Browse rooms", "Join by ID", "Edit notice", "Share room")
        labels.forEachIndexed { index, label ->
            rule.onNodeWithText(label).performScrollTo().performClick()
            assertEquals("A neighboring option intercepted $label", labels.take(index + 1), taps)
        }
    }

    @Test
    fun settingsActionsAreDistinctAndUnavailableCameraCannotClaimSuccess() {
        val taps = mutableListOf<String>()
        var mic by mutableStateOf(false)
        rule.setContent {
            RoomSettingsSheet(
                micOn = mic, cameraOn = false, cameraAvailable = false,
                onDismiss = {},
                onMicrophone = { mic = !mic; taps += "Microphone" },
                onCamera = { taps += "Camera" },
                onBackground = { taps += "Background" },
                onUnavailable = { taps += it }
            )
        }
        val screenRight = rule.onAllNodes(isRoot()).fetchSemanticsNodes()
            .maxOf { it.boundsInRoot.right }
        listOf("Sticker", "Beautification").forEach {
            rule.onNodeWithText(it).performScrollTo().performClick()
        }
        rule.onNodeWithText("Microphone").performScrollTo().performClick()
        assertTrue(mic)
        rule.onNodeWithText("Camera").performScrollTo().performClick()
        rule.onNodeWithText("Switch camera").performScrollTo().performClick()
        val direction = rule.onNodeWithText("Front ↔ Back").assertIsDisplayed()
            .fetchSemanticsNode().boundsInRoot
        assertTrue(direction.left >= 0 && direction.right <= screenRight)
        rule.onNodeWithText("Background").performScrollTo().performClick()
        assertEquals(listOf("Stickers", "Beautification", "Microphone", "Background"), taps)
    }
}