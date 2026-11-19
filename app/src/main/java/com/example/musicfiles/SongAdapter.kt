package com.example.musicfiles

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Shows songs as rows.
 * [onItemClick] gets the full list and the tapped position, so playback can queue the rest.
 * [onAction] is the small button on each row (add to playlist, or remove from playlist).
 */
class SongAdapter(
    private val actionIcon: Int,
    private val onItemClick: (List<Song>, Int) -> Unit,
    private val onAction: (Song) -> Unit
) : RecyclerView.Adapter<SongAdapter.ViewHolder>() {

    private var songs: List<Song> = emptyList()

    fun submit(list: List<Song>) {
        songs = list
        notifyDataSetChanged()
    }

    fun currentSongs(): List<Song> = songs

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = songs.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = songs[position]
        holder.title.text = song.title
        holder.subtitle.text = "${song.artist}  •  ${formatDuration(song.durationMs)}"
        holder.action.setImageResource(actionIcon)
        holder.action.setOnClickListener { onAction(song) }
        holder.itemView.setOnClickListener { onItemClick(songs, holder.bindingAdapterPosition) }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.songTitle)
        val subtitle: TextView = view.findViewById(R.id.songSubtitle)
        val action: ImageButton = view.findViewById(R.id.songAction)
    }
}
