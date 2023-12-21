package com.example.uas

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.uas.databinding.FragmentFavoriteBinding
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FavoriteFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FavoriteFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentFavoriteBinding
    private lateinit var movieAdapter: MovieAdapter
    private lateinit var prefManager: PrefManager
    private val movieCollectionRef = FirebaseFirestore.getInstance().collection("Movies")
    private val usersCollectionRef = FirebaseFirestore.getInstance().collection("Users")
    private var movieList = ArrayList<MovieData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FavoriteFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FavoriteFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFavoriteBinding.bind(view)

        // Inisialisasi prefManager saat Fragment terhubung dengan Activity
        prefManager = PrefManager.getInstance(requireContext())

        initVars()
        getImages()

        with(binding) {
            movieAdapter.onItemClick = {
                startActivity(Intent(activity, MovieDetailsActivity::class.java).apply {
                    putExtra(HomeFragment.EXTRA_ID, it.id)
                    putExtra(HomeFragment.EXTRA_IMAGE, it.gambar)
                    putExtra(HomeFragment.EXTRA_NAMA, it.nama)
                    putExtra(HomeFragment.EXTRA_DIREKTOR, it.direktor)
                    putExtra(HomeFragment.EXTRA_RATING, it.rating.toString())
                    putExtra(HomeFragment.EXTRA_STORY, it.storyline)
                    putExtra(HomeFragment.EXTRA_GENRE, it.genre.toTypedArray())
                })
            }
        }
    }

    private fun initVars() {
        binding.layoutRV.setHasFixedSize(true)
        binding.layoutRV.layoutManager = GridLayoutManager(activity, 2)
        movieAdapter = MovieAdapter(movieList)
        binding.layoutRV.adapter = movieAdapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getImages() {
        usersCollectionRef.document(prefManager.getUsername()).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val storedFavorite = document.get("favorite") as MutableList<String>? ?: mutableListOf()

                    // Dapatkan daftar ID movie dari movieCollectionRef
                    movieCollectionRef.get()
                        .addOnSuccessListener { querySnapshotMovies ->
                            // Filter ID movie pada storedFavorite
                            val invalidFavorites = storedFavorite.filter { listIdMovie ->
                                // dapatkan ID movie dari storedFavorite yang tidak dimiliki oleh movieCollectionRef.
                                // Artinya, bisa jadi ID movie tersebut yang ada pada movieCollectionRef telah dihapus, sedangkan storedFavorite masih memiliki data ID movie tersebut.
                                !querySnapshotMovies.documents.any { it.id == listIdMovie }
                            }

                            // Jika ada nilai favorit yang tidak valid(adanya list ID movie pada variabel invalidFavorite),
                            // Maka list ID movie tersebut akan dihapus dari storedFavorite karena data ID movie itu sudah tidak ada lagi do movieCollectionRef.
                            if (invalidFavorites.isNotEmpty()) {
                                storedFavorite.removeAll(invalidFavorites)

                                // Perbarui field "favorite" di dalam usersCollectionRef menggunakan fungsi updateFavorite()
                                updateFavorite(storedFavorite)
                            }

                            if (storedFavorite.isNotEmpty()) {
                            // Ambil data-data movie berdasarkan storedFavorite yang telah difilter dan update sebelumnya.
                                movieCollectionRef.whereIn(FieldPath.documentId(), storedFavorite).get()
                                    .addOnSuccessListener { querySnapshot ->
                                        movieList.clear()
                                        for (document in querySnapshot.documents) {
                                            val movieData = MovieData(
                                                document.id,
                                                document.getString("gambar") ?: "",
                                                document.getString("nama") ?: "",
                                                document.getLong("rating")?.toInt() ?: 0,
                                                document.getString("direktor") ?: "",
                                                document.get("genre") as List<String>? ?: listOf(),
                                                document.getString("storyline") ?: ""
                                            )
                                            movieList.add(movieData)
                                        }
                                        movieAdapter.notifyDataSetChanged()
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.d("FavoriteFragment", "Error fetching movie data", exception)
                                        Toast.makeText(activity, "Error fetching movie data", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.d("FavoriteFragment", "Error fetching movie collection data", exception)
                            Toast.makeText(activity, "Error fetching movie collection data", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("FavoriteFragment", "Tidak ada koneksi internet", exception)
                Toast.makeText(activity, "Tidak ada koneksi internet", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateFavorite(updatedFavorite: List<String>) {
        val mapDocument = HashMap<String, Any>()
        mapDocument["favorite"] = updatedFavorite

        usersCollectionRef.document(prefManager.getUsername()).update(mapDocument)
            .addOnSuccessListener {
                Log.d("FavoriteFragment", "Updated favorite successfully")
            }
            .addOnFailureListener { exception ->
                Log.d("FavoriteFragment", "Error updating favorite", exception)
                Toast.makeText(activity, "Error updating favorite", Toast.LENGTH_SHORT).show()
            }
    }
}