package com.inventory.tfgproject.model

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class FirebaseDatabaseClient {
    private val database = FirebaseDatabase.getInstance().reference
    private val userRef = database.child("users")

}