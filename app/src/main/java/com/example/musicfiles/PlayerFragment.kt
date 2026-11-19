package com.example.musicfiles

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.media3.common.C
import androidx.media3.common.Player

/** Full-screen player: seek bar, previous/play/next, and volume. */
class PlayerFragment : Fragment(R.layout.fragment_player) {

    private var player: Player? = null
    private var userSeeking = false

    private lateinit var title: TextView
    private lateinit var artist: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var currentTime: TextView
    private lateinit var totalTime: TextView
    private lateinit var playPause: ImageButton
    private lateinit var audio: AudioManager

    private val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            render(player)
        }
    }

    // Moves the seek bar while the song plays
    private val ticker: Runnable = object : Runnable {
        override fun run() {
            val p = player ?: return
            if (!userSeeking) updateSeek(p)
            if (p.isPlaying) view?.postDelayed(this, TICK_INTERVAL_MS)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        title = view.findViewById(R.id.playerTitle)
        artist = view.findViewById(R.id.playerArtist)
        seekBar = view.findViewById(R.id.playerSeek)
        currentTime = view.findViewById(R.id.currentTime)
        totalTime = view.findViewById(R.id.totalTime)
        playPause = view.findViewById(R.id.playerPlayPause)
        audio = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager

        view.findViewById<ImageButton>(R.id.playerPrevious).setOnClickListener { PlaybackManager.previous() }
        playPause.setOnClickListener { PlaybackManager.togglePlayPause() }
        view.findViewById<ImageButton>(R.id.playerNext).setOnClickListener { PlaybackManager.next() }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) currentTime.text = formatDuration(progress.toLong())
            }

            override fun onStartTrackingTouch(bar: SeekBar) {
                userSeeking = true
            }

            override fun onStopTrackingTouch(bar: SeekBar) {
                userSeeking = false
                PlaybackManager.seekTo(bar.progress.toLong())
            }
        })

        setupVolume(view.findViewById(R.id.volumeSeek))

        PlaybackManager.connect(requireContext()) { p ->
            if (!isAdded) return@connect
            player = p
            p.addListener(playerListener)
            render(p)
        }
    }

    override fun onDestroyView() {
        view?.removeCallbacks(ticker)
        player?.removeListener(playerListener)
        player = null
        super.onDestroyView()
    }

    private fun render(p: Player) {
        val item = p.currentMediaItem
        title.text = item?.mediaMetadata?.title ?: getString(R.string.nothing_playing)
        artist.text = item?.mediaMetadata?.artist ?: ""
        playPause.setImageResource(
            if (p.isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        )
        updateSeek(p)

        view?.removeCallbacks(ticker)
        if (p.isPlaying) view?.postDelayed(ticker, TICK_INTERVAL_MS)
    }

    private fun updateSeek(p: Player) {
        val duration = if (p.duration > 0 && p.duration != C.TIME_UNSET) p.duration else 0L
        seekBar.max = duration.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        seekBar.progress = p.currentPosition.coerceIn(0L, duration).toInt()
        currentTime.text = formatDuration(p.currentPosition)
        totalTime.text = formatDuration(duration)
    }

    /** Volume controls the phone's music volume, same as the hardware buttons. */
    private fun setupVolume(volumeBar: SeekBar) {
        volumeBar.max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        volumeBar.progress = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
        volumeBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) audio.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
            }

            override fun onStartTrackingTouch(bar: SeekBar) = Unit
            override fun onStopTrackingTouch(bar: SeekBar) = Unit
        })
    }

    companion object {
        private const val TICK_INTERVAL_MS = 250L
    }
}
