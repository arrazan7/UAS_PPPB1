package com.example.uas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.uas.databinding.EachGenreBinding

class GenreAdapter (private val genreList: Array<String>)
    : RecyclerView.Adapter<GenreAdapter.GenreViewHolder>() {

    var onItemClick: ((Array<String>) -> Unit)? = null

    inner class GenreViewHolder(var binding: EachGenreBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        val binding = EachGenreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GenreViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        with(holder.binding) {
            genreText.text = genreList[position]
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(arrayOf(genreList[position]))
        }
    }

    override fun getItemCount(): Int {
        return genreList.size
    }
}