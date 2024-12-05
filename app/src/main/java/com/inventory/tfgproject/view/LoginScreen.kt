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
import com.inventory.tfgproject.viewmodel.LoginViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LoginScreen : AppCompatActivity() {
    private lateinit var binding : ActivityLoginScreenBinding
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var auth : FirebaseAuth
    private val RC_SIGN_IN = 9001
    private lateinit var user : User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        user = User()

        initListeners()
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
            val validEmail = binding.emailLoginContainer.helperText == null
            val validPassword = binding.passwordLoginContainer.helperText == null

            val email = binding.edtEmailLogin.text.toString().trim()
            val password = binding.edtPasswordLogin.text.toString().trim()

            if (validEmail && validPassword && email.isNotEmpty() && password.isNotEmpty()) {
                signInVerification(email,password)
            } else {
                invalidForm()
            }
        }
        binding.imgGoogle.setOnClickListener {
            confGoogleSignIn()
        }
    }

    private fun signInVerification(email:String, password:String){
        authViewModel.signStatus.observe(this,Observer {isSuccessful ->
            if(isSuccessful){
                val user = authViewModel.getCurrentUser()
                if(user != null && user.isEmailVerified){
                    toast("Correo verificado. Accediendo a la cuenta...", Toast.LENGTH_SHORT)
                    startActivity(Intent(this, MainMenu::class.java))
                }else {
                    registerLoadingScreen()
                }
            }else {
                binding.emailLoginContainer.helperText = "El correo no existe o es incorrecto"
                binding.passwordLoginContainer.helperText = "La contraseña es incorrecta"
            }
        })
        authViewModel.signInUser(email,password)
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
       // authViewModel.sendVerificationEmail()
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
    fun ActualDate():String{
        val sdf : SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaActual = Date()
        return sdf.format(fechaActual)
    }

    private fun confGoogleSignIn(){
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this,gso)

        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == RC_SIGN_IN){
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.let { firebaseAuthWithGoogle(it) }
            }catch (e: ApiException){
                Toast.makeText(this,"Google sign-in failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val currentUser = authViewModel.getCurrentUser()

                    var userName = currentUser?.displayName?.split(" ")?.get(0) ?: ""
                    var userSurname = currentUser?.displayName?.split(" ")?.getOrNull(1) ?: ""
                    val userEmail = currentUser?.email.toString()
                    val userPhone = currentUser?.phoneNumber
                    val userAddress = ""
                    val profilePhoto = currentUser?.photoUrl
                    val joinedDate = ActualDate()

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
                                        } else {
                                            Log.d("Firebase", "Error saving user data.")
                                        }
                                    }
                            } else {
                                Log.d("Firebase Database ","User already exists")
                            }
                        }

                    }

                    Toast.makeText(this, "Hello, ${currentUser?.displayName}!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this,MainMenu::class.java))
                } else {
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}