package com.inventory.tfgproject.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.inventory.tfgproject.model.User

class UserViewModel: ViewModel() {
    private val _userData = MutableLiveData<User?>()
    val userData: LiveData<User?> get() = _userData

    fun loadUserData(currentUser:FirebaseUser){
        Log.d("Firebase", "Listening for changes on User UID: ${currentUser.uid}")
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.uid)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)
                    Log.d("Firebase", "User data updated: $user")
                    if (user != null) {
                        _userData.postValue(user)
                    }
                } else {
                    Log.e("Firebase", "User data not found in database for UID: ${currentUser.uid}")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("UserViewModel", "Error getting user data", databaseError.toException())
            }
        })
    }
}