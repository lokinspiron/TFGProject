package com.inventory.tfgproject.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.viewModels
import com.inventory.tfgproject.extension.loseFocusAfterAction
import com.inventory.tfgproject.extension.onTextChanged
import com.inventory.tfgproject.model.FirebaseAuthClient
import com.inventory.tfgproject.databinding.ActivityLoginScreenBinding
import com.inventory.tfgproject.extension.toast
import com.inventory.tfgproject.viewmodel.LoginViewModel

class LoginScreen : AppCompatActivity() {
    private lateinit var binding : ActivityLoginScreenBinding
    private val authenticator = FirebaseAuthClient()
    val loginViewModel:LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initListeners()
    }

    private fun initListeners(){
        binding.edtEmail.loseFocusAfterAction(EditorInfo.IME_ACTION_NEXT)
        binding.edtEmail.onTextChanged { onFieldChanged() }

        binding.edtPassword.loseFocusAfterAction(EditorInfo.IME_ACTION_DONE)
        binding.edtEmail.onTextChanged { onFieldChanged() }

        binding.btnLogin.setOnClickListener {
            val email = binding.edtEmail.text.toString()
            val password = binding.edtPassword.text.toString()

            authenticator.loginUser(email,password){isSuccess ->
                if(isSuccess){
                    toast("Login exitoso", Toast.LENGTH_SHORT)
                    startActivity(Intent(this,MainMenu::class.java))
                }else {
                    toast("Error al iniciar sesión", Toast.LENGTH_SHORT)
                }

            }
        }
    }

    private fun onFieldChanged() {

    }


}