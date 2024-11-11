package com.example.tfgproject.model


import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import timber.log.Timber
import javax.inject.Inject

class FirebaseAuthClient() {
    private val auth = FirebaseAuth.getInstance()

    fun loginUser(email:String,password:String){
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener{ task ->
                if(task.isSuccessful) {
                    val user = auth.currentUser
                    Timber.tag("Login").d("Login correcto: %s", user?.email)
                }else {
                    Timber.tag("Login").d ("Error: ${task.exception?.message}")
                }

            }
    }

    fun registerUser(email: String, password: String){
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener{ task ->
                if(task.isSuccessful){
                    val user = auth.currentUser
                    saveUserData(user)
                }else {
                    Timber.tag("Register").e("Error: ${task.exception?.message}")
                }
    }

}

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

        if(userId != null){
         database.child("users").child(userId).setValue(userMap)
             .addOnSuccessListener {
                 Timber.tag("SaveUser").d("User data saved successfully")
             }
             .addOnFailureListener { exception ->
                 Timber.tag("SaveUser").e("Error saving user data: ${exception.message}")
             }
        }
        }
    }
