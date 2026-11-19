package com.example.musicfiles

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/** One playlist: play all, add songs, and remove songs (playlist only, files are not deleted). */
class PlaylistDetailFragment : Fragment(R.layout.fragment_playlist_detail) {

    private lateinit var adapter: SongAdapter
    private lateinit var emptyText: TextView

    private val playlistId: Long by lazy { requireArguments().getLong(ARG_ID) }

    companion object {
        private const val ARG_ID = "playlist_id"
        private const val ARG_NAME = "playlist_name"

        fun newInstance(id: Long, name: String) = PlaylistDetailFragment().apply {
            arguments = bundleOf(ARG_ID to id, ARG_NAME to name)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<TextView>(R.id.playlistTitle).text = requireArguments().getString(ARG_NAME)
        emptyText = view.findViewById(R.id.emptyText)

        val list = view.findViewById<RecyclerView>(R.id.list)
        list.layoutManager = LinearLayoutManager(requireContext())
        adapter = SongAdapter(
            actionIcon = android.R.drawable.ic_menu_close_clear_cancel,
            onItemClick = { songs, index -> PlaybackManager.play(requireContext(), songs, index) },
            onAction = { song -> removeSong(song) }
        )
        list.adapter = adapter

        view.findViewById<MaterialButton>(R.id.playAll).setOnClickListener {
            val songs = adapter.currentSongs()
            if (songs.isEmpty()) {
                Toast.makeText(requireContext(), R.string.playlist_empty, Toast.LENGTH_SHORT).show()
            } else {
                PlaybackManager.play(requireContext(), songs, 0)
            }
        }
        view.findViewById<MaterialButton>(R.id.addSongs).setOnClickListener { showAddSongs() }
    }

    override fun onResume() {
        super.onResume()
        reload()
    }

    private fun reload() {
        if (!isAdded) return
        val ctx = requireContext().applicationContext
        runAsync({ PlaylistRepository.getInstance(ctx).songs(playlistId) }) { songs ->
            if (!isAdded) return@runAsync
            adapter.submit(songs)
            emptyText.visibility = if (songs.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun removeSong(song: Song) {
        val ctx = requireContext().applicationContext
        runAsync({ PlaylistRepository.getInstance(ctx).removeSong(playlistId, song.mediaId) }) { reload() }
    }

    /** Shows every song on the phone that is not already in this playlist, with checkboxes. */
    private fun showAddSongs() {
        val ctx = requireContext().applicationContext
        runAsync({
            val existing = PlaylistRepository.getInstance(ctx).songIds(playlistId)
            MediaRepository.songs(ctx).filter { it.mediaId !in existing }
        }) { candidates ->
            if (!isAdded) return@runAsync
            if (candidates.isEmpty()) {
                Toast.makeText(requireContext(), R.string.all_songs_added, Toast.LENGTH_SHORT).show()
                return@runAsync
            }
            val checked = BooleanArray(candidates.size)
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.add_songs)
                .setMultiChoiceItems(
                    candidates.map { "${it.title}  -  ${it.artist}" }.toTypedArray(),
                    checked
                ) { _, which, isChecked -> checked[which] = isChecked }
                .setPositiveButton(R.string.add) { _, _ ->
                    val selected = candidates.filterIndexed { i, _ -> checked[i] }
                    if (selected.isNotEmpty()) {
                        runAsync({ PlaylistRepository.getInstance(ctx).addSongs(playlistId, selected) }) { reload() }
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }
}
