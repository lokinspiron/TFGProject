package com.inventory.tfgproject.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.inventory.tfgproject.model.User

class UserViewModel: ViewModel() {
    private val _userData = MutableLiveData<User?>()
    val userData: LiveData<User?> get() = _userData

    fun loadUserData(){
        val currentUser = FirebaseAuth.getInstance().currentUser
        Log.d("Firebase", "User UID: ${currentUser?.uid}")
        if(currentUser != null){
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.uid)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user = dataSnapshot.getValue(User::class.java)
                    if (user != null) {
                        _userData.postValue(user)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("UserViewModel", "Error getting user data", databaseError.toException())
                }
            })
        }
    }

    fun updateUser(user: User?) {
        _userData.value = user
    }
}