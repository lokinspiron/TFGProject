package com.inventory.tfgproject.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.inventory.tfgproject.AnimationUtil
import com.inventory.tfgproject.R
import com.inventory.tfgproject.model.FirebaseAuthClient
import com.inventory.tfgproject.databinding.ActivityLoginScreenBinding
import com.inventory.tfgproject.extension.loseFocusAfterAction
import com.inventory.tfgproject.extension.toast
import com.inventory.tfgproject.viewmodel.AuthViewModel
import com.inventory.tfgproject.viewmodel.LoginViewModel

class LoginScreen : AppCompatActivity() {
    private lateinit var binding : ActivityLoginScreenBinding
    private val authClient = FirebaseAuthClient()
    val loginViewModel:LoginViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
            val email = binding.edtEmailLogin.text.toString()
            val password = binding.edtPasswordLogin.text.toString()

            if (validEmail && validPassword && email.isNotEmpty() && password.isNotEmpty()) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = FirebaseAuth.getInstance().currentUser
                            if (user != null && user.isEmailVerified) {
                                toast("Correo verificado. Accediendo a la cuenta...", Toast.LENGTH_SHORT)
                                startActivity(Intent(this, MainMenu::class.java))
                            } else {
                                registerLoadingScreen()
                                authViewModel.sendVerificationEmail()
                            }
                        } else {
                            task.exception?.let { exception ->
                                when (exception) {
                                    is FirebaseAuthInvalidCredentialsException -> {
                                        binding.emailLoginContainer.helperText = "El correo no existe o es incorrecto"
                                        binding.passwordLoginContainer.helperText = "La contraseña es incorrecta"
                                    }
                                    else -> {
                                        toast("Error al iniciar sesión", Toast.LENGTH_SHORT)
                                    }
                                }
                            }
                        }
                    }
            } else {
                invalidForm()
            }
        }

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

        authViewModel.isEmailVerified.observe(this, Observer{isVerified ->
            if(isVerified){
                authViewModel.stopVerificationCheck()
                toast("Correo verificado", Toast.LENGTH_SHORT)
                startActivity(Intent(this,MainMenu::class.java))
            }else{
                setContentView(viewLoading)
            }
        })
        authViewModel.startVerificationCheck()
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
}