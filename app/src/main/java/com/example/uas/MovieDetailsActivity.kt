package com.example.uas

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.uas.databinding.ActivityMovieDetailsBinding
import com.example.uas.roomDatabase.MovieDao
import com.example.uas.roomDatabase.MovieRoomDatabase
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MovieDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMovieDetailsBinding
    private lateinit var genreAdapter: GenreAdapter
    private lateinit var prefManager: PrefManager
    private lateinit var mMovieDao: MovieDao
    private lateinit var executorService: ExecutorService
    private val usersCollectionRef = FirebaseFirestore.getInstance().collection("Users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi prefManager sebelum digunakan
        prefManager = PrefManager.getInstance(this@MovieDetailsActivity)

        executorService = Executors.newSingleThreadExecutor()
        val movieRoomDb = MovieRoomDatabase.getDatabase(this@MovieDetailsActivity)
        mMovieDao = movieRoomDb!!.movieDao()!!

        with(binding) {
            // Mengatur tampilan genre
            val flexLayoutManager = FlexboxLayoutManager(this@MovieDetailsActivity)
            flexLayoutManager.flexDirection = FlexDirection.ROW
            flexLayoutManager.justifyContent = JustifyContent.FLEX_START

            genreRV.setHasFixedSize(true)
            genreRV.layoutManager = flexLayoutManager


            if (mMovieDao.isNetworkAvailable(this@MovieDetailsActivity)) {
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
            }
            else {
                val id_room = intent.getStringExtra(HomeFragment.EXTRA_ROOM_ID)
                val gambar_room = intent.getStringExtra(HomeFragment.EXTRA_ROOM_IMAGE)
                val nama_room = intent.getStringExtra(HomeFragment.EXTRA_ROOM_NAMA)
                val direktor_room = intent.getStringExtra(HomeFragment.EXTRA_ROOM_DIREKTOR)
                val rate_room = intent.getStringExtra(HomeFragment.EXTRA_ROOM_RATING)
                val story_room = intent.getStringExtra(HomeFragment.EXTRA_ROOM_STORY)
                val genre_room = intent.getStringExtra(HomeFragment.EXTRA_ROOM_GENRE)
                val genre_room_list = mMovieDao.fromStringToList(genre_room)

                // Mendapatkan jalur gambar dari Room Database
                val imagePath = gambar_room

                // Memuat gambar dari jalur file ke dalam ShapeableImageView
                val imageFile = File(imagePath)
                if (imageFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                    posterMovie.setImageBitmap(bitmap)
                } else {
                    // Gambar tidak ditemukan, mungkin hendak menampilkan gambar placeholder di sini
                    posterMovie.setImageResource(R.drawable.upload_image)
                }
                titleMovie.text = nama_room
                dirMovie.text = direktor_room
                rateMovie.text = "$rate_room / 10"
                storyMovie.text = story_room

                genreAdapter = GenreAdapter(genre_room_list?.toTypedArray() ?: emptyArray())
                genreRV.adapter = genreAdapter

                genreAdapter.onItemClick = {
                    Toast.makeText(this@MovieDetailsActivity, "${it[0]}", Toast.LENGTH_SHORT).show()
                }

                btnLike.setOnClickListener {
                    Toast.makeText(this@MovieDetailsActivity, "Tidak ada koneksi internet", Toast.LENGTH_SHORT).show()
                }
            }

            btnBack.setOnClickListener {
                startActivity(Intent(this@MovieDetailsActivity, HomeUserActivity::class.java))
                finish()
            }
        }
    }

    private fun deleteRoomDatabase() {
        // Mendapatkan path database
        val databasePath = applicationContext.getDatabasePath("movie_database").absolutePath

        // Menutup koneksi ke database jika masih terbuka
        MovieRoomDatabase.getDatabase(this@MovieDetailsActivity)?.close()

        // Menghapus database
        val databaseFile = File(databasePath)
        if (databaseFile.exists()) {
            databaseFile.delete()
            Toast.makeText(this, "Database deleted successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Database not found", Toast.LENGTH_SHORT).show()
        }
    }
}