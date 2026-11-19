package com.example.musicfiles

import android.content.Context
import android.content.ContentUris
import android.provider.MediaStore

/** Reads all music files from the phone's MediaStore index. */
object MediaRepository {

    fun songs(context: Context): List<Song> {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION
        )
        // Only real music files, not ringtones/notifications/recordings
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        val songs = mutableListOf<Song>()
        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                songs += Song(
                    mediaId = id,
                    uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id).toString(),
                    title = cursor.getString(titleCol) ?: "Unknown",
                    artist = cursor.getString(artistCol)
                        ?.takeIf { it != "<unknown>" }
                        ?: context.getString(R.string.unknown_artist),
                    durationMs = cursor.getLong(durationCol)
                )
            }
        }
        return songs
    }
}
