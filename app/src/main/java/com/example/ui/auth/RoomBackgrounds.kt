package com.example.ui.auth

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R

internal data class RoomBackgroundOption(
    val id: String,
    val title: String,
    val drawableId: Int,
    val night: Boolean
)

internal val roomBackgrounds = listOf(
    RoomBackgroundOption("sunrise_sea", "Sunrise Sea", R.drawable.rimi_bg_sunrise_sea, false),
    RoomBackgroundOption("nature_view", "Nature View", R.drawable.rimi_bg_nature_view, false),
    RoomBackgroundOption("cozy_day_room", "Cozy Day Room", R.drawable.rimi_bg_cozy_day_room, false),
    RoomBackgroundOption("garden_view", "Garden View", R.drawable.rimi_bg_garden_view, false),
    RoomBackgroundOption("moonlight_sea", "Moonlight Sea", R.drawable.rimi_bg_moonlight_sea, true),
    RoomBackgroundOption("city_night", "City Night", R.drawable.rimi_bg_city_night, true),
    RoomBackgroundOption("dream_sky", "Dream Sky", R.drawable.rimi_bg_dream_sky, true),
    RoomBackgroundOption("luxury_night_room", "Luxury Night Room",
        R.drawable.rimi_bg_luxury_night_room, true)
)

/** Device-only preference, scoped to the signed-in user; null restores the original orange. */
internal class RoomBackgroundPreferences(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "rimi_room_backgrounds", Context.MODE_PRIVATE
    )

    fun load(userId: String): RoomBackgroundOption? {
        if (userId.isBlank()) return null
        val id = preferences.getString("background:$userId", null)
        return roomBackgrounds.firstOrNull { it.id == id }
    }

    fun save(userId: String, id: String?): Boolean {
        require(id == null || roomBackgrounds.any { it.id == id }) {
            "Unknown Party Room background"
        }
        if (userId.isBlank()) return false
        val editor = preferences.edit()
        if (id == null) editor.remove("background:$userId")
        else editor.putString("background:$userId", id)
        return editor.commit()
    }
}

/** Drawn first in the room Box so seats and all controls remain above the image. */
@Composable
internal fun BoxScope.RoomBackgroundLayer(background: RoomBackgroundOption?) {
    if (background != null) {
        Image(
            painter = painterResource(background.drawableId),
            contentDescription = "Room background: ${background.title}",
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )
    }
}