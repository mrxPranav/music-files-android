package com.example.musicfiles

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import androidx.fragment.app.Fragment

/** Dashboard: total audio count, playlist count, and songs across playlists. */
class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<MaterialButton>(R.id.openMedia).setOnClickListener {
            (requireActivity() as MainActivity).selectTab(R.id.nav_media)
        }
        view.findViewById<MaterialButton>(R.id.openPlaylists).setOnClickListener {
            (requireActivity() as MainActivity).selectTab(R.id.nav_playlists)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshCounts()
    }

    private fun refreshCounts() {
        if (!isAdded) return
        val ctx = requireContext().applicationContext
        runAsync({
            val repo = PlaylistRepository.getInstance(ctx)
            listOf(
                MediaRepository.songs(ctx).size,
                repo.playlists().size,
                repo.totalPlaylistSongs()
            )
        }) { counts ->
            val v = view ?: return@runAsync
            v.findViewById<TextView>(R.id.totalAudioCount).text = counts[0].toString()
            v.findViewById<TextView>(R.id.playlistCount).text = counts[1].toString()
            v.findViewById<TextView>(R.id.playlistSongCount).text = counts[2].toString()
        }
    }
}
