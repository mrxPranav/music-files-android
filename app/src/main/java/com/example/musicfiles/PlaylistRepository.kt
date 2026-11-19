package com.example.musicfiles

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase

data class PlaylistInfo(val id: Long, val name: String, val songCount: Int)

/** All playlist reads and writes go through here. */
class PlaylistRepository private constructor(context: Context) {

    private val db: SQLiteDatabase = DbHelper(context.applicationContext).writableDatabase

    companion object {
        @Volatile
        private var instance: PlaylistRepository? = null

        fun getInstance(context: Context): PlaylistRepository =
            instance ?: synchronized(this) {
                instance ?: PlaylistRepository(context).also { instance = it }
            }
    }

    fun playlists(): List<PlaylistInfo> {
        val result = mutableListOf<PlaylistInfo>()
        db.rawQuery(
            """SELECT p.id, p.name, COUNT(i.media_id)
               FROM playlists p
               LEFT JOIN playlist_items i ON i.playlist_id = p.id
               GROUP BY p.id
               ORDER BY p.name COLLATE NOCASE""",
            null
        ).use { c ->
            while (c.moveToNext()) {
                result += PlaylistInfo(c.getLong(0), c.getString(1), c.getInt(2))
            }
        }
        return result
    }

    /** Returns the new playlist id, or -1 if the name is empty or already used. */
    fun createPlaylist(name: String): Long {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return -1L
        val values = ContentValues().apply {
            put("name", trimmed)
            put("created_at", System.currentTimeMillis())
        }
        return db.insert("playlists", null, values)
    }

    fun deletePlaylist(playlistId: Long) {
        db.delete("playlists", "id = ?", arrayOf(playlistId.toString()))
    }

    fun songs(playlistId: Long): List<Song> {
        val result = mutableListOf<Song>()
        db.rawQuery(
            "SELECT media_id, uri, title, artist, duration FROM playlist_items " +
                "WHERE playlist_id = ? ORDER BY position",
            arrayOf(playlistId.toString())
        ).use { c ->
            while (c.moveToNext()) {
                result += Song(
                    mediaId = c.getLong(0),
                    uri = c.getString(1),
                    title = c.getString(2),
                    artist = c.getString(3),
                    durationMs = c.getLong(4)
                )
            }
        }
        return result
    }

    fun songIds(playlistId: Long): Set<Long> {
        val result = mutableSetOf<Long>()
        db.rawQuery(
            "SELECT media_id FROM playlist_items WHERE playlist_id = ?",
            arrayOf(playlistId.toString())
        ).use { c ->
            while (c.moveToNext()) result += c.getLong(0)
        }
        return result
    }

    /** Adds songs to the end of the playlist. Returns how many were added (duplicates are skipped). */
    fun addSongs(playlistId: Long, songs: List<Song>): Int {
        var added = 0
        db.beginTransaction()
        try {
            var position = nextPosition(playlistId)
            for (song in songs) {
                val values = ContentValues().apply {
                    put("playlist_id", playlistId)
                    put("media_id", song.mediaId)
                    put("uri", song.uri)
                    put("title", song.title)
                    put("artist", song.artist)
                    put("duration", song.durationMs)
                    put("position", position)
                }
                val rowId = db.insertWithOnConflict(
                    "playlist_items", null, values, SQLiteDatabase.CONFLICT_IGNORE
                )
                if (rowId != -1L) {
                    added++
                    position++
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return added
    }

    fun removeSong(playlistId: Long, mediaId: Long) {
        db.delete(
            "playlist_items",
            "playlist_id = ? AND media_id = ?",
            arrayOf(playlistId.toString(), mediaId.toString())
        )
    }

    fun totalPlaylistSongs(): Int =
        db.rawQuery("SELECT COUNT(*) FROM playlist_items", null).use { c ->
            c.moveToFirst()
            c.getInt(0)
        }

    private fun nextPosition(playlistId: Long): Int =
        db.rawQuery(
            "SELECT COALESCE(MAX(position), 0) + 1 FROM playlist_items WHERE playlist_id = ?",
            arrayOf(playlistId.toString())
        ).use { c ->
            c.moveToFirst()
            c.getInt(0)
        }
}
