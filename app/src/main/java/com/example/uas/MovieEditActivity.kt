package com.example.uas

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.uas.databinding.ActivityMovieEditBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class MovieEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMovieEditBinding
    private val imageCollectionRef = FirebaseStorage.getInstance().reference.child("Images")
    private val movieCollectionRef = FirebaseFirestore.getInstance().collection("Movies")
    private var imageUri: Uri? = null
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
        imageUri = it
        binding.editPosterMovie.setImageURI(it)
    }
    private lateinit var dokumenID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            val id = intent.getStringExtra(HomeAdminFragment.EXTRA_ID)
            val gambar = intent.getStringExtra(HomeAdminFragment.EXTRA_IMAGE)
            val nama = intent.getStringExtra(HomeAdminFragment.EXTRA_NAMA)
            val direktor = intent.getStringExtra(HomeAdminFragment.EXTRA_DIREKTOR)
            val story = intent.getStringExtra(HomeAdminFragment.EXTRA_STORY)

            Picasso.get().load(gambar).into(editPosterMovie)
            inputMovieTitle.setText(nama)
            inputMovieDir.setText(direktor)
            inputStoryLine.setText(story)

            btnSubmitMovie.setOnClickListener {
                deleteImageFromStorage(gambar ?: "")
                addMovieToFirebase(id ?: "")
            }

            btnDeleteMovie.setOnClickListener {
                deleteImageFromStorage(gambar ?: "")
                val movieID = id?.let { it1 -> MovieData(id = it1, "","",null, "", "", "") }
                if (movieID != null) {
                    deleteMovieFromFirebase(movieID)
                }
                startActivity(Intent(this@MovieEditActivity, HomeAdminActivity::class.java))
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
        // Dapatkan referensi ke Firebase Storage
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)

        // Hapus gambar dari Firebase Storage
        storageReference.delete().addOnSuccessListener {
            // Hapus berhasil
            Toast.makeText(this@MovieEditActivity, "Image Deleted Successfully", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            // Gagal menghapus
            Toast.makeText(this@MovieEditActivity, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun addMovieToFirebase(documentID: String) {
        with(binding) {
            val takeImage = imageCollectionRef.child(System.currentTimeMillis().toString()) // Memberikan nama unik pada gambar yang diupload berupa angka waktu
            imageUri?.let { takeImage.putFile(it).addOnCompleteListener { task -> // Menyimpan gambar pada Firebase Storage
                if (task.isSuccessful) { // Jika gambar sudah berhasil disimpan pada Firebase Storage, maka ...
                    progressBar.visibility = View.VISIBLE
                    takeImage.downloadUrl.addOnSuccessListener { uri -> // Mengambil link URL gambar untuk alamat akses
                        val mapDocument = HashMap<String, Any>() // Membuat kode acak untuk nama document

                        mapDocument["gambar"] = uri.toString()
                        mapDocument["nama"] = inputMovieTitle.text.toString()
                        mapDocument["rating"] = 8
                        mapDocument["direktor"] = inputMovieDir.text.toString()
                        mapDocument["genre"] = "Genre Uji Coba"
                        mapDocument["storyline"] = inputStoryLine.text.toString()

                        // Menggunakan nilai id sebagai nama dokumen
                        movieCollectionRef.document(documentID).set(mapDocument)
                            .addOnCompleteListener { firestoreTask ->
                                if (firestoreTask.isSuccessful) {
                                    Toast.makeText(this@MovieEditActivity, "Movie Uploaded Successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this@MovieEditActivity, firestoreTask.exception?.message, Toast.LENGTH_SHORT).show()
                                }
                                resetForm()
                                editPosterMovie.setImageResource(R.drawable.upload_image)
                            }
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

    private fun deleteMovieFromFirebase(movieData: MovieData) {
        if (movieData.id.isEmpty()) {
            Log.d("MovieEditActivity", "Error delete data empty ID", return)
        }

        movieCollectionRef.document(movieData.id).delete()
            .addOnFailureListener{
                Log.d("MovieEditActivity", "Error delete data movie")
            }
    }

    private fun resetForm() {
        with(binding){
            inputMovieTitle.setText("")
            inputMovieDir.setText("")
            inputStoryLine.setText("") // Perlu ditambahkan checkbox dan slider nantinya
        }
    }
}