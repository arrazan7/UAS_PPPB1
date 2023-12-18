package com.example.uas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.uas.databinding.MoviePosterAdminBinding
import com.squareup.picasso.Picasso

class MovieAdapter (private val movieList: List<MovieData>)
    : RecyclerView.Adapter<MovieAdapter.ImagesViewHolder>() {

    var onItemClick: ((MovieData) -> Unit)? = null

    inner class ImagesViewHolder(var binding: MoviePosterAdminBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesViewHolder {
        val binding = MoviePosterAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImagesViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return movieList.size
    }

    override fun onBindViewHolder(holder: ImagesViewHolder, position: Int) {
        with(holder.binding) {
            with(movieList[position]) {
                Picasso.get().load(gambar).into(posterMovieAdmin)
                namaMovieAdmin.text = nama
            }
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(movieList[position])
        }
    }
}