package com.example.uas

import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.squareup.picasso.Picasso

class NotificationClass private constructor(context: Context) {
    private val channelId = "NOTIF_UAS"
    private val notifId = 90
    companion object {
        @Volatile
        private var instance: NotificationClass? = null
        fun getInstance(context: Context): NotificationClass {
            return instance ?: synchronized(this) {
                instance ?: NotificationClass(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    fun showNotif(context: Context, title: String, text:String, notifManager: NotificationManager) {
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setContentTitle(title)
            .setContentText(text) // Isi pesan notif bebas
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val notifChannel = NotificationChannel(
//                channelId, // Id channel
//                "Notifku", // Nama channel notifikasi
//                NotificationManager.IMPORTANCE_DEFAULT
//            )
            with(notifManager) {
//                createNotificationChannel(notifChannel)
                notify(0, builder.build())
            }
        }
        else {
            notifManager.notify(0, builder.build())
        }
    }

    fun showNotifImage(context: Context, title: String, text: String, gambar: String, notifManager: NotificationManager) {
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Menggunakan Picasso untuk memuat gambar dari URL Firebase
        val target = object : com.squareup.picasso.Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                // Mengatur gambar yang diunduh ke pemberitahuan
                builder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap))
                notifManager.notify(notifId, builder.build())
                Log.d("Picasso", "Bitmap loaded successfully")
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                // Menangani kegagalan memuat gambar
                Log.e("Picasso", "Bitmap loading failed: ${e?.message}")
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                // Persiapan sebelum memuat gambar
                Log.d("Picasso", "Preparing to load image")
            }
        }

        // Memuat gambar dari URL menggunakan Picasso
        Picasso.get().load(gambar).into(target)
    }
}