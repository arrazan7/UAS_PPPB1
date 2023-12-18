package com.example.uas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.uas.databinding.ActivityHomeAdminBinding

class HomeAdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeAdminBinding
    private lateinit var prefManager: PrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi prefManager sebelum digunakan
        prefManager = PrefManager.getInstance(this@HomeAdminActivity)

        replaceFragment(HomeAdminFragment())

        with(binding){
            bottomNavView.setOnItemSelectedListener {
                when(it.itemId) {
                    R.id.nav_home_admin -> replaceFragment(HomeAdminFragment())
                    R.id.nav_reg -> replaceFragment(RegisterAdminFragment())
                    R.id.nav_prof_admin -> replaceFragment(ProfileAdminFragment())

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
        startActivity(Intent(this@HomeAdminActivity, MainActivity::class.java))
        finish()
    }

    fun intentToMovieAddActivity() {
        startActivity(Intent(this@HomeAdminActivity, MovieAddActivity::class.java))
        finish()
    }
}