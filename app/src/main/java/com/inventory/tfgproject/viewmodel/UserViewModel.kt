package com.inventory.tfgproject.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.inventory.tfgproject.UserRepository
import com.inventory.tfgproject.model.User
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private val _profileImage = MutableLiveData<String?>(null)
    val profileImage: LiveData<String?> get() = _profileImage

    private val _userData = MutableLiveData<User?>()
    val userData: LiveData<User?> get() = _userData

    private var valueEventListener: ValueEventListener? = null
    private var userRef: DatabaseReference? = null

    fun loadUserData(currentUser: FirebaseUser) {
        removeListener()

        Log.d("Firebase", "Listening for changes on User UID: ${currentUser.uid}")
        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.uid)

        valueEventListener = object : ValueEventListener {
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
                clearUserData()
            }
        }

        // Agregar el listener
        valueEventListener?.let { listener ->
            userRef?.addValueEventListener(listener)
        }
    }

    private fun removeListener() {
        try {
            valueEventListener?.let { listener ->
                userRef?.removeEventListener(listener)
            }
        } catch (e: Exception) {
            Log.e("UserViewModel", "Error removing listener", e)
        } finally {
            valueEventListener = null
            userRef = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        removeListener()
    }

    fun clearUserData() {
        removeListener()
        _userData.postValue(null)
        _profileImage.postValue(null)
    }
}