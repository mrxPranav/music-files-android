package com.example.musicfiles

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

data class Song(
    val mediaId: Long,
    val uri: String,
    val title: String,
    val artist: String,
    val durationMs: Long
) {
    fun toMediaItem(): MediaItem = MediaItem.Builder()
        .setMediaId(mediaId.toString())
        .setUri(uri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .build()
        )
        .build()
}

fun formatDuration(ms: Long): String = "%d:%02d".format(ms / 60000, ms / 1000 % 60)
