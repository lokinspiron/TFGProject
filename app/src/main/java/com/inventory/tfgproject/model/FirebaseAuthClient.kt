package com.inventory.tfgproject.model


import android.util.Log
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import java.util.Locale

class FirebaseAuthClient {
    val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference

    init {
        auth.setLanguageCode(Locale.getDefault().language)
    }

    fun createUserWithEmail(email: String, password: String, user: User, onComplete: (Boolean) -> Unit) {
        auth.signOut()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    val uid = firebaseUser?.uid
                    if (firebaseUser != null && uid != null) {
                        val userRef = db.child("users").child(uid)
                        userRef.setValue(user)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    firebaseUser.sendEmailVerification()
                                        .addOnCompleteListener { emailTask ->
                                            if (emailTask.isSuccessful) {
                                                Log.d("Auth", "Verification email sent")
                                            } else {
                                                Log.e("Auth", "Failed to send verification email")
                                            }
                                            onComplete(true)
                                        }
                                } else {
                                    onComplete(false)
                                    Log.d("Auth", "Failed to create user in database")
                                }
                            }
                    } else {
                        Log.e("Auth", "Error: FirebaseUser is null")
                        onComplete(false)
                    }
                } else {
                    Log.e("Auth", "Error creating user: ${task.exception?.message}")
                    onComplete(false)
                }
            }
    }

    fun signInWithEmail(email: String, password: String, callback: (Boolean, Exception?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful, task.exception)
            }
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun logout(){
        auth.signOut()
    }

    fun reAuthenticate(password: String, onComplete: (Boolean) -> Unit) {
        val user = auth.currentUser
        val email = user?.email

        if (user != null && email != null) {
            val credential = EmailAuthProvider.getCredential(email, password)
            user.reauthenticate(credential)
                .addOnCompleteListener { task ->
                    onComplete(task.isSuccessful)
                }
        } else {
            onComplete(false)
        }
    }





}
