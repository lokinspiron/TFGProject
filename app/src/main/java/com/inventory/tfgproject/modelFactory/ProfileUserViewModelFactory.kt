package com.inventory.tfgproject.modelFactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.inventory.tfgproject.repository.UserRepository
import com.inventory.tfgproject.viewmodel.ProfileUserViewModel

class ProfileUserViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileUserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileUserViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}