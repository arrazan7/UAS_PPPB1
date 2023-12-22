package com.example.uas.roomDatabase

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface MovieDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(movie: MovieRoom)

    @Update
    fun update(movie: MovieRoom)

    @Delete
    fun delete(movie: MovieRoom)

    @get:Query("SELECT * FROM movie_table ORDER BY id ASC")
    val allMovies: LiveData<List<MovieRoom>>

    @Query("SELECT gambar FROM movie_table WHERE id = :documentID")
    fun getGambarById(documentID: String): LiveData<String?>

    fun fromListToString(listGenre: List<String>?): String? {
        return listGenre?.joinToString(",")
    }

    fun fromStringToList(stringGenre: String?): List<String>? {
        return stringGenre?.split(",")?.map { it.trim() }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo

        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}