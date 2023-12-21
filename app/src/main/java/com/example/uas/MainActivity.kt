package com.example.uas

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.NotificationCompat
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
                // Membuat Notifikasi
                val channelId = "NOTIF_LOGIN_USER"
                val builder = NotificationCompat.Builder(this@MainActivity, channelId)
                    .setSmallIcon(R.drawable.baseline_notifications_24)
                    .setContentTitle("Berhasil Login")
                    .setContentText("Selamat data ${prefManager.getUsername()} !!!. Anda berhasil login sebagai User") // Isi pesan bebas
                    .setAutoCancel(true)
                val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    val notifChannel = NotificationChannel(
//                        channelId, // Id channel
//                        "Notifku", // Nama channel notifikasi
//                        NotificationManager.IMPORTANCE_DEFAULT
//                    )
                    with(notifManager) {
//                        createNotificationChannel(notifChannel)
                        notify(0, builder.build())
                    }
                }
                else {
                    notifManager.notify(0, builder.build())
                }

                // Beralih Activity
                startActivity(Intent(this@MainActivity, HomeUserActivity::class.java))
                finish()
            } else if (role == "admin") {
                // Membuat Notifikasi
                val channelId = "NOTIF_LOGIN_ADMIN"
                val builder = NotificationCompat.Builder(this@MainActivity, channelId)
                    .setSmallIcon(R.drawable.baseline_notifications_24)
                    .setContentTitle("Berhasil Login")
                    .setContentText("Selamat data ${prefManager.getUsername()} !!!. Anda berhasil login sebagai Admin") // Isi pesan bebas
                    .setAutoCancel(true)
                val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    val notifChannel = NotificationChannel(
//                        channelId, // Id channel
//                        "Notifku", // Nama channel notifikasi
//                        NotificationManager.IMPORTANCE_DEFAULT
//                    )
                    with(notifManager) {
//                        createNotificationChannel(notifChannel)
                        notify(1, builder.build())
                    }
                }
                else {
                    notifManager.notify(1, builder.build())
                }

                // Beralih Activity
                startActivity(Intent(this@MainActivity, HomeAdminActivity::class.java))
                finish()
            }
        }
    }
}