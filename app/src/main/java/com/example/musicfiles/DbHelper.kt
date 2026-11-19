package com.example.musicfiles

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/** Creates the SQLite database that stores playlists. */
class DbHelper(context: Context) : SQLiteOpenHelper(context, "music.db", null, 1) {

    override fun onConfigure(db: SQLiteDatabase) {
        // Needed so deleting a playlist also deletes its songs
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE playlists (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE,
                created_at INTEGER NOT NULL
            )"""
        )
        db.execSQL(
            """CREATE TABLE playlist_items (
                playlist_id INTEGER NOT NULL REFERENCES playlists(id) ON DELETE CASCADE,
                media_id INTEGER NOT NULL,
                uri TEXT NOT NULL,
                title TEXT NOT NULL,
                artist TEXT NOT NULL,
                duration INTEGER NOT NULL,
                position INTEGER NOT NULL,
                PRIMARY KEY (playlist_id, media_id)
            )"""
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
}
