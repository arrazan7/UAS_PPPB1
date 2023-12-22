package com.example.uas.roomDatabase

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.uas.R
import com.example.uas.databinding.MoviePosterBinding
import com.squareup.picasso.Picasso
import java.io.File


class MovieRoomAdapter (private val movieRoomList: List<MovieRoom>)
    : RecyclerView.Adapter<MovieRoomAdapter.ImagesViewHolder>() {

    var onItemClick: ((MovieRoom) -> Unit)? = null

    inner class ImagesViewHolder(var binding: MoviePosterBinding)
        : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesViewHolder {
        val binding = MoviePosterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImagesViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return movieRoomList.size
    }

    override fun onBindViewHolder(holder: ImagesViewHolder, position: Int) {
        with(holder.binding) {
            with(movieRoomList[position]) {
                // Mengatur nama poster ke setiap tampilan recyclerview
                namaMovie.text = nama

                // Mendapatkan jalur gambar dari Room Database
                val imagePath = gambar

                // Memuat gambar dari jalur file ke dalam ShapeableImageView
                val imageFile = File(imagePath)
                if (imageFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                    posterMovie.setImageBitmap(bitmap)
                } else {
                    // Gambar tidak ditemukan, mungkin hendak menampilkan gambar placeholder di sini
                    posterMovie.setImageResource(R.drawable.upload_image)
                }
            }
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(movieRoomList[position])
        }
    }
}