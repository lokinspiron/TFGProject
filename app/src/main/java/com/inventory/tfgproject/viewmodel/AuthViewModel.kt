package com.inventory.tfgproject.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.whileSelect

class AuthViewModel: ViewModel() {
    private val firebaseDb = FirebaseDatabase.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val _isEmailVerified = MutableLiveData<Boolean>()
    val isEmailVerified: LiveData<Boolean> = _isEmailVerified

    private val _user = MutableLiveData<FirebaseUser>()
    val user: LiveData<FirebaseUser> get() = _user

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private var verificationJob: Job? = null

    fun sendVerificationEmail(){
        val user = firebaseAuth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener{ task ->
            if(task.isSuccessful){
                Log.d("Auth", "Email de verificación enviado correctamente")
            }else {
                Log.d("Auth", "Error al enviar email de verificación")
                _isEmailVerified.postValue(false)
            }
        }
    }

    fun startVerificationCheck() {
        verificationJob = CoroutineScope(Dispatchers.IO).launch{
            while(true){
                val user = firebaseAuth.currentUser
                user?.reload()
                if(user?.isEmailVerified == true){
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

    fun signInWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    firebaseAuth.currentUser?.let { firebaseUser ->
                        Log.d("FirebaseAuth", "Usuario autenticado: ${firebaseUser.displayName}")
                        _user.value = firebaseUser
                    } ?: run {
                        Log.e("FirebaseAuth", "El usuario es null después de la autenticación.")
                        _error.value = "Error: El usuario es null"
                    }
                } else {
                    _error.value = "Error de autenticación"
                }
            }
    }

    fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            signInWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            _error.value = "Error en la autenticación con Google"
            Log.w("AuthenticationViewModel", "signInResult:failed code=" + e.statusCode)
        }
    }

    fun saveUserDetailsToDatabase(user: FirebaseUser) {
        val db = FirebaseDatabase.getInstance()
        val userRef = db.reference.child("users").child(user.uid)

        val userData = hashMapOf(
            "name" to user.displayName,
            "email" to user.email,
            "profilePictureUrl" to user.photoUrl.toString()
        )

        userRef.setValue(userData)
            .addOnSuccessListener {
                Log.d("AuthenticationViewModel", "User details saved")
            }
            .addOnFailureListener { e ->
                Log.w("AuthenticationViewModel", "Error saving user details", e)
            }
    }
}