package com.inventory.tfgproject.view

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.inventory.tfgproject.AnimationUtil
import com.inventory.tfgproject.R
import com.inventory.tfgproject.model.FirebaseAuthClient
import com.inventory.tfgproject.databinding.ActivityLoginScreenBinding
import com.inventory.tfgproject.extension.loseFocusAfterAction
import com.inventory.tfgproject.extension.toast
import com.inventory.tfgproject.model.User
import com.inventory.tfgproject.viewmodel.AuthViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LoginScreen : AppCompatActivity() {
    private lateinit var binding : ActivityLoginScreenBinding
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var auth : FirebaseAuth
    private lateinit var user : User
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        user = User()

        initListeners()

        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    if (account != null) {
                        Log.d("GoogleSignIn", "Google account: ${account.displayName}")
                        firebaseAuthWithGoogle(account)
                    } else {
                        Log.e("GoogleSignIn", "Account is null, task failed")
                    }
                } catch (e: ApiException) {
                    Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("GoogleSignIn", "Error: ${e.message}")
                }
            }
        }
    }

    private fun initListeners(){
        binding.edtPasswordLogin.loseFocusAfterAction(EditorInfo.IME_ACTION_DONE)
        binding.emailLoginContainer.helperText = null
        binding.passwordLoginContainer.helperText = null

        binding.edtEmailLogin.setOnFocusChangeListener{_,focused ->
            if(!focused){
                binding.emailLoginContainer.helperText = validEmail()
            }
        }

        binding.edtPasswordLogin.setOnFocusChangeListener{_,focused ->
            if(!focused){
                binding.passwordLoginContainer.helperText = validPassword()
            }
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.edtEmailLogin.text.toString().trim()
            val password = binding.edtPasswordLogin.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    signInVerification(email, password)
                } else {
                    binding.emailLoginContainer.helperText = "Introduce un correo válido"
                }
            } else {
                invalidForm()
            }
        }

        binding.imgGoogle.setOnClickListener {
            launchGoogleSignIn()
        }

        binding.txtForgotPassword.setOnClickListener {
            val email = binding.edtEmailLogin.text.toString().trim()
            if (email.isNotEmpty() && binding.emailLoginContainer.helperText == null) {
                authViewModel.resetPassword(email)
                toast("Se ha enviado el correo para restablecer la contraseña", LENGTH_SHORT)
            } else {
                binding.emailLoginContainer.helperText = "Introduce un correo válido"
            }
        }
    }

    private fun signInVerification(email: String, password: String) {
        binding.btnLogin.isEnabled = false
        binding.pbLogin.visibility = View.VISIBLE

        binding.emailLoginContainer.helperText = null
        binding.passwordLoginContainer.helperText = null
        authViewModel.clearError()

        authViewModel.signStatus.observe(this) { isSuccessful ->
            binding.btnLogin.isEnabled = true
            binding.pbLogin.visibility = View.GONE

            if (isSuccessful) {
                val user = authViewModel.getCurrentUser()
                if (user != null && user.isEmailVerified) {
                    toast("Accediendo a la cuenta...", Toast.LENGTH_SHORT)
                    startActivity(Intent(this, MainMenu::class.java))
                } else {
                    registerLoadingScreen()
                }
            }
        }

        authViewModel.authError.observe(this) { errorMessage ->
            errorMessage?.let {
                when {
                    it.contains("correo") -> binding.emailLoginContainer.helperText = it
                    it.contains("contraseña") -> binding.passwordLoginContainer.helperText = it
                    else -> toast(it, Toast.LENGTH_SHORT)
                }
            }
        }

        authViewModel.signInUser(email, password)
    }

    private fun invalidForm() {
        if (binding.edtEmailLogin.text.isNullOrEmpty()) {
            binding.emailLoginContainer.helperText = "Correo requerido"
        }
        if (binding.edtPasswordLogin.text.isNullOrEmpty()) {
            binding.passwordLoginContainer.helperText = "Contraseña requerida"
        }
    }


    private fun registerLoadingScreen(){
        val viewLoading = layoutInflater.inflate(R.layout.activity_register_loading_screen,null,false)

        setContentView(viewLoading)

        val dstockTextView: TextView = viewLoading.findViewById(R.id.txtDStockLoading)
        val logoImageView: ImageView = viewLoading.findViewById(R.id.imgRegisterLoading)

        if (!AnimationUtil.isAnimationDone) {
            AnimationUtil.upAnimation(this, logoImageView, R.anim.movement_down)
            AnimationUtil.downAnimation(this, dstockTextView, R.anim.movement_down)

            AnimationUtil.isAnimationDone = true
        }

        emailVerification(viewLoading)
    }

    private fun emailVerification(viewLoading: View){
        authViewModel.isEmailVerified.observe(this, Observer{isVerified ->
            if(isVerified){
                authViewModel.stopVerificationCheck()
                toast("Correo verificado", Toast.LENGTH_SHORT)
                startActivity(Intent(this,MainMenu::class.java))
            }else{
                setContentView(viewLoading)
            }
        })

        authViewModel.resetPasswordStatus.observe(this) { isSuccessful ->
            if (isSuccessful) {
                toast("Se ha enviado un correo para restablecer tu contraseña", Toast.LENGTH_LONG)
            } else {
                toast("Error al enviar el correo de recuperación", Toast.LENGTH_SHORT)
            }
        }
    }

    private fun validPassword(): String? {
        val password = binding.edtPasswordLogin.text.toString().trim()
        if (!password.contains(Regex("(?=.*[A-Z])(?=.*\\d)"))) {
            return "Incluye al menos una mayúscula y número"
        }
        return null
    }

    private fun validEmail(): String? {
        val email = binding.edtEmailLogin.text.toString().trim()
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            return "Introduce un correo válido"
        }
        return null
    }

    @SuppressLint("SimpleDateFormat")
    fun actualDate():String{
        val sdf : SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaActual = Date()
        return sdf.format(fechaActual)
    }

    private fun launchGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .setLogSessionId(true.toString())
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        if (FirebaseAuth.getInstance().currentUser != null) {
            FirebaseAuth.getInstance().signOut()
        }

        googleSignInClient.signOut().addOnCompleteListener {
            googleSignInClient.revokeAccess().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val currentUser = authViewModel.getCurrentUser()

                    val userName = currentUser?.displayName?.split(" ")?.get(0) ?: ""
                    val userSurname = currentUser?.displayName?.split(" ")?.getOrNull(1) ?: ""
                    val userEmail = currentUser?.email.toString()
                    val userPhone = currentUser?.phoneNumber
                    val userAddress = ""
                    val profilePhoto = currentUser?.photoUrl
                    val joinedDate = actualDate()

                    user = User(
                        name = userName,
                        surname = userSurname,
                        email = userEmail,
                        phoneNumber = userPhone,
                        address = userAddress,
                        profilePictureUrl = profilePhoto.toString(),
                        joinedDate = joinedDate
                    )

                    val userId = currentUser?.uid
                    if (userId != null) {
                        val database = FirebaseDatabase.getInstance()
                        val userRef = database.getReference("users").child(userId)

                        userRef.get().addOnSuccessListener { snapshot ->
                            if(!snapshot.exists()){
                                userRef.setValue(user)
                                    .addOnCompleteListener { dbTask ->
                                        if (dbTask.isSuccessful) {
                                            Log.d("Firebase", "User data saved successfully.")
                                            navigateToMainMenu()
                                        } else {
                                            Log.d("Firebase", "Error saving user data.")
                                        }
                                    }
                            } else {
                                Log.d("Firebase Database ","User already exists")
                                navigateToMainMenu()
                            }
                        }
                    }

                } else {
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToMainMenu() {
        startActivity(Intent(this, MainMenu::class.java))
        finish()
    }
}