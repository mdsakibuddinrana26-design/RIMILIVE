package com.example.ui.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomLayoutsTest {
    @Test
    fun formerTwelveSeatRoomIsExactlyThreeCameraAndFiveAudio() {
        val spec = roomLayoutSpec("8-seat")
        assertEquals(8, spec.totalSeats)
        assertEquals((1..3).toList(), spec.cameraSeats.toList())
        assertEquals((4..8).toList(), spec.audioSeats.toList())
        assertEquals(spec, roomLayoutSpec("12-seat"))
        assertTrue(12 !in spec.audioSeats)
    }

    @Test
    fun fifteenSeatRoomIsExactlyFiveCameraAndTenAudio() {
        val spec = roomLayoutSpec("15-seat")
        assertEquals(15, spec.totalSeats)
        assertEquals((1..5).toList(), spec.cameraSeats.toList())
        assertEquals((6..15).toList(), spec.audioSeats.toList())
        assertEquals(15, spec.audioSeats.last)
    }

    @Test
    fun allFourVisualPreviewsKeepTheTwoSupportedRoomCapacities() {
        assertEquals(listOf("8-seat", "8-seat", "15-seat", "15-seat"),
            (0..3).map(::roomLayoutForPreview))
    }
}