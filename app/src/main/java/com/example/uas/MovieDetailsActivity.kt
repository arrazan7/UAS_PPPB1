package com.example.uas

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.uas.databinding.ActivityMovieDetailsBinding

class MovieDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMovieDetailsBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            val id = intent.getStringExtra(HomeFragment.EXTRA_ID)
            val gambar = intent.getStringExtra(HomeFragment.EXTRA_IMAGE)
            val nama = intent.getStringExtra(HomeFragment.EXTRA_NAMA)
            val direktor = intent.getStringExtra(HomeFragment.EXTRA_DIREKTOR)
            val story = intent.getStringExtra(HomeFragment.EXTRA_STORY)
        }
    }
}