package com.example.tfgproject.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.viewModels
import com.example.tfgproject.extension.dismissKeyboard
import com.example.tfgproject.extension.loseFocusAfterAction
import com.example.tfgproject.extension.onTextChanged
import com.example.tfgproject.model.FirebaseAuthClient
import com.inventory.tfgproject.databinding.ActivityLoginScreenBinding
import com.example.tfgproject.extension.toast
import com.example.tfgproject.viewmodel.LoginViewModel

class LoginScreen : AppCompatActivity() {
    private lateinit var binding : ActivityLoginScreenBinding
    private val authenticator = FirebaseAuthClient()
    val loginViewModel:LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)


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

    private fun initListeners(){
        binding.edtEmail.loseFocusAfterAction(EditorInfo.IME_ACTION_NEXT)
        binding.edtEmail.onTextChanged { onFieldChanged() }

        binding.edtPassword.loseFocusAfterAction(EditorInfo.IME_ACTION_DONE)
        binding.edtEmail.onTextChanged { onFieldChanged() }


    }

    private fun onFieldChanged() {

    }


}