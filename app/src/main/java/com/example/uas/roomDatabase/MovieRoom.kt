package com.example.uas.roomDatabase

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movie_table")
data class MovieRoom(
    @PrimaryKey(autoGenerate = false)
    @NonNull
    val id: String,
    @ColumnInfo(name = "gambar")
    val gambar: String,
    @ColumnInfo(name = "nama")
    val nama: String,
    @ColumnInfo(name = "rating")
    val rating: Int,
    @ColumnInfo(name = "direktor")
    val direktor: String,
    @ColumnInfo(name = "genre")
    val genre: String,
    @ColumnInfo(name = "storyline")
    val storyline: String
)
