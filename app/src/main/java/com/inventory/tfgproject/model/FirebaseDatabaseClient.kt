package com.inventory.tfgproject.model

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class FirebaseDatabaseClient {
    private val database = FirebaseDatabase.getInstance().reference

    fun saveUserData(user:FirebaseUser) {
        val userRef = database.child("users")
        userRef.setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("Firebase", "User data saved successfully")
            } else {
                Log.e("Firebase", "Error saving user data", task.exception)
            }
        }
    }
}