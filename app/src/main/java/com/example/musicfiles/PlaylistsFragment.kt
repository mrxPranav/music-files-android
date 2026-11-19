package com.example.musicfiles

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

/** List of playlists. "+" creates one, tap opens it, long-press deletes it. */
class PlaylistsFragment : Fragment(R.layout.fragment_list) {

    private lateinit var adapter: PlaylistAdapter
    private lateinit var emptyText: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        emptyText = view.findViewById(R.id.emptyText)
        emptyText.setText(R.string.no_playlists)

        val list = view.findViewById<RecyclerView>(R.id.list)
        list.layoutManager = LinearLayoutManager(requireContext())
        adapter = PlaylistAdapter(
            onClick = { openPlaylist(it) },
            onLongClick = { confirmDelete(it) }
        )
        list.adapter = adapter

        view.findViewById<FloatingActionButton>(R.id.fab).apply {
            visibility = View.VISIBLE
            setOnClickListener { showCreatePlaylistDialog(requireContext()) { reload() } }
        }
    }

    override fun onResume() {
        super.onResume()
        reload()
    }

    private fun reload() {
        if (!isAdded) return
        val ctx = requireContext().applicationContext
        runAsync({ PlaylistRepository.getInstance(ctx).playlists() }) { playlists ->
            if (!isAdded) return@runAsync
            adapter.submit(playlists)
            emptyText.visibility = if (playlists.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun openPlaylist(playlist: PlaylistInfo) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, PlaylistDetailFragment.newInstance(playlist.id, playlist.name))
            .addToBackStack(null)
            .commit()
    }

    private fun confirmDelete(playlist: PlaylistInfo) {
        val ctx = requireContext().applicationContext
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_playlist_title, playlist.name))
            .setMessage(R.string.delete_playlist_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                runAsync({ PlaylistRepository.getInstance(ctx).deletePlaylist(playlist.id) }) { reload() }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
