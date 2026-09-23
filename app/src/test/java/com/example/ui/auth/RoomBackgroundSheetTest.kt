package com.example.ui.auth

import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h640dp-xxhdpi", sdk = [36])
class RoomBackgroundSheetTest {
    @get:Rule val rule = createComposeRule()

    private val state = EightSeatRoomState(
        hostName = "Host", hostPhoto = "", roomType = "Race",
        memberCount = 1, notice = "Welcome", isHost = true,
        mySeat = 1, myPhoto = "", occupied = emptySet(),
        names = emptyMap(), photos = emptyMap(),
        memberCameras = emptyMap(), memberMics = emptyMap(),
        cameraOn = false, micOn = true, pkRunning = false,
        pkSeconds = 0, lastMessage = "Member joined", chatInput = ""
    )
    private val actions = EightSeatRoomActions(
        onNotice = {}, onShare = {}, onLeave = {}, onPk = {},
        onCameraSeat = {}, onAudioSeat = {}, onCameraToggle = {},
        onMicToggle = {}, onChatChange = {}, onSend = {},
        onGame = {}, onGift = {}, onCoin = {}, onMore = {}
    )

    @Test
    fun everyThumbnailAppliesItsOwnImageAndRoomControlsStayVisible() {
        val context = RuntimeEnvironment.getApplication()
        val preference = RoomBackgroundPreferences(context)
        val userId = "background-compose-test"
        assertTrue(preference.save(userId, null))
        var selectedId by mutableStateOf<String?>(null)
        var showingSheet by mutableStateOf(true)
        var inRoom by mutableStateOf(true)
        var fifteenSeats by mutableStateOf(false)

        rule.setContent {
            Box(Modifier.fillMaxSize()) {
                if (inRoom) {
                    val image = roomBackgrounds.firstOrNull { it.id == selectedId }
                    if (fifteenSeats) FifteenSeatRoomScreen(
                        state, actions, pkScoreLine = null, background = image
                    )
                    else EightSeatRoomScreen(state, actions, background = image)
                    if (showingSheet) RoomBackgroundSheet(
                        selectedId = selectedId,
                        onSelect = { id ->
                            selectedId = id
                            assertTrue(preference.save(userId, id))
                            showingSheet = false
                        },
                        onDismiss = { showingSheet = false }
                    )
                }
            }
        }

        assertEquals(8, roomBackgrounds.size)
        assertEquals(4, roomBackgrounds.count { !it.night })
        assertEquals(4, roomBackgrounds.count { it.night })
        roomBackgrounds.forEachIndexed { index, option ->
            val imageBounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeResource(context.resources, option.drawableId, imageBounds)
            assertEquals("${option.title} width", 1080, imageBounds.outWidth)
            assertEquals("${option.title} height", 1440, imageBounds.outHeight)
            if (index > 0) rule.runOnIdle { showingSheet = true }
            val rowIndex = if (index < 4) 3 + index / 2 else 6 + (index - 4) / 2
            rule.onNodeWithTag("room-background-list").performScrollToIndex(rowIndex)
            rule.onNodeWithContentDescription("Select ${option.title} background")
                .assertIsDisplayed().performClick()
            assertEquals(option.id, selectedId)
            assertEquals(option, preference.load(userId))
            rule.onNodeWithContentDescription("Room background: ${option.title}")
                .assertIsDisplayed()
            rule.onNodeWithContentDescription("Camera seat 1, Host").assertIsDisplayed()
            rule.onNodeWithContentDescription("SMS input").assertIsDisplayed()
            rule.onNodeWithContentDescription("PK controls").assertIsDisplayed()
            rule.onNodeWithContentDescription("More room options").assertIsDisplayed()
        }

        rule.runOnIdle { inRoom = false }
        rule.runOnIdle {
            selectedId = RoomBackgroundPreferences(context).load(userId)?.id
            fifteenSeats = true
            inRoom = true
        }
        rule.onNodeWithContentDescription("Room background: Luxury Night Room")
            .assertIsDisplayed()
        (1..15).forEach { seat ->
            val label = when (seat) {
                1 -> "Camera seat 1, Host"
                in 2..5 -> "Empty camera seat $seat, invite"
                else -> "Empty audio seat $seat, invite"
            }
            rule.onNodeWithContentDescription(label).assertIsDisplayed()
        }
        rule.runOnIdle { showingSheet = true }
        rule.onNodeWithTag("room-background-list").performScrollToIndex(1)
        rule.onNodeWithContentDescription("Select Original Room background")
            .assertIsDisplayed().performClick()
        rule.onNodeWithContentDescription("Room background: Luxury Night Room")
            .assertDoesNotExist()
        assertEquals(null, RoomBackgroundPreferences(context).load(userId))
    }

    @Test
    fun preferenceIsLocalAndScopedToTheSignedInUser() {
        val context = RuntimeEnvironment.getApplication()
        val preferences = RoomBackgroundPreferences(context)
        val first = "background-first-test"
        val second = "background-second-test"
        assertTrue(preferences.save(first, null))
        assertTrue(preferences.save(second, null))
        assertTrue(preferences.save(first, "moonlight_sea"))
        assertEquals("moonlight_sea", RoomBackgroundPreferences(context).load(first)?.id)
        assertEquals(null, RoomBackgroundPreferences(context).load(second))
        assertFalse(preferences.save("", "garden_view"))
        assertEquals(null, preferences.load(""))
        assertTrue(preferences.save(first, null))
    }
}