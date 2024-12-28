package com.inventory.tfgproject

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.inventory.tfgproject.model.User

class UserRepository {
    private val db = FirebaseDatabase.getInstance()
    private val userRef = db.getReference("users")
    private val currentUser = FirebaseAuth.getInstance().currentUser

    fun getUserData(onSuccess: (User?) -> Unit, onError: (Exception) -> Unit) {
        currentUser?.let { users ->
            userRef.child(users.uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val user = if (snapshot.exists()) {
                            User(
                                name = snapshot.child("name").getValue(String::class.java) ?: "",
                                surname = snapshot.child("surname").getValue(String::class.java) ?: "",
                                email = snapshot.child("email").getValue(String::class.java) ?: "",
                                birthDate = snapshot.child("birthDate").getValue(String::class.java),
                                phoneNumber = snapshot.child("phoneNumber").getValue(String::class.java),
                                address = snapshot.child("address").getValue(String::class.java),
                                profilePictureUrl = snapshot.child("profilePictureUrl").getValue(String::class.java)
                            )
                        } else null
                        onSuccess(user)
                    } catch (e: Exception) {
                        onError(e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    onError(error.toException())
                }
            })
        } ?: onError(Exception("No user logged in"))
    }

    fun updateUserData(user: User, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        if (currentUser == null) {
            onError(Exception("User not authenticated"))
            return
        }

        userRef.child(currentUser.uid)
            .setValue(user)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }
}

