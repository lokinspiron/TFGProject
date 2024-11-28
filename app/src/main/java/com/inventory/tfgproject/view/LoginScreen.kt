package com.inventory.tfgproject.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.inventory.tfgproject.AnimationUtil
import com.inventory.tfgproject.R
import com.inventory.tfgproject.extension.loseFocusAfterAction
import com.inventory.tfgproject.extension.onTextChanged
import com.inventory.tfgproject.model.FirebaseAuthClient
import com.inventory.tfgproject.databinding.ActivityLoginScreenBinding
import com.inventory.tfgproject.extension.toast
import com.inventory.tfgproject.viewmodel.AuthViewModel
import com.inventory.tfgproject.viewmodel.LoginViewModel

class LoginScreen : AppCompatActivity() {
    private lateinit var binding : ActivityLoginScreenBinding
    private val authenticator = FirebaseAuthClient()
    val loginViewModel:LoginViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initListeners()
    }

    private fun initListeners(){
        binding.edtEmailLogin.loseFocusAfterAction(EditorInfo.IME_ACTION_NEXT)
        binding.edtPasswordLogin.loseFocusAfterAction(EditorInfo.IME_ACTION_DONE)

        binding.btnLogin.setOnClickListener {
            val email = binding.edtEmailLogin.text.toString()
            val password = binding.edtPasswordLogin.text.toString()

            authViewModel.isEmailVerified.observe(this, Observer { isVerified ->
                if (isVerified) {
                    toast("Correo verificado. Accediendo a la cuenta...", Toast.LENGTH_LONG)
                    authenticator.loginUser(email,password){isSuccess ->
                        if(isSuccess){
                            toast("Login exitoso", Toast.LENGTH_SHORT)
                            startActivity(Intent(this,MainMenu::class.java))
                        }else {
                            toast("Error al iniciar sesión", Toast.LENGTH_SHORT)
                        }
                    }
                }else {
                    registerLoadingScreen()
                }
            })
        }
    }


    private fun registerLoadingScreen(){
        val viewLoading = layoutInflater.inflate(R.layout.activity_register_loading_screen,null,false)

        setContentView(viewLoading)

        val dstockTextView: TextView = viewLoading.findViewById(R.id.txtDStockLoading)
        val logoImageView: ImageView = viewLoading.findViewById(R.id.imgRegisterLoading)

        AnimationUtil.UpAnimation(this, logoImageView, R.anim.rotate_open_anim)
        AnimationUtil.DownAnimation(this, dstockTextView, R.anim.rotate_open_anim)

        authViewModel.sendVerificationEmail()

        authViewModel.isEmailVerified.observe(this, Observer{isVerified ->
            if(isVerified){
                toast("Correo verificado", Toast.LENGTH_SHORT)
                startActivity(Intent(this,MainMenu::class.java))
            }else{
                setContentView(viewLoading)
            }
        })

    }
}