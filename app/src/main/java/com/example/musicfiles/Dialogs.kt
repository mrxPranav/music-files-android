package com.example.musicfiles

import android.content.Context
import android.util.TypedValue
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/** Asks for a name, creates the playlist, then calls [onCreated] with its id. */
fun showCreatePlaylistDialog(context: Context, onCreated: (Long) -> Unit) {
    val padding = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 20f, context.resources.displayMetrics
    ).toInt()
    val input = EditText(context).apply {
        hint = context.getString(R.string.playlist_name_hint)
        setPadding(padding, padding / 2, padding, padding / 2)
        setSingleLine(true)
    }

    MaterialAlertDialogBuilder(context)
        .setTitle(R.string.new_playlist)
        .setView(input)
        .setPositiveButton(R.string.create) { _, _ ->
            val name = input.text.toString()
            runAsync({ PlaylistRepository.getInstance(context).createPlaylist(name) }) { id ->
                if (id == -1L) {
                    Toast.makeText(context, R.string.playlist_name_invalid, Toast.LENGTH_SHORT).show()
                } else {
                    onCreated(id)
                }
            }
        }
        .setNegativeButton(R.string.cancel, null)
        .show()
}
