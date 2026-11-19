package com.example.musicfiles

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.media3.common.Player
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var nowPlayingBar: LinearLayout
    private lateinit var nowPlayingTitle: TextView
    private lateinit var nowPlayingArtist: TextView
    private lateinit var nowPlayingProgress: ProgressBar
    private lateinit var playPauseButton: ImageButton

    private var player: Player? = null

    private val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            renderNowPlaying(player)
        }
    }

    // Refreshes the mini player progress bar every half second while music plays
    private val progressTicker: Runnable = object : Runnable {
        override fun run() {
            val p = player ?: return
            renderNowPlaying(p)
            if (p.isPlaying) nowPlayingBar.postDelayed(this, PROGRESS_INTERVAL_MS)
        }
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (!hasAudioPermission()) {
                Toast.makeText(this, R.string.no_permission, Toast.LENGTH_LONG).show()
            }
            // Show the home screen either way; it shows zero counts without permission
            bottomNav.selectedItemId = R.id.nav_home
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottomNav)
        nowPlayingBar = findViewById(R.id.nowPlayingBar)
        nowPlayingTitle = findViewById(R.id.nowPlayingTitle)
        nowPlayingArtist = findViewById(R.id.nowPlayingArtist)
        nowPlayingProgress = findViewById(R.id.nowPlayingProgress)
        playPauseButton = findViewById(R.id.nowPlayingPlayPause)

        // Every tab change goes through the bottom bar, which calls showTab
        bottomNav.setOnItemSelectedListener { item ->
            showTab(item.itemId)
            true
        }

        // Tapping the mini player opens the full player screen
        nowPlayingBar.setOnClickListener { openFullPlayer() }
        findViewById<ImageButton>(R.id.nowPlayingPrevious).setOnClickListener { PlaybackManager.previous() }
        playPauseButton.setOnClickListener { PlaybackManager.togglePlayPause() }
        findViewById<ImageButton>(R.id.nowPlayingNext).setOnClickListener { PlaybackManager.next() }

        PlaybackManager.connect(this) { controller ->
            player = controller
            controller.addListener(playerListener)
            renderNowPlaying(controller)
        }

        if (savedInstanceState == null) {
            requestPermissionsIfNeeded()
        }
    }

    override fun onDestroy() {
        nowPlayingBar.removeCallbacks(progressTicker)
        player?.removeListener(playerListener)
        player = null
        // Only disconnects the UI. Playback continues in PlaybackService.
        PlaybackManager.release()
        super.onDestroy()
    }

    /** Switches tab from a button inside a screen (for example, the dashboard). */
    fun selectTab(itemId: Int) {
        bottomNav.selectedItemId = itemId
    }

    private fun openFullPlayer() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, PlayerFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun showTab(itemId: Int) {
        val fragment: Fragment = when (itemId) {
            R.id.nav_media -> MediaFragment()
            R.id.nav_playlists -> PlaylistsFragment()
            else -> HomeFragment()
        }
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun requestPermissionsIfNeeded() {
        val needed = buildList {
            if (!hasAudioPermission()) add(audioPermission())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (needed.isEmpty()) {
            bottomNav.selectedItemId = R.id.nav_home
        } else {
            permissionLauncher.launch(needed.toTypedArray())
        }
    }

    private fun hasAudioPermission(): Boolean =
        checkSelfPermission(audioPermission()) == PackageManager.PERMISSION_GRANTED

    private fun audioPermission(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    private fun renderNowPlaying(p: Player) {
        val item = p.currentMediaItem
        nowPlayingBar.visibility = if (item == null) View.GONE else View.VISIBLE
        nowPlayingTitle.text = item?.mediaMetadata?.title ?: ""
        nowPlayingArtist.text = item?.mediaMetadata?.artist ?: ""

        val duration = p.duration
        nowPlayingProgress.progress = if (duration > 0) {
            (p.currentPosition * 1000 / duration).toInt().coerceIn(0, 1000)
        } else {
            0
        }

        playPauseButton.setImageResource(
            if (p.isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        )

        nowPlayingBar.removeCallbacks(progressTicker)
        if (p.isPlaying) nowPlayingBar.postDelayed(progressTicker, PROGRESS_INTERVAL_MS)
    }

    companion object {
        private const val PROGRESS_INTERVAL_MS = 500L
    }
}
