package com.inventory.tfgproject.model


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import java.util.Locale

class FirebaseAuthClient {
    private val auth = FirebaseAuth.getInstance()
    init {
        auth.setLanguageCode(Locale.getDefault().language)
    }
    fun loginUser(email:String,password:String,callback: (Boolean) -> Unit){
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener{ task ->
                if(task.isSuccessful) {
                    callback(true)
                }else {
                    callback(false)
                }

            }
    }

    fun registerUser(email: String, password: String, callback: (Boolean) -> Unit){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true)
                } else {
                    callback(false)
                }
            }
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

}
