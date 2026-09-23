package com.example.ui.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
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
        rule.setContent {
            EightSeatRoomScreen(
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
                    onCameraSeat = {}, onAudioSeat = {}, onCameraToggle = {},
                    onMicToggle = {}, onChatChange = {}, onSend = {},
                    onGame = {}, onGift = {}, onCoin = {}, onMore = {}
                )
            )
        }
        rule.onNodeWithContentDescription("Camera seat 1, Host").assertIsDisplayed()
        (2..3).forEach { rule.onNodeWithContentDescription("Empty camera seat $it, invite").assertIsDisplayed() }
        (4..8).forEach { rule.onNodeWithContentDescription("Empty audio seat $it, invite").assertIsDisplayed() }
        rule.onNodeWithContentDescription("Gifts").assertIsDisplayed()
        rule.onNodeWithContentDescription("Send message").assertIsDisplayed()
        rule.onNodeWithContentDescription("Leave room").assertIsDisplayed()
        rule.onRoot().captureRoboImage(filePath = "build/eight-seat-room.png")
    }
}