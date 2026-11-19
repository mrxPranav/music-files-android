package com.example.musicfiles

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

/** All audio files on the phone. Tap a song to play from it; the ⋮ button adds it to a playlist. */
class MediaFragment : Fragment(R.layout.fragment_list) {

    private lateinit var adapter: SongAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<FloatingActionButton>(R.id.fab).visibility = View.GONE

        val list = view.findViewById<RecyclerView>(R.id.list)
        list.layoutManager = LinearLayoutManager(requireContext())
        adapter = SongAdapter(
            actionIcon = android.R.drawable.ic_menu_more,
            onItemClick = { songs, index -> PlaybackManager.play(requireContext(), songs, index) },
            onAction = { song -> showAddToPlaylist(song) }
        )
        list.adapter = adapter

        val ctx = requireContext().applicationContext
        runAsync({ MediaRepository.songs(ctx) }) { songs ->
            if (!isAdded) return@runAsync
            adapter.submit(songs)
            view.findViewById<TextView>(R.id.emptyText).apply {
                setText(R.string.no_songs)
                visibility = if (songs.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun showAddToPlaylist(song: Song) {
        val ctx = requireContext()
        runAsync({ PlaylistRepository.getInstance(ctx).playlists() }) { playlists ->
            if (!isAdded) return@runAsync
            val names = playlists.map { it.name } + getString(R.string.new_playlist)
            MaterialAlertDialogBuilder(ctx)
                .setTitle(R.string.add_to_playlist)
                .setItems(names.toTypedArray()) { _, which ->
                    if (which == playlists.size) {
                        showCreatePlaylistDialog(ctx) { id -> addToPlaylist(song, id) }
                    } else {
                        addToPlaylist(song, playlists[which].id)
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    private fun addToPlaylist(song: Song, playlistId: Long) {
        val ctx = requireContext().applicationContext
        runAsync({ PlaylistRepository.getInstance(ctx).addSongs(playlistId, listOf(song)) }) { added ->
            if (!isAdded) return@runAsync
            val message = if (added > 0) R.string.added_to_playlist else R.string.already_in_playlist
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }
}
