package com.inventory.tfgproject.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.whileSelect

class AuthViewModel: ViewModel() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val _isEmailVerified = MutableLiveData<Boolean>()
    val isEmailVerified: LiveData<Boolean> = _isEmailVerified

    private var verificationJob: Job? = null

    fun sendVerificationEmail(){
        val user = firebaseAuth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener{ task ->
            if(task.isSuccessful){
                startVerificationCheck()
            }else {
                _isEmailVerified.postValue(false)
            }
        }
    }

    private fun startVerificationCheck() {
        verificationJob = CoroutineScope(Dispatchers.IO).launch{
            while(true){
                val user = firebaseAuth.currentUser
                user?.reload()
                if(user?.isEmailVerified == true){
                    _isEmailVerified.postValue(true)
                    break
                }
                delay(3000)
            }
        }
    }

    fun stopVerificationCheck(){
        verificationJob?.cancel()
    }
}