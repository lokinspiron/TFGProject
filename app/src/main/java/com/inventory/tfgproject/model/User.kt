package com.inventory.tfgproject.model

import android.os.Parcel
import android.os.Parcelable

data class User(
    val name:String = "",
    val surname:String = "",
    val email:String = "",
    val birthDate: String? = null,
    val phoneNumber: String? = null,
    val address: String? = null,
    val profilePictureUrl: String? = null,
    val joinedDate:String? = "" ): Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(surname)
        parcel.writeString(email)
        parcel.writeString(birthDate)
        parcel.writeString(phoneNumber)
        parcel.writeString(address)
        parcel.writeString(profilePictureUrl)
        parcel.writeString(joinedDate)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}