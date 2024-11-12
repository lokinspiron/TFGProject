package com.example.tfgproject.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tfgproject.Event

class LoginViewModel: ViewModel() {

    private val _navigateToLogin = MutableLiveData<Event<Boolean>>()
    val navigateLogin: LiveData<Event<Boolean>>
        get() = _navigateToLogin

    private val _navigateToRegister = MutableLiveData<Event<Boolean>>()
    val navigateRegister: LiveData<Event<Boolean>>
        get() = _navigateToRegister

    fun onLoginSelected(){
        _navigateToLogin.value = Event(true)
    }

    fun onRegisterSelected(){
        _navigateToRegister.value = Event(true)
    }
}