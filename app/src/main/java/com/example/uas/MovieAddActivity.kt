package com.example.uas

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import com.example.uas.databinding.ActivityMovieAddBinding
import com.example.uas.roomDatabase.MovieDao
import com.example.uas.roomDatabase.MovieRoom
import com.example.uas.roomDatabase.MovieRoomDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MovieAddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMovieAddBinding
    private lateinit var mMovieDao: MovieDao
    private lateinit var executorService: ExecutorService
    private val imageCollectionRef = FirebaseStorage.getInstance().reference.child("Images")
    private val movieCollectionRef = FirebaseFirestore.getInstance().collection("Movies")
    private var imageUri: Uri? = null
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
        imageUri = it
        binding.addPosterMovie.setImageURI(it)
    }
    private val selectedGenres = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        executorService = Executors.newSingleThreadExecutor()
        val movieRoomDb = MovieRoomDatabase.getDatabase(this@MovieAddActivity)
        mMovieDao = movieRoomDb!!.movieDao()!!

        with(binding) {
            btnBack.setOnClickListener {
                startActivity(Intent(this@MovieAddActivity, HomeAdminActivity::class.java))
                finish()
            }

            btnAddMovie.setOnClickListener {
                if (gHorror.isChecked) selectedGenres.add("Horror")
                if (gThriller.isChecked) selectedGenres.add("Thriller")
                if (gAnimasi.isChecked) selectedGenres.add("Animasi")
                if (gDokumenter.isChecked) selectedGenres.add("Dokumenter")
                if (gKomedi.isChecked) selectedGenres.add("Komedi")
                if (gAksi.isChecked) selectedGenres.add("Aksi")
                if (gDrama.isChecked) selectedGenres.add("Drama")
                if (gRomantis.isChecked) selectedGenres.add("Romantis")
                if (gMisteri.isChecked) selectedGenres.add("Misteri")
                if (gKeluarga.isChecked) selectedGenres.add("Keluarga")
                if (gFiksiilmiah.isChecked) selectedGenres.add("Fiksi Ilmiah")
                if (gPetualangan.isChecked) selectedGenres.add("Petualangan")
                if (gFantasi.isChecked) selectedGenres.add("Fantasi")
                if (gMusikal.isChecked) selectedGenres.add("Musikal")
                if (gPersahabatan.isChecked) selectedGenres.add("Persahabatan")
                if (gBiografi.isChecked) selectedGenres.add("Biografi")
                if (gNoir.isChecked) selectedGenres.add("Noir")
                if (gDewasa.isChecked) selectedGenres.add("Dewasa")

                // MENAMBAH DATA MOVIE KE FIREBASE
                val takeImage = imageCollectionRef.child(System.currentTimeMillis().toString()) // Memberikan nama unik pada gambar yang diupload berupa angka waktu
                imageUri?.let { takeImage.putFile(it).addOnCompleteListener { task -> // Menyimpan gambar pada Firebase Storage
                    if (task.isSuccessful) { // Jika gambar sudah berhasil disimpan pada Firebase Storage, maka ...
                        progressBar.visibility = View.VISIBLE
                        takeImage.downloadUrl.addOnSuccessListener { uri -> // Mengambil link URL gambar untuk alamat akses
                            val mapDocument = HashMap<String, Any>() // Membuat kode acak untuk nama document

                            mapDocument["gambar"] = uri.toString()
                            mapDocument["nama"] = inputMovieTitle.text.toString()
                            mapDocument["rating"] = ratingSlider.value.toDouble() // Mengambil nilai dari Slider dan mengubahnya menjadi Double
                            mapDocument["direktor"] = inputMovieDir.text.toString()
                            mapDocument["genre"] = selectedGenres
                            mapDocument["storyline"] = inputStoryLine.text.toString()

                            movieCollectionRef.add(mapDocument).addOnCompleteListener { firestoreTask ->
                                if (firestoreTask.isSuccessful) {
                                    // Memperoleh ID dokumen yang dihasilkan oleh Firestore
                                    val newDocumentId = firestoreTask.result?.id
                                    // Menyimpan gambar ke penyimpanan lokal
                                    val fileName = System.currentTimeMillis().toString() + ".jpg"
                                    val filePath = applicationContext.filesDir.absolutePath + File.separator + fileName
                                    saveImageLocally(imageUri!!, filePath)
                                    Log.d("Save Image with Room", "$filePath")
                                    Log.d("Save Image with Room", "$fileName")

                                    // MENAMBAH DATA MOVIE KE ROOM
                                    insertRoom(
                                        MovieRoom(
                                            id = newDocumentId.toString(),
                                            gambar = filePath, // Menggunakan jalur file lokal
                                            nama = inputMovieTitle.text.toString(),
                                            rating = ratingSlider.value.toInt(),
                                            direktor = inputMovieDir.text.toString(),
                                            genre = mMovieDao.fromListToString(selectedGenres).toString(),
                                            storyline = inputStoryLine.text.toString()
                                        )
                                    )


                                    Toast.makeText(this@MovieAddActivity, "Uploaded Successfully", Toast.LENGTH_SHORT).show()

                                    // Membuat Notifikasi
                                    val channelId = "NOTIF_ADD_MOVIE"
                                    val notifId = 90
                                    val builder = NotificationCompat.Builder(this@MovieAddActivity, channelId)
                                        .setSmallIcon(R.drawable.baseline_notifications_24)
                                        .setContentTitle("Add Movie")
                                        .setContentText("Anda berhasil menambah film ${inputMovieTitle.text.toString()}")
                                        .setAutoCancel(true)
                                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                    val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                                    // Menggunakan Picasso untuk memuat gambar dari URL Firebase
                                    val target = object : com.squareup.picasso.Target {
                                        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                                            // Mengatur gambar yang diunduh ke pemberitahuan
                                            builder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap))
                                            notifManager.notify(notifId, builder.build())
                                            Log.d("Picasso", "Bitmap loaded successfully")
                                        }

                                        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                                            // Menangani kegagalan memuat gambar
                                            Log.e("Picasso", "Bitmap loading failed: ${e?.message}")
                                        }

                                        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                                            // Persiapan sebelum memuat gambar
                                            Log.d("Picasso", "Preparing to load image")
                                        }
                                    }

                                    // Memuat gambar dari URL menggunakan Picasso
                                    Picasso.get().load(uri.toString()).into(target)
                                }
                                else {
                                    Toast.makeText(this@MovieAddActivity, firestoreTask.exception?.message, Toast.LENGTH_SHORT).show()
                                }
                                resetForm()
                                addPosterMovie.setImageResource(R.drawable.upload_image)
                            }
                        }
                        progressBar.visibility = View.GONE
                    }
                    else {
                        Toast.makeText(this@MovieAddActivity, task.exception?.message, Toast.LENGTH_SHORT).show()
                        addPosterMovie.setImageResource(R.drawable.upload_image)
                    }
                } }

                startActivity(Intent(this@MovieAddActivity, HomeAdminActivity::class.java))
            }

            addPosterMovie.setOnClickListener {
                resultLauncher.launch("image/*")
            }
        }
    }

    override fun onBackPressed() {
        // Tambahkan logika yang diperlukan sebelum intent ke HomeAdminActivity
        // Contoh: Simpan data, lakukan validasi, dll.

        // Intent ke HomeAdminActivity
        startActivity(Intent(this@MovieAddActivity, HomeAdminActivity::class.java))

        // Panggil super.onBackPressed() jika ingin menjalankan perilaku default (tutup aktivitas saat tombol back ditekan)
        super.onBackPressed()
    }

    private fun resetForm() {
        with(binding){
            inputMovieTitle.setText("")
            inputMovieDir.setText("")
            inputStoryLine.setText("") // Perlu ditambahkan checkbox dan slider nantinya
        }
    }

    // Fungsi untuk menambah movie data ke Room
    private fun insertRoom(movie: MovieRoom) {
        executorService.execute {mMovieDao.insert(movie)}
    }

    // Fungsi untuk menyimpan gambar secara lokal
    private fun saveImageLocally(uri: Uri, filePath: String) {
        val inputStream = contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(filePath)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
    }
}