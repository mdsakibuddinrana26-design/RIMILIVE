package com.example.ui.auth

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
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
class FifteenSeatRoomScreenTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun allFifteenSeatsAndActionsFitAndRemainClickable() {
        var selectedCamera = 0
        var selectedAudio = 0
        var giftsOpened = false
        rule.setContent {
            FifteenSeatRoomScreen(
                state = EightSeatRoomState(
                    hostName = "Host", hostPhoto = "", roomType = "Race",
                    memberCount = 1, notice = "Welcome", isHost = true,
                    mySeat = 1, myPhoto = "", occupied = emptySet(),
                    names = emptyMap(), photos = emptyMap(),
                    memberCameras = emptyMap(), memberMics = emptyMap(),
                    cameraOn = false, micOn = true, pkRunning = false,
                    pkSeconds = 0, lastMessage = "Welcome to the room", chatInput = ""
                ),
                actions = EightSeatRoomActions(
                    onNotice = {}, onShare = {}, onLeave = {}, onPk = {},
                    onCameraSeat = { selectedCamera = it },
                    onAudioSeat = { selectedAudio = it },
                    onCameraToggle = {}, onMicToggle = {},
                    onChatChange = {}, onSend = {}, onGame = {},
                    onGift = { giftsOpened = true }, onCoin = {}, onMore = {}
                ),
                pkScoreLine = null
            )
        }
        val root = rule.onRoot().fetchSemanticsNode().boundsInRoot
        val seats = (1..15).map { seat ->
            val label = when (seat) {
                1 -> "Camera seat 1, Host"
                in 2..5 -> "Empty camera seat $seat, invite"
                else -> "Empty audio seat $seat, invite"
            }
            rule.onNodeWithContentDescription(label).assertIsDisplayed()
                .fetchSemanticsNode().boundsInRoot
        }
        seats.forEach { seat ->
            assertTrue("Seat extends outside the portrait screen: $seat",
                seat.left >= root.left && seat.top >= root.top &&
                    seat.right <= root.right && seat.bottom <= root.bottom)
        }
        seats.forEachIndexed { index, seat ->
            seats.drop(index + 1).forEach { other ->
                assertTrue("Seats overlap: $seat and $other", !overlaps(seat, other))
            }
        }
        listOf("Send message", "Gifts", "PK controls", "Turn camera on",
            "Mute microphone", "Leave room").forEach {
            rule.onNodeWithContentDescription(it).assertIsDisplayed()
        }
        val send = rule.onNodeWithContentDescription("Send message").fetchSemanticsNode().boundsInRoot
        assertTrue("Bottom actions overlap seats", seats.maxOf { it.bottom } < send.top)
        rule.onNodeWithContentDescription("Empty camera seat 5, invite").performClick()
        rule.onNodeWithContentDescription("Empty audio seat 15, invite").performClick()
        rule.onNodeWithContentDescription("Gifts").performClick()
        assertEquals(5, selectedCamera)
        assertEquals(15, selectedAudio)
        assertTrue(giftsOpened)
    }

    private fun overlaps(a: Rect, b: Rect) =
        a.left < b.right && b.left < a.right && a.top < b.bottom && b.top < a.bottom
}