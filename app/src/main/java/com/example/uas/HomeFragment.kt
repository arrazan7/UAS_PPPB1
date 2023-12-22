package com.example.uas

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.uas.databinding.FragmentHomeBinding
import com.example.uas.roomDatabase.MovieDao
import com.example.uas.roomDatabase.MovieRoom
import com.example.uas.roomDatabase.MovieRoomAdapter
import com.example.uas.roomDatabase.MovieRoomDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentHomeBinding
    private lateinit var homeUserActivity: HomeUserActivity
    private lateinit var movieAdapter: MovieAdapter
    private lateinit var movieRoomAdapter: MovieRoomAdapter
    private lateinit var mMovieDao: MovieDao
    private lateinit var executorService: ExecutorService
    private val movieCollectionRef = FirebaseFirestore.getInstance().collection("Movies")
    private var movieList = ArrayList<MovieData>()
    private var movieRoomList = ArrayList<MovieRoom>()

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
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val EXTRA_ID = "extra_id"
        const val EXTRA_IMAGE = "extra_image"
        const val EXTRA_NAMA = "extra_nama"
        const val EXTRA_DIREKTOR = "extra_direktur"
        const val EXTRA_RATING = "extra_rating"
        const val EXTRA_STORY = "ektra_story"
        const val EXTRA_GENRE = "extra_genre"

        const val EXTRA_ROOM_ID = "extra_room_id"
        const val EXTRA_ROOM_IMAGE = "extra_room_image"
        const val EXTRA_ROOM_NAMA = "extra_room_nama"
        const val EXTRA_ROOM_DIREKTOR = "extra_room_direktur"
        const val EXTRA_ROOM_STORY = "ektra_room_story"
        const val EXTRA_ROOM_RATING = "ektra_room_rating"
        const val EXTRA_ROOM_GENRE = "extra_room_genre"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Inisialisasi homeUserActivity saat Fragment terhubung dengan Activity
        if (context is HomeUserActivity) {
            homeUserActivity = context
        } else {
            throw IllegalStateException("Activity harus merupakan instance dari MainActivity")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        executorService = Executors.newSingleThreadExecutor()
        val movieRoomDb = MovieRoomDatabase.getDatabase(requireContext())
        mMovieDao = movieRoomDb!!.movieDao()!!

        initVars()

        if (mMovieDao.isNetworkAvailable(requireContext())) {
            // Kode untuk aksi yang akan diambil jika ada koneksi internet
            Toast.makeText(activity, "Koneksi internet tersedia.", Toast.LENGTH_SHORT).show()
            movieAdapter = MovieAdapter(movieList)
            binding.layoutRV.adapter = movieAdapter
            getImages()

            movieAdapter.onItemClick = {
                startActivity(Intent(activity, MovieDetailsActivity::class.java).apply {
                    putExtra(EXTRA_ID, it.id)
                    putExtra(EXTRA_IMAGE, it.gambar)
                    putExtra(EXTRA_NAMA, it.nama)
                    putExtra(EXTRA_DIREKTOR, it.direktor)
                    putExtra(EXTRA_STORY, it.storyline)
                    putExtra(EXTRA_RATING, it.rating.toString())
                    putExtra(EXTRA_GENRE, it.genre.toTypedArray())
                })
            }
        }
        else {
            // Kode untuk aksi yang akan diambil jika tidak ada koneksi internet
            Toast.makeText(activity, "Tidak ada koneksi internet.", Toast.LENGTH_SHORT).show()
            movieRoomAdapter = MovieRoomAdapter(movieRoomList)
            binding.layoutRV.adapter = movieRoomAdapter
            getImagesRoom()

            movieRoomAdapter.onItemClick = {
                startActivity(Intent(activity, MovieDetailsActivity::class.java).apply {
                    putExtra(EXTRA_ROOM_ID, it.id)
                    putExtra(EXTRA_ROOM_IMAGE, it.gambar)
                    putExtra(EXTRA_ROOM_NAMA, it.nama)
                    putExtra(EXTRA_ROOM_DIREKTOR, it.direktor)
                    putExtra(EXTRA_ROOM_STORY, it.storyline)
                    putExtra(EXTRA_ROOM_RATING, it.rating)
                    putExtra(EXTRA_ROOM_GENRE, it.genre)
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
        movieCollectionRef.get().addOnSuccessListener { querySnapshot ->
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
    }

    private fun getImagesRoom() {
        mMovieDao.allMovies.observe(requireActivity()) {
            movieRoomList.clear()
            movieRoomList.addAll(it)
            movieRoomAdapter.notifyDataSetChanged()

            Log.d("HomeAdminActivity", "Number of movie: ${it.size}")
        }
    }
}