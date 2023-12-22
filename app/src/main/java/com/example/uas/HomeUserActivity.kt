package com.example.uas

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.uas.databinding.ActivityHomeUserBinding

class HomeUserActivity : AppCompatActivity() {
    private lateinit var binding:ActivityHomeUserBinding
    private lateinit var prefManager: PrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi prefManager sebelum digunakan
        prefManager = PrefManager.getInstance(this@HomeUserActivity)

        replaceFragment(HomeFragment())

        with(binding){
            bottomNavView.setOnItemSelectedListener {
                when(it.itemId) {
                    R.id.nav_home -> replaceFragment(HomeFragment())
                    R.id.nav_fav -> replaceFragment(FavoriteFragment())
                    R.id.nav_prof -> replaceFragment(ProfileFragment())

                    else -> {}
                }
                true
            }
        }
    }

    private fun replaceFragment(fragment : Fragment) {
        val fragmentManager = supportFragmentManager
        var fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }

    fun logOut() {
        prefManager.setLoggedIn(false)
        prefManager.clear()
        startActivity(Intent(this@HomeUserActivity, MainActivity::class.java))
        finish()
    }
}