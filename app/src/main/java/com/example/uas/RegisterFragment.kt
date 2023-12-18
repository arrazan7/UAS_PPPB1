package com.example.uas

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import com.example.uas.databinding.FragmentRegisterBinding
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RegisterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegisterFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentRegisterBinding
    private val usersCollectionRef = FirebaseFirestore.getInstance().collection("Users")

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
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RegisterFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RegisterFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRegisterBinding.bind(view)

        with(binding) {
            btnRegister.setOnClickListener {
                val savedUsername = regisUsername.text.toString().lowercase()
                val savedEmail = regisEmail.text.toString()
                val savedPassword = regisPassword.text.toString()
                val savedConfirm = regisPassword2.text.toString()

                // Memastikan semua kolom input terisi
                if (savedUsername.isNotEmpty() && savedEmail.isNotEmpty() && savedPassword.isNotEmpty() && savedConfirm.isNotEmpty()) {
                    // Memastikan password dan confirm password sama
                    if (savedPassword == savedConfirm) {
                        if (checkbox.isChecked) {
                            // Checkbox dicentang
                            val newUser = Users (
                                username = savedUsername,
                                email = savedEmail,
                                password = savedPassword,
                                role = "user"
                            )
                            addUser(newUser)
                        }
                        else {
                            // Checkbox tidak dicentang
                            Toast.makeText(requireContext(), "Checkbox harus dicentang", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else {
                        Toast.makeText(requireContext(), "Password tidak sama", Toast.LENGTH_SHORT).show()
                    }
                }
                else {
                    Toast.makeText(requireContext(), "Kolom kosong tidak diizinkan", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun addUser(user: Users) {
        usersCollectionRef.document(user.username).get()
            .addOnSuccessListener { document ->
                if (document.getString("username") == null) {
                    usersCollectionRef.document(user.username).set(user)
                    resetForm()
                    Toast.makeText(activity, "Berhasil menyimpan data User", Toast.LENGTH_SHORT).show()
                }
                else {
                    // Memastikan username tidak sama dengan user lain
                    Toast.makeText(activity, "Username ${document?.getString("username")} sudah digunakan", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.d("RegisterFragment", "Tidak ada koneksi internet", exception)
                Toast.makeText(activity, "Tidak ada koneksi internet", Toast.LENGTH_SHORT).show()
            }
    }

    private fun resetForm() {
        with(binding){
            regisUsername.setText("")
            regisEmail.setText("")
            regisPassword.setText("")
            regisPassword2.setText("")
        }
    }
}