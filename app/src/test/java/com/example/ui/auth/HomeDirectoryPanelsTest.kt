package com.example.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
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
class HomeDirectoryPanelsTest {
    @get:Rule val rule = createComposeRule()

    private val rooms = listOf(
        RoomCardInfo("100201", "host-a", "Amina", "", "", "Party", 2, 100L),
        RoomCardInfo("100202", "host-b", "Samir", "", "", "Race", 7, 200L),
        RoomCardInfo("100203", "host-c", "Raya", "", "", "Party", 1, 300L)
    )

    @Test
    fun chatCardsUsePartySizeTwoColumnGridAndOnlyCallButtonCalls() {
        var calls = 0
        rule.setContent {
            CallDirectoryPanel(rooms, onCall = { calls++ }, modifier = Modifier.fillMaxSize())
        }
        val first = rule.onNodeWithContentDescription("Chat host Amina, room 100201")
            .assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val second = rule.onNodeWithContentDescription("Chat host Samir, room 100202")
            .assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val screen = rule.onRoot().fetchSemanticsNode().boundsInRoot
        assertEquals(first.top, second.top)
        assertTrue(first.right < second.left)
        assertTrue(first.left >= screen.left && second.right <= screen.right)
        val density = first.height / 186f
        assertTrue("Chat card must remain portrait-shaped", first.height > first.width)
        assertTrue("Cards must match Party's 186dp height", density in 2.5f..3.5f)
        rule.onNodeWithText("Hosting Race • room 100202").assertIsDisplayed()
        rule.onNodeWithContentDescription("Chat host Amina, room 100201").performClick()
        assertEquals("Card itself must not open another destination", 0, calls)
        rule.onAllNodesWithText("Call")[0].performClick()
        assertEquals(1, calls)
    }

    @Test
    fun topCardsKeepRankingAndExistingBrowseRoute() {
        var browse = 0
        rule.setContent {
            TopRoomsPanel(rooms, onOpenRooms = { browse++ }, modifier = Modifier.fillMaxSize())
        }
        val first = rule.onNodeWithContentDescription("Rank 1, room 100202")
            .assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val second = rule.onNodeWithContentDescription("Rank 2, room 100201")
            .assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertEquals(first.top, second.top)
        assertTrue(first.right < second.left && first.height > first.width)
        rule.onNodeWithText("#1").assertIsDisplayed()
        rule.onNodeWithText("7 in room").assertIsDisplayed()
        rule.onNodeWithContentDescription("Rank 1, room 100202").performClick()
        assertEquals(1, browse)
    }

    @Test
    @Config(qualifiers = "w320dp-h540dp-xxhdpi", sdk = [36])
    fun compactPhoneKeepsPortraitCardsInsideTheScreen() {
        rule.setContent {
            Column(Modifier.fillMaxSize()) {
                CallDirectoryPanel(rooms.take(2), onCall = {}, modifier = Modifier.weight(1f))
            }
        }
        val root = rule.onRoot().fetchSemanticsNode().boundsInRoot
        val second = rule.onNodeWithContentDescription("Chat host Samir, room 100202")
            .assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertTrue(second.right <= root.right && second.bottom <= root.bottom)
    }

    @Test
    fun partyDiscoveryCardsOpenOnlyTheirOwnRoomIds() {
        val opened = mutableListOf<String>()
        rule.setContent {
            androidx.compose.foundation.layout.Row {
                rooms.take(2).forEach { room ->
                    RoomDiscoveryCard(room, onClick = { opened += room.id },
                        modifier = Modifier.weight(1f))
                }
            }
        }
        rule.onNodeWithText("Amina").performClick()
        rule.onNodeWithText("Samir").performClick()
        assertEquals(listOf("100201", "100202"), opened)
    }
}