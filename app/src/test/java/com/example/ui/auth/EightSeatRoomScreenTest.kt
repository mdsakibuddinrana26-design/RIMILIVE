package com.example.ui.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import com.github.takahirom.roborazzi.captureRoboImage
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
class EightSeatRoomScreenTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun allSeatsAndBottomActionsFitOnCompactPortraitScreen() {
        var typed = ""
        var sent = false
        rule.setContent {
            EightSeatRoomScreen(
                state = EightSeatRoomState(
                    hostName = "Host", hostPhoto = "", roomType = "Race",
                    memberCount = 1, notice = "Welcome", isHost = true,
                    mySeat = 1, myPhoto = "", occupied = emptySet(),
                    names = emptyMap(), photos = emptyMap(),
                    memberCameras = emptyMap(), memberMics = emptyMap(),
                    cameraOn = false, micOn = true, pkRunning = false,
                    pkSeconds = 0, lastMessage = "Guest joined the room", chatInput = ""
                ),
                actions = EightSeatRoomActions(
                    onNotice = {}, onShare = {}, onLeave = {}, onPk = {},
                    onCameraSeat = {}, onAudioSeat = {}, onCameraToggle = {},
                    onMicToggle = {}, onChatChange = { typed = it }, onSend = { sent = true },
                    onGame = {}, onGift = {}, onCoin = {}, onMore = {}
                )
            )
        }
        rule.onNodeWithContentDescription("Camera seat 1, Host").assertIsDisplayed()
        (2..3).forEach { rule.onNodeWithContentDescription("Empty camera seat $it, invite").assertIsDisplayed() }
        (4..8).forEach { rule.onNodeWithContentDescription("Empty audio seat $it, invite").assertIsDisplayed() }
        rule.onNodeWithText("Welcome to RIMILIVE!").assertIsDisplayed()
        rule.onNodeWithText("Guest joined the room").assertIsDisplayed()
        rule.onNodeWithText("Enter something...").assertIsDisplayed()
        rule.onNodeWithContentDescription("Gifts").assertIsDisplayed()
        val labels = listOf("SMS input", "PK controls", "Message / Chat",
            "Games", "Gifts", "More room options")
        val buttons = labels.map {
            rule.onNodeWithContentDescription(it).assertIsDisplayed()
                .fetchSemanticsNode().boundsInRoot
        }
        buttons.zipWithNext().forEach { (left, right) ->
            assertTrue("Bottom row is out of order", left.right <= right.left)
            assertTrue("Bottom controls are not on one row", left.top <= right.bottom && right.top <= left.bottom)
        }
        rule.onNodeWithContentDescription("SMS input").performTextInput("Hello")
        rule.onNodeWithContentDescription("SMS input").performImeAction()
        assertEquals("Hello", typed)
        assertTrue(sent)
        rule.onNodeWithContentDescription("Leave room").assertIsDisplayed()
        rule.onRoot().captureRoboImage(filePath = "build/eight-seat-room.png")
    }
}