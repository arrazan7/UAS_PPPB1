package com.example.uas

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import androidx.room.InvalidationTracker
import com.example.uas.databinding.ActivityMovieEditBinding
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

class MovieEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMovieEditBinding
    private lateinit var mMovieDao: MovieDao
    private lateinit var executorService: ExecutorService
    private val imageCollectionRef = FirebaseStorage.getInstance().reference.child("Images")
    private val movieCollectionRef = FirebaseFirestore.getInstance().collection("Movies")
    private var imageUri: Uri? = null
    private var gambarURL = ""
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
        imageUri = it
        binding.editPosterMovie.setImageURI(it)
    }
    private val selectedGenres = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        executorService = Executors.newSingleThreadExecutor()
        val movieRoomDb = MovieRoomDatabase.getDatabase(this@MovieEditActivity)
        mMovieDao = movieRoomDb!!.movieDao()!!

        with(binding) {
            if (mMovieDao.isNetworkAvailable(this@MovieEditActivity)) {
                val id = intent.getStringExtra(HomeAdminFragment.EXTRA_ID)!!
                gambarURL = intent.getStringExtra(HomeAdminFragment.EXTRA_IMAGE)!!
                val nama = intent.getStringExtra(HomeAdminFragment.EXTRA_NAMA)
                val direktor = intent.getStringExtra(HomeAdminFragment.EXTRA_DIREKTOR)
                val story = intent.getStringExtra(HomeAdminFragment.EXTRA_STORY)
                val rate = intent.getIntExtra(HomeAdminFragment.EXTRA_RATING, 0)
                val genre = intent.getStringArrayExtra(HomeAdminFragment.EXTRA_GENRE) ?: emptyArray()

                Picasso.get().load(gambarURL).into(editPosterMovie)
                inputMovieTitle.setText(nama)
                inputMovieDir.setText(direktor)
                inputStoryLine.setText(story)
                ratingSlider.value = rate.toFloat()

                for (i in genre) {
                    if (i == "Horror") gHorror.isChecked = true
                    if (i == "Thriller") gThriller.isChecked = true
                    if (i == "Animasi") gAnimasi.isChecked = true
                    if (i == "Dokumenter") gDokumenter.isChecked = true
                    if (i == "Komedi") gKomedi.isChecked = true
                    if (i == "Aksi") gAksi.isChecked = true
                    if (i == "Drama") gDrama.isChecked = true
                    if (i == "Romantis") gRomantis.isChecked = true
                    if (i == "Misteri") gMisteri.isChecked = true
                    if (i == "Keluarga") gKeluarga.isChecked = true
                    if (i == "Fiksi Ilmiah") gFiksiilmiah.isChecked = true
                    if (i == "Petualangan") gPetualangan.isChecked = true
                    if (i == "Fantasi") gFantasi.isChecked = true
                    if (i == "Musikal") gMusikal.isChecked = true
                    if (i == "Persahabatan") gPersahabatan.isChecked = true
                    if (i == "Biografi") gBiografi.isChecked = true
                    if (i == "Noir") gNoir.isChecked = true
                    if (i == "Dewasa") gDewasa.isChecked = true
                }

                btnSubmitMovie.setOnClickListener {
                    // Memperbarui movie pada Firebasefirestore dan Room dengan fungsi
                    addMovieToFirebase(id)

                    startActivity(Intent(this@MovieEditActivity, HomeAdminActivity::class.java))
                }

                btnDeleteMovie.setOnClickListener {
                    // Menghapus movie pada Firebasefirestore
                    deleteImageFromStorage(gambarURL)
                    val movieID = id?.let { it1 -> MovieData(id = it1, "","",null, "", listOf(""), "") }
                    if (movieID != null) {
                        deleteMovieFromFirebase(movieID)
                    }

                    // Menghapus movie pada Room
                    val movieToDelete = MovieRoom(id = id, "", "", 0, "", "", "")
                    deleteRoom(movieToDelete)

                    // Membuat Notifikasi
                    showNotifUpdateMovie(91, "Edit Movie", "Berhasil menghapus film $nama", gambarURL)

                    startActivity(Intent(this@MovieEditActivity, HomeAdminActivity::class.java))
                }
            }


            else {
                val id_room = intent.getStringExtra(HomeAdminFragment.EXTRA_ROOM_ID)
                val gambar_room = intent.getStringExtra(HomeAdminFragment.EXTRA_ROOM_IMAGE)
                val nama_room = intent.getStringExtra(HomeAdminFragment.EXTRA_ROOM_NAMA)
                val direktor_room = intent.getStringExtra(HomeAdminFragment.EXTRA_ROOM_DIREKTOR)
                val story_room = intent.getStringExtra(HomeAdminFragment.EXTRA_ROOM_STORY)
                val rate_room = intent.getIntExtra(HomeAdminFragment.EXTRA_ROOM_RATING, 0)
                val genre_room = intent.getStringExtra(HomeAdminFragment.EXTRA_ROOM_GENRE)
                val genre_room_list = mMovieDao.fromStringToList(genre_room)

                tvEditmovie.setOnClickListener {
                    Toast.makeText(this@MovieEditActivity, "$id_room $nama_room $direktor_room $rate_room $gambar_room", Toast.LENGTH_SHORT).show()
                }
                // Mendapatkan jalur gambar dari Room Database
                val imagePath = gambar_room

                // Memuat gambar dari jalur file ke dalam ShapeableImageView
                val imageFile = File(imagePath)
                if (imageFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                    editPosterMovie.setImageBitmap(bitmap)
                } else {
                    // Gambar tidak ditemukan, mungkin hendak menampilkan gambar placeholder di sini
                    editPosterMovie.setImageResource(R.drawable.upload_image)
                }
                inputMovieTitle.setText(nama_room)
                inputMovieDir.setText(direktor_room)
                inputStoryLine.setText(story_room)
                ratingSlider.value = rate_room.toFloat()

                if (genre_room_list != null) {
                    for (i in genre_room_list) {
                        if (i == "Horror") gHorror.isChecked = true
                        if (i == "Thriller") gThriller.isChecked = true
                        if (i == "Animasi") gAnimasi.isChecked = true
                        if (i == "Dokumenter") gDokumenter.isChecked = true
                        if (i == "Komedi") gKomedi.isChecked = true
                        if (i == "Aksi") gAksi.isChecked = true
                        if (i == "Drama") gDrama.isChecked = true
                        if (i == "Romantis") gRomantis.isChecked = true
                        if (i == "Misteri") gMisteri.isChecked = true
                        if (i == "Keluarga") gKeluarga.isChecked = true
                        if (i == "Fiksi Ilmiah") gFiksiilmiah.isChecked = true
                        if (i == "Petualangan") gPetualangan.isChecked = true
                        if (i == "Fantasi") gFantasi.isChecked = true
                        if (i == "Musikal") gMusikal.isChecked = true
                        if (i == "Persahabatan") gPersahabatan.isChecked = true
                        if (i == "Biografi") gBiografi.isChecked = true
                        if (i == "Noir") gNoir.isChecked = true
                        if (i == "Dewasa") gDewasa.isChecked = true
                    }
                }

                btnSubmitMovie.setOnClickListener {
                    Toast.makeText(this@MovieEditActivity, "Tidak ada koneksi internet", Toast.LENGTH_SHORT).show()
                }
                btnDeleteMovie.setOnClickListener {
                    Toast.makeText(this@MovieEditActivity, "Tidak ada koneksi internet", Toast.LENGTH_SHORT).show()
                }
            }

            editPosterMovie.setOnClickListener {
                resultLauncher.launch("image/*")
            }

            btnBack.setOnClickListener {
                startActivity(Intent(this@MovieEditActivity, HomeAdminActivity::class.java))
                finish()
            }
        }
    }

    override fun onBackPressed() {
        // Tambahkan logika yang diperlukan sebelum intent ke HomeAdminActivity
        // Contoh: Simpan data, lakukan validasi, dll.

        // Intent ke HomeAdminActivity
        startActivity(Intent(this@MovieEditActivity, HomeAdminActivity::class.java))

        // Panggil super.onBackPressed() jika ingin menjalankan perilaku default (tutup aktivitas saat tombol back ditekan)
        super.onBackPressed()
    }

    // Fungsi untuk menghapus gambar dari Firebase Storage
    private fun deleteImageFromStorage(imageUrl: String) {
        // Jika URL gambar kosong, keluar dari fungsi
        if (imageUrl.isEmpty()) {
            return
        }

        // Dapatkan referensi ke Firebase Storage
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)

        // Hapus gambar dari Firebase Storage
        storageReference.delete().addOnSuccessListener {
            // Hapus berhasil
            Toast.makeText(this@MovieEditActivity, "Image Deleted Successfully", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            // Gagal menghapus
            Toast.makeText(this@MovieEditActivity, "Error Deleting Image: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addMovieToFirebase(documentID: String) {
        with(binding) {
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

            // Memeriksa jika movie diupdate tanpa mengubah gambar
            if (imageUri == null) withOutImageUri(documentID)

            // Memeriksa jika movie diupdate dengan mengubah gambar
            if (imageUri != null) {
                deleteImageFromStorage(gambarURL)
                withImageUri(documentID)
            }
        }
    }

    private fun deleteMovieFromFirebase(movieData: MovieData) {
        if (movieData.id.isEmpty()) {
            Log.d("MovieEditActivity", "Error delete data empty ID", return)
        }

        movieCollectionRef.document(movieData.id).delete()
            .addOnFailureListener{
                Log.d("MovieEditActivity", "Error delete data movie")
            }
    }

    private fun withImageUri(documentID: String) {
        with(binding) {
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

                        // Menggunakan nilai id sebagai nama dokumen
                        movieCollectionRef.document(documentID).set(mapDocument).addOnCompleteListener { firestoreTask ->
                            if (firestoreTask.isSuccessful) {
                                Toast.makeText(this@MovieEditActivity, "Movie Uploaded Successfully", Toast.LENGTH_SHORT).show()

                                // Membuat Notifikasi
                                showNotifUpdateMovie(91, "Edit Movie", "Berhasil mengedit film ${inputMovieTitle.text.toString()}", uri.toString())
                            } else {
                                Toast.makeText(this@MovieEditActivity, firestoreTask.exception?.message, Toast.LENGTH_SHORT).show()
                            }
                        }

                        // Mengupdate data movie pada Room
                        val gambar_room_lama = mMovieDao.getGambarById(documentID)
                        gambar_room_lama.observe(this@MovieEditActivity,
                            Observer { alamat ->
                                // Menghapus gambar dari penyimpanan lokal
                                deleteImageLocally(alamat ?: "")
                                Log.d("Delete Image with Room", "$alamat")
                            })

                        val fileName = System.currentTimeMillis().toString() + ".jpg"
                        val filePath = applicationContext.filesDir.absolutePath + File.separator + fileName
                        saveImageLocally(imageUri!!, filePath)
                        Log.d("Save Image with Room", "$filePath")
                        Log.d("Save Image with Room", "$fileName")
                        Log.d("Save Image with Room", "$gambar_room_lama")
                        updateRoom(
                            MovieRoom(
                                id = documentID,
                                gambar = filePath,
                                nama = inputMovieTitle.text.toString(),
                                rating = ratingSlider.value.toInt(),
                                direktor = inputMovieDir.text.toString(),
                                genre = mMovieDao.fromListToString(selectedGenres).toString(),
                                storyline = inputStoryLine.text.toString()
                            )
                        )
                    }
                    progressBar.visibility = View.GONE
                }
                else {
                    Toast.makeText(this@MovieEditActivity, task.exception?.message, Toast.LENGTH_SHORT).show()
                    editPosterMovie.setImageResource(R.drawable.upload_image)
                }
            } }
        }
    }

    private fun withOutImageUri(documentID: String) {
        with(binding) {
            val mapDocument = HashMap<String, Any>() // Membuat kode acak untuk nama document

            mapDocument["gambar"] = gambarURL
            mapDocument["nama"] = inputMovieTitle.text.toString()
            mapDocument["rating"] = ratingSlider.value.toDouble() // Mengambil nilai dari Slider dan mengubahnya menjadi Double
            mapDocument["direktor"] = inputMovieDir.text.toString()
            mapDocument["genre"] = selectedGenres
            mapDocument["storyline"] = inputStoryLine.text.toString()

            // Menggunakan nilai id sebagai nama dokumen
            movieCollectionRef.document(documentID).set(mapDocument).addOnCompleteListener { firestoreTask ->
                if (firestoreTask.isSuccessful) {
                    Toast.makeText(this@MovieEditActivity, "Movie Uploaded Successfully", Toast.LENGTH_SHORT).show()

                    // Membuat Notifikasi
                    showNotifUpdateMovie(91, "Edit Movie", "Berhasil mengedit film ${inputMovieTitle.text.toString()}", gambarURL)
                } else {
                    Toast.makeText(this@MovieEditActivity, firestoreTask.exception?.message, Toast.LENGTH_SHORT).show()
                }

                // Mengupdate data movie pada Room
                val gambar_room_lama = mMovieDao.getGambarById(documentID)
                gambar_room_lama.observe(this@MovieEditActivity) { gambarPath ->
                    val gambarPathNonNull = gambarPath ?: "" // Jika gambarPath null, atur menjadi string kosong
                    updateRoom(
                        MovieRoom(
                            id = documentID,
                            gambar = gambarPathNonNull,
                            nama = inputMovieTitle.text.toString(),
                            rating = ratingSlider.value.toInt(),
                            direktor = inputMovieDir.text.toString(),
                            genre = mMovieDao.fromListToString(selectedGenres).toString(),
                            storyline = inputStoryLine.text.toString()
                        )
                    )
                }
            }
            progressBar.visibility = View.GONE
        }
    }

    private fun showNotifUpdateMovie(notifID: Int, title: String, text: String, image: String) {
        // Membuat Notifikasi
        val channelId = "NOTIF_UPDATE_MOVIE"
        val builder = NotificationCompat.Builder(this@MovieEditActivity, channelId)
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Menggunakan Picasso untuk memuat gambar dari URL Firebase
        val target = object : com.squareup.picasso.Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                // Mengatur gambar yang diunduh ke pemberitahuan
                builder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap))
                notifManager.notify(notifID, builder.build())
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
        Picasso.get().load(image).into(target)
    }

    private fun resetForm() {
        with(binding){
            inputMovieTitle.setText("")
            inputMovieDir.setText("")
            inputStoryLine.setText("") // Perlu ditambahkan checkbox dan slider nantinya
        }
    }

    private fun updateRoom(movie: MovieRoom) {
        executorService.execute {mMovieDao.update(movie)}
    }
    private fun deleteRoom(movie: MovieRoom) {
        executorService.execute {mMovieDao.delete(movie)}
    }

    private fun deleteImageLocally(filePath: String) {
        val imageFile = File(filePath)
        if (imageFile.exists()) {
            imageFile.delete()
            Log.d("MovieEditActivity", "Image deleted successfully: $filePath")
        } else {
            Log.e("MovieEditActivity", "Image not found: $filePath")
        }
    }

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