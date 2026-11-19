package com.example.musicfiles

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlaylistAdapter(
    private val onClick: (PlaylistInfo) -> Unit,
    private val onLongClick: (PlaylistInfo) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {

    private var playlists: List<PlaylistInfo> = emptyList()

    fun submit(list: List<PlaylistInfo>) {
        playlists = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_playlist, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = playlists.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.name.text = playlist.name
        holder.count.text = holder.itemView.context.getString(R.string.songs_count, playlist.songCount)
        holder.itemView.setOnClickListener { onClick(playlist) }
        holder.itemView.setOnLongClickListener {
            onLongClick(playlist)
            true
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.playlistName)
        val count: TextView = view.findViewById(R.id.playlistCount)
    }
}
