package com.example.uas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.uas.databinding.ActivityMovieDetailsBinding
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class MovieDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMovieDetailsBinding
    private lateinit var genreAdapter: GenreAdapter
    private lateinit var prefManager: PrefManager
    private val usersCollectionRef = FirebaseFirestore.getInstance().collection("Users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi prefManager sebelum digunakan
        prefManager = PrefManager.getInstance(this@MovieDetailsActivity)

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

            usersCollectionRef.document(prefManager.getUsername()).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val storedFavorite = document.get("favorite") as MutableList<String>? ?: mutableListOf()

                        if (storedFavorite.contains(id.toString())) {
                            btnLike.visibility = View.GONE
                            btnUnlike.visibility = View.VISIBLE
                        } else {
                            btnLike.visibility = View.VISIBLE
                            btnUnlike.visibility = View.GONE
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("MovieDetailsActivity", "Tidak ada koneksi internet", exception)
                    Toast.makeText(this@MovieDetailsActivity, "Tidak ada koneksi internet", Toast.LENGTH_SHORT).show()
                }

            btnLike.setOnClickListener {
                usersCollectionRef.document(prefManager.getUsername()).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val storedFavorite = document.get("favorite") as MutableList<String>? ?: mutableListOf()
                            storedFavorite.add(id.toString())

                            val mapDocument = HashMap<String, Any>()
                            mapDocument["favorite"] = storedFavorite

                            usersCollectionRef.document(prefManager.getUsername()).update(mapDocument).addOnCompleteListener { firestoreTask ->
                                    if (firestoreTask.isSuccessful) {
                                        Toast.makeText(this@MovieDetailsActivity, "Anda menyukai film $nama", Toast.LENGTH_SHORT).show()
                                        btnLike.visibility = View.GONE
                                        btnUnlike.visibility = View.VISIBLE
                                    } else {
                                        Toast.makeText(this@MovieDetailsActivity, firestoreTask.exception?.message, Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d("MovieDetailsActivity", "Tidak ada koneksi internet", exception)
                        Toast.makeText(this@MovieDetailsActivity, "Tidak ada koneksi internet", Toast.LENGTH_SHORT).show()
                    }
            }

            btnUnlike.setOnClickListener {
                usersCollectionRef.document(prefManager.getUsername()).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val storedFavorite = document.get("favorite") as MutableList<String>? ?: mutableListOf()

                            if (storedFavorite.contains(id.toString())) {
                                storedFavorite.remove(id.toString())

                                val mapDocument = HashMap<String, Any>()
                                mapDocument["favorite"] = storedFavorite

                                usersCollectionRef.document(prefManager.getUsername()).update(mapDocument)
                                    .addOnCompleteListener { firestoreTask ->
                                        if (firestoreTask.isSuccessful) {
                                            Toast.makeText(this@MovieDetailsActivity, "Anda tidak menyukai film $nama", Toast.LENGTH_SHORT).show()
                                            btnLike.visibility = View.VISIBLE
                                            btnUnlike.visibility = View.GONE
                                        } else {
                                            Toast.makeText(this@MovieDetailsActivity, firestoreTask.exception?.message, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d("MovieDetailsActivity", "Tidak ada koneksi internet", exception)
                        Toast.makeText(this@MovieDetailsActivity, "Tidak ada koneksi internet", Toast.LENGTH_SHORT).show()
                    }
            }

            btnBack.setOnClickListener {
                startActivity(Intent(this@MovieDetailsActivity, HomeUserActivity::class.java))
                finish()
            }
        }
    }
}