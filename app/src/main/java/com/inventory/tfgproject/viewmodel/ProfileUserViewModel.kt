package com.inventory.tfgproject.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.inventory.tfgproject.repository.UserRepository
import com.inventory.tfgproject.model.User

class ProfileUserViewModel(private val repository: UserRepository) : ViewModel() {
    private val _userData = MutableLiveData<User?>()
    val userData: LiveData<User?> = _userData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _updateSuccess = MutableLiveData<Boolean>()
    val updateSuccess: LiveData<Boolean> = _updateSuccess

    private val _deleteSuccess = MutableLiveData<Boolean>()
    val deleteSuccess: LiveData<Boolean> = _deleteSuccess

    fun deleteUser() {
        _isLoading.value = true
        repository.deleteUser(
            onSuccess = {
                _deleteSuccess.postValue(true)
                _isLoading.value = false
            },
            onError = { exception ->
                _error.postValue(exception.message)
                _deleteSuccess.postValue(false)
                _isLoading.value = false
                Log.e("UserViewModel", "Error deleting user", exception)
            }
        )
    }

    fun loadUserData() {
        _isLoading.value = true
        repository.getUserData(
            onSuccess = { user ->
                _userData.postValue(user)
                _isLoading.value = false
                _error.value = null
            },
            onError = { exception ->
                _error.postValue(exception.message)
                _isLoading.value = false
                Log.e("UserViewModel", "Error loading user data", exception)
            }
        )
    }

    fun updateUserData(updatedUser: User) {
        Log.d("ProfileUserViewModel", "updateUserData() called with user: $updatedUser")
        _isLoading.value = true

        repository.updateUserData(
            user = updatedUser,
            onSuccess = {
                Log.d("ProfileUserViewModel", "Update success callback received")
                _updateSuccess.postValue(true)
                _isLoading.value = false
                loadUserData()
            },
            onError = { exception ->
                Log.e("ProfileUserViewModel", "Update error callback received", exception)
                _error.postValue(exception.message)
                _updateSuccess.postValue(false)
                _isLoading.value = false
            }
        )
    }

    fun updateProfilePicture(imageUrl: String) {
        _isLoading.value = true

        repository.updateUserProfilePicture(
            imageUrl = imageUrl,
            onSuccess = {
                _updateSuccess.postValue(true)
                _isLoading.value = false
                loadUserData()
            },
            onError = { exception ->
                _error.postValue(exception.message)
                _updateSuccess.postValue(false)
                _isLoading.value = false
            }
        )
    }


}