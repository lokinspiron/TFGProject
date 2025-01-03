package com.inventory.tfgproject.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.inventory.tfgproject.model.FirebaseAuthClient
import com.inventory.tfgproject.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class AuthViewModel: ViewModel() {
    private val firebaseDb = FirebaseDatabase.getInstance()
    private val firebaseAuth = FirebaseAuthClient()

    private val _isEmailVerified = MutableLiveData<Boolean>()
    val isEmailVerified: LiveData<Boolean> = _isEmailVerified

    private val _authStatus = MutableLiveData<Boolean>()
    val authStatus: LiveData<Boolean> get() = _authStatus

    private val _signStatus = MutableLiveData<Boolean>()
    val signStatus: LiveData<Boolean> get() = _signStatus

    private val _resetPasswordStatus = MutableLiveData<Boolean>()
    val resetPasswordStatus: LiveData<Boolean> = _resetPasswordStatus

    private val _authError = MutableLiveData<String?>()
    val authError: LiveData<String?> = _authError

    private val _user = MutableLiveData<FirebaseUser>()
    val user: LiveData<FirebaseUser>  get() = _user

    private var verificationJob: Job? = null

    fun createUser(email:String,password:String,user: User){
        firebaseAuth.createUserWithEmail(email,password,user){isSuccessful ->
            _authStatus.value = isSuccessful
        }
    }

    fun signInUser(email: String, password: String) {
        firebaseAuth.signInWithEmail(email, password) { isSuccessful, exception ->
            _signStatus.value = isSuccessful
            _authError.value = when (exception) {
                is FirebaseAuthInvalidUserException -> "El correo no existe"
                is FirebaseAuthInvalidCredentialsException -> "La contraseña es incorrecta"
                else -> exception?.message
            }
        }
    }

    fun sendVerificationEmail(){
        val user = firebaseAuth.getCurrentUser()
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Auth", "Email de verificación enviado correctamente")
                    startVerificationCheck(user)
                } else {
                    Log.e("Auth", "Error al enviar email de verificación: ${task.exception?.message}")
                    _isEmailVerified.postValue(false)
                }
            }
        } else {
            Log.e("Auth", "Error: Usuario no encontrado")
            _isEmailVerified.postValue(false)
        }
    }

    private fun startVerificationCheck(user : FirebaseUser) {
        verificationJob = CoroutineScope(Dispatchers.IO).launch{
            while(true){
                user.reload()
                if(user.isEmailVerified){
                    _isEmailVerified.postValue(true)
                    break
                }
                delay(2000)
            }
        }
    }

    fun stopVerificationCheck(){
        verificationJob?.cancel()
    }

    fun resetPassword(email: String) {
        firebaseAuth.auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Auth", "Email de recuperación enviado correctamente")
                    _resetPasswordStatus.postValue(true)
                } else {
                    Log.e("Auth", "Error al enviar email de recuperación: ${task.exception?.message}")
                    _resetPasswordStatus.postValue(false)
                }
            }
    }

    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.getCurrentUser()
    }

    fun clearError() {
        _authError.value = null
    }
}