package com.example.tfgproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.tfgproject.model.FirebaseAuthClient
import com.inventory.tfgproject.databinding.ActivityLoginScreenBinding
import com.example.tfgproject.extension.toast

class LoginScreen : AppCompatActivity() {
    private lateinit var binding : ActivityLoginScreenBinding
    private val authenticator = FirebaseAuthClient()

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
                }else {
                    toast("Error al iniciar sesión", Toast.LENGTH_SHORT)
                }

            }
        }

    }




}