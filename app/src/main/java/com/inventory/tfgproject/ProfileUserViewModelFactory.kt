package com.inventory.tfgproject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.inventory.tfgproject.viewmodel.ProfileUserViewModel
import com.inventory.tfgproject.viewmodel.UserViewModel

class ProfileUserViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileUserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileUserViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}