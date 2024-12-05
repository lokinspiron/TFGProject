package com.inventory.tfgproject.viewmodel

import android.util.Patterns
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.inventory.tfgproject.model.FirebaseAuthClient

class LoginViewModel: ViewModel() {

    private val auth = FirebaseAuthClient()

    fun getUserName() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val database = FirebaseDatabase.getInstance().reference
            val userId = user.uid

            database.child("users").child(userId).get().addOnSuccessListener { snapshot ->
                val name = snapshot.child("name").value.toString()
            }
        }
    }

    private fun isValidEmail(email : String) = Patterns.EMAIL_ADDRESS.matcher(email).matches() || email.isEmpty()

    private fun isValidPassword(password : String): Boolean = password.length > 6 || password.isEmpty()

}