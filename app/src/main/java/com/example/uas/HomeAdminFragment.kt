package com.example.uas

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.uas.databinding.FragmentHomeAdminBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeAdminFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeAdminFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentHomeAdminBinding
    private lateinit var homeAdminActivity: HomeAdminActivity
    private lateinit var movieAdapter: MovieAdminAdapter
    private val movieCollectionRef = FirebaseFirestore.getInstance().collection("Movies")
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
        return inflater.inflate(R.layout.fragment_home_admin, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeAdminFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeAdminFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val EXTRA_ID = "extra_id"
        const val EXTRA_IMAGE = "extra_image"
        const val EXTRA_NAMA = "extra_nama"
        const val EXTRA_DIREKTOR = "extra_direktur"
        const val EXTRA_STORY = "ektra_story"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Inisialisasi mainActivity saat Fragment terhubung dengan Activity
        if (context is HomeAdminActivity) {
            homeAdminActivity = context
        } else {
            throw IllegalStateException("Activity harus merupakan instance dari HomeAdminActivity")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeAdminBinding.bind(view)

        initVars()
        getImages()

        with(binding) {
            addMovie.setOnClickListener {
                homeAdminActivity.intentToMovieAddActivity()
            }

            movieAdapter.onItemClick = {
                startActivity(Intent(activity, MovieEditActivity::class.java).apply {
                    putExtra(EXTRA_ID, it.id)
                    putExtra(EXTRA_IMAGE, it.gambar)
                    putExtra(EXTRA_NAMA, it.nama)
                    putExtra(EXTRA_DIREKTOR, it.direktor)
                    putExtra(EXTRA_STORY, it.storyline)
                })
            }
        }
    }

    private fun initVars() {
        binding.layoutRV.setHasFixedSize(true)
        binding.layoutRV.layoutManager = GridLayoutManager(activity, 2)
        movieAdapter = MovieAdminAdapter(movieList)
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
                    document.getString("genre") ?: "",
                    document.getString("storyline") ?: ""
                )
                movieList.add(movieData)
            }
            movieAdapter.notifyDataSetChanged()
        }
    }
}