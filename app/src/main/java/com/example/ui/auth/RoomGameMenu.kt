package com.example.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/** Each menu target has its own callback; the dialog has no clickable parent or overlay. */
@Composable
internal fun RoomGameMenu(
    onDismiss: () -> Unit,
    onPkGame: () -> Unit,
    onLudo: () -> Unit,
    onGamiRace: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Games") },
        text = {
            Column {
                TextButton(onClick = onPkGame) { Text("PK Game") }
                TextButton(onClick = onLudo) { Text("Ludo") }
                TextButton(onClick = onGamiRace) { Text("GAMI Race") }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}