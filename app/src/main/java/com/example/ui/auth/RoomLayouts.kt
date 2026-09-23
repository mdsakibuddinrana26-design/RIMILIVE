package com.example.ui.auth

/** The old 12-seat selection now opens the same existing room flow with eight seats. */
internal data class RoomLayoutSpec(
    val key: String,
    val cameraSeats: IntRange,
    val audioSeats: IntRange
) {
    val totalSeats: Int get() = cameraSeats.count() + audioSeats.count()
}

internal fun roomLayoutSpec(key: String): RoomLayoutSpec =
    if (key == "15-seat") {
        RoomLayoutSpec("15-seat", 1..5, 6..15)
    } else {
        // Previously saved "12-seat" documents must not expose their removed seats.
        RoomLayoutSpec("8-seat", 1..3, 4..8)
    }