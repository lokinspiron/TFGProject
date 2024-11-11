package com.example.tfgproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.tfgproject.model.FirebaseAuthClient
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.ActivityLoginScreenBinding

class LoginScreen : AppCompatActivity() {
    private lateinit var binding : ActivityLoginScreenBinding
    private val authenticator = FirebaseAuthClient()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val loginHelper = LoginHelper(this)
        loginHelper.showLoginSuccessToast()

        binding.btnLogin.setOnClickListener {
            val email = binding.edtEmail.text.toString()
            val password = binding.edtPassword.text.toString()

            authenticator.loginUser(email,password)
        }


    }
}