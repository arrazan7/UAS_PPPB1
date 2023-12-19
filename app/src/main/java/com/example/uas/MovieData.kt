package com.example.uas

import android.os.Parcel
import android.os.Parcelable

data class MovieData(
    val id: String,
    val gambar: String,
    val nama: String,
    val rating: Int?,
    val direktor: String,
    val genre: List<String>,
    val storyline: String
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readInt(),
            parcel.readString()!!,
            parcel.createStringArrayList()!!,
            parcel.readString()!!
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(id)
            parcel.writeString(gambar)
            parcel.writeString(nama)
            parcel.writeInt(rating!!)
            parcel.writeString(direktor)
            parcel.writeStringList(genre)
            parcel.writeString(storyline)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<MovieData> {
            override fun createFromParcel(parcel: Parcel): MovieData {
                return MovieData(parcel)
            }

            override fun newArray(size: Int): Array<MovieData?> {
                return arrayOfNulls(size)
            }
        }

    }
