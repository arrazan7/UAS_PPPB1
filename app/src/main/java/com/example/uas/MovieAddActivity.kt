package com.example.uas

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.uas.databinding.ActivityMovieAddBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MovieAddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMovieAddBinding
    private val imageCollectionRef = FirebaseStorage.getInstance().reference.child("Images")
    private val movieCollectionRef = FirebaseFirestore.getInstance().collection("Movies")
    private var imageUri: Uri? = null
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
        imageUri = it
        binding.addPosterMovie.setImageURI(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            btnBack.setOnClickListener {
                startActivity(Intent(this@MovieAddActivity, HomeAdminActivity::class.java))
                finish()
            }

            btnAddMovie.setOnClickListener {
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

                            movieCollectionRef.add(mapDocument).addOnCompleteListener { firestoreTask ->
                                if (firestoreTask.isSuccessful) {
                                    Toast.makeText(this@MovieAddActivity, "Uploaded Successfully", Toast.LENGTH_SHORT).show()
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
}