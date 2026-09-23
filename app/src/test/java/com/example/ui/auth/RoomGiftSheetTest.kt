package com.example.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
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
@Config(qualifiers = "w320dp-h540dp-xxhdpi", sdk = [36])
class RoomGiftSheetTest {
    @get:Rule val rule = createComposeRule()

    private val selected = SemanticsMatcher.expectValue(SemanticsProperties.Selected, true)

    @Test
    fun everyCategoryAndQuantityCanBeSelectedWithoutFakeGiftSending() {
        var open by mutableStateOf(true)
        rule.setContent {
            if (open) RoomGiftSheet(
                receiverSeat = 3, coinBalance = 125,
                onDismiss = { open = false }
            )
        }
        assertEquals(listOf("Popular", "Lucky", "Luxury", "VIP", "Romantic", "Festival"),
            giftCategories)
        rule.onNodeWithText("To: Seat 3").assertIsDisplayed()
        rule.onNodeWithText("Coins: 125").assertIsDisplayed()
        listOf("Rose", "Heart", "Star", "Crown").forEach {
            rule.onNodeWithText(it).assertIsDisplayed()
        }
        rule.onNodeWithContentDescription("Select Rose gift").performClick()
        rule.onNodeWithContentDescription("Select Rose gift").assert(selected)
        giftCategories.forEach { category ->
            rule.onNodeWithContentDescription("$category category")
                .performScrollTo().assertIsDisplayed().performClick()
            rule.onNodeWithContentDescription("$category category").assert(selected)
            if (category != "Popular") {
                rule.onNodeWithText("No $category gifts available yet").assertIsDisplayed()
            }
        }
        listOf(1, 10, 99, 999).forEach { count ->
            rule.onNodeWithContentDescription("Quantity $count").assertIsDisplayed().performClick()
            rule.onNodeWithContentDescription("Quantity $count").assert(selected)
        }
        rule.onNodeWithText("Send").assertIsDisplayed().assertIsNotEnabled()
        val rootBottom = rule.onAllNodes(isRoot()).fetchSemanticsNodes()
            .maxOf { it.boundsInRoot.bottom }
        assertTrue("Send is covered by the navigation area",
            rule.onNodeWithText("Send").fetchSemanticsNode().boundsInRoot.bottom < rootBottom)
        rule.onNodeWithContentDescription("Close Gift panel").performClick()
        rule.onNodeWithText("Send").assertDoesNotExist()
    }

    @Test
    fun unknownBalanceAndUnverifiedPricesNeverAppearAsSpendableCoins() {
        rule.setContent {
            RoomGiftSheet(receiverSeat = 1, coinBalance = null, onDismiss = {})
        }
        rule.onNodeWithText("To: Host").assertIsDisplayed()
        rule.onNodeWithText("Coins: Unavailable").assertIsDisplayed()
        assertEquals(4, rule.onAllNodesWithText("Price pending").fetchSemanticsNodes().size)
        rule.onNodeWithText("Send").assertIsNotEnabled()
    }

    @Test
    fun roomGiftActionOpensAndClosesTheNewSheet() {
        var open by mutableStateOf(false)
        val room = EightSeatRoomState(
            hostName = "Host", hostPhoto = "", roomType = "Race",
            memberCount = 1, notice = "Welcome", isHost = true,
            mySeat = 1, myPhoto = "", occupied = emptySet(),
            names = emptyMap(), photos = emptyMap(),
            memberCameras = emptyMap(), memberMics = emptyMap(),
            cameraOn = false, micOn = true, pkRunning = false,
            pkSeconds = 0, lastMessage = "", chatInput = ""
        )
        rule.setContent {
            RoomControlBar(
                state = room,
                actions = EightSeatRoomActions(
                    onNotice = {}, onShare = {}, onLeave = {}, onPk = {},
                    onCameraSeat = {}, onAudioSeat = {}, onCameraToggle = {},
                    onMicToggle = {}, onChatChange = {}, onSend = {},
                    onGame = {}, onGift = { open = true }, onCoin = {}, onMore = {}
                )
            )
            if (open) RoomGiftSheet(1, null, onDismiss = { open = false })
        }
        rule.onNodeWithContentDescription("Gifts").performClick()
        rule.onNodeWithText("Send Gift").assertIsDisplayed()
        rule.onNodeWithContentDescription("Close Gift panel").performClick()
        rule.onNodeWithText("Send Gift").assertDoesNotExist()
        rule.onNodeWithContentDescription("Gifts").assertIsDisplayed()
    }
}