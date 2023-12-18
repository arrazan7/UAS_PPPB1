package com.example.uas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.uas.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var prefManager: PrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi prefManager sebelum digunakan
        prefManager = PrefManager.getInstance(this@MainActivity)
        intentToHomeActivity(prefManager.getRole())

        with(binding){
            viewPager.adapter = TabAdapter(this@MainActivity)
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = when (position) {
                    0 -> "Register"
                    1 -> "Login"
                    else -> ""
                }
            }.attach()
        }
    }

    fun intentToHomeActivity(role: String) {
        val isLoggedIn = prefManager.isLoggedIn()
        if (isLoggedIn) {
            if (role == "user") {
                startActivity(Intent(this@MainActivity, HomeUserActivity::class.java))
                finish()
            } else if (role == "admin") {
                startActivity(Intent(this@MainActivity, HomeAdminActivity::class.java))
                finish()
            }
        }
    }
}