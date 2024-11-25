package com.inventory.tfgproject.model

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class FirebaseDatabaseClient {
    fun saveUserData(user: FirebaseUser?) {
        val database = FirebaseDatabase.getInstance().reference
        val userId = user?.uid

        val userMap = mapOf(
            "name" to "user's name",
            "surname" to "user's surname",
            "email" to user?.email,
            "birthDate" to "user's birth date",
            "phoneNumber" to "user's phone number",
            "address" to "user's address",
            "profilePictureUrl" to "user's profile picture URL",
            "joinedDate" to System.currentTimeMillis()
        )

        if (userId != null) {
            database.child("users").child(userId).setValue(userMap)
                .addOnSuccessListener {

                }
                .addOnFailureListener {

                }
        }
    }
}