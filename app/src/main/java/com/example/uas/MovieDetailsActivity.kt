package com.example.uas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.uas.databinding.ActivityMovieDetailsBinding
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.squareup.picasso.Picasso

class MovieDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMovieDetailsBinding
    private lateinit var genreAdapter: GenreAdapter
    private var genreList = ArrayList<MovieData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            val id = intent.getStringExtra(HomeFragment.EXTRA_ID)
            val gambar = intent.getStringExtra(HomeFragment.EXTRA_IMAGE)
            val nama = intent.getStringExtra(HomeFragment.EXTRA_NAMA)
            val direktor = intent.getStringExtra(HomeFragment.EXTRA_DIREKTOR)
            val rate = intent.getStringExtra(HomeFragment.EXTRA_RATING)
            val story = intent.getStringExtra(HomeFragment.EXTRA_STORY)
            val genre = intent.getStringArrayExtra(HomeFragment.EXTRA_GENRE) ?: emptyArray()

            Picasso.get().load(gambar).into(posterMovie)
            titleMovie.text = nama
            dirMovie.text = direktor
            rateMovie.text = "$rate / 10"
            storyMovie.text = story

            val flexLayoutManager = FlexboxLayoutManager(this@MovieDetailsActivity)
            flexLayoutManager.flexDirection = FlexDirection.ROW
            flexLayoutManager.justifyContent = JustifyContent.FLEX_START

            genreRV.setHasFixedSize(true)
            genreRV.layoutManager = flexLayoutManager
            genreAdapter = GenreAdapter(genre)
            genreRV.adapter = genreAdapter

            genreAdapter.onItemClick = {
                Toast.makeText(this@MovieDetailsActivity, "${it[0]}", Toast.LENGTH_SHORT).show()
            }

            btnBack.setOnClickListener {
                startActivity(Intent(this@MovieDetailsActivity, HomeUserActivity::class.java))
                finish()
            }
        }
    }
}