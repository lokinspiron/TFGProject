package com.inventory.tfgproject.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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

    private val _user = MutableLiveData<FirebaseUser>()
    val user: LiveData<FirebaseUser>  get() = _user

    private var verificationJob: Job? = null

    fun createUser(email:String,password:String,user: User){
        firebaseAuth.createUserWithEmail(email,password,user){isSuccessful ->
            _authStatus.value = isSuccessful
        }
    }

    fun signInUser(email: String,password: String){
        firebaseAuth.signInWithEmail(email,password){isSuccessful ->
            _signStatus.value = isSuccessful
        }
    }

    fun sendVerificationEmail(){
        val user = firebaseAuth.getCurrentUser()
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Auth", "Email de verificación enviado correctamente")
                } else {
                    Log.e("Auth", "Error al enviar email de verificación: ${task.exception?.message}")
                    _isEmailVerified.postValue(false)
                }
            }
            startVerificationCheck(user)
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

    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.getCurrentUser()
    }

//    fun signInWithGoogle(idToken: String) {
//        val credential = GoogleAuthProvider.getCredential(idToken, null)
//        firebaseAuth.signInWithCredential(credential)
//            .addOnCompleteListener { task: Task<AuthResult> ->
//                if (task.isSuccessful) {
//                    firebaseAuth.currentUser?.let { firebaseUser ->
//                        Log.d("FirebaseAuth", "Usuario autenticado: ${firebaseUser.displayName}")
//                        _user.value = firebaseUser
//                    } ?: run {
//                        Log.e("FirebaseAuth", "El usuario es null después de la autenticación.")
//                        _error.value = "Error: El usuario es null"
//                    }
//                } else {
//                    _error.value = "Error de autenticación"
//                }
//            }
//    }


//    fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
//        try {
//            val account = task.getResult(ApiException::class.java)
//            signInWithGoogle(account.idToken!!)
//        } catch (e: ApiException) {
//            _error.value = "Error en la autenticación con Google"
//            Log.w("AuthenticationViewModel", "signInResult:failed code=" + e.statusCode)
//        }
//    }

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