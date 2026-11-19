package com.example.musicfiles

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken

/** Connects the UI to [PlaybackService] and sends play commands to it. */
object PlaybackManager {

    private const val TAG = "PlaybackManager"

    private var controller: MediaController? = null
    private var connecting = false
    private val pending = mutableListOf<(MediaController) -> Unit>()

    /** Calls [onReady] on the main thread once the controller is connected. */
    fun connect(context: Context, onReady: (MediaController) -> Unit) {
        controller?.let {
            onReady(it)
            return
        }
        pending += onReady
        if (connecting) return
        connecting = true

        val app = context.applicationContext
        val token = SessionToken(app, ComponentName(app, PlaybackService::class.java))
        val future = MediaController.Builder(app, token).buildAsync()
        future.addListener({
            try {
                val connected = future.get()
                controller = connected
                pending.forEach { it(connected) }
            } catch (e: Exception) {
                Log.e(TAG, "Could not connect to playback service", e)
            } finally {
                pending.clear()
                connecting = false
            }
        }, ContextCompat.getMainExecutor(app))
    }

    /** Replaces the queue with [songs] and starts playing from [startIndex]. */
    fun play(context: Context, songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return
        connect(context) { c ->
            c.setMediaItems(songs.map { it.toMediaItem() }, startIndex, 0L)
            c.prepare()
            c.play()
        }
    }

    fun togglePlayPause() {
        controller?.let { if (it.isPlaying) it.pause() else it.play() }
    }

    fun next() {
        controller?.seekToNextMediaItem()
    }

    /** Goes to the start of the song, or to the previous song if already near the start. */
    fun previous() {
        controller?.seekToPrevious()
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
    }

    /** Disconnects the UI. Playback keeps going in the service. */
    fun release() {
        controller?.release()
        controller = null
        connecting = false
        pending.clear()
    }
}
