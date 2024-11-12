package com.example.tfgproject.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent;
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.tfgproject.AnimationUtil
import com.example.tfgproject.LoginScreen
import com.example.tfgproject.RegisterScreen
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.ActivityLoginBinding
import com.example.tfgproject.extension.span
import com.example.tfgproject.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    val loginViewModel:LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()

        val backLayout: ConstraintLayout = findViewById(R.id.bkgBienvenido)
        AnimationUtil.UpAnimation(this, backLayout, R.anim.desplazamiento_arriba)

        binding.btnLogin.setOnClickListener { startActivity(Intent(this, LoginScreen::class.java))}
        binding.txtRegistrate.setOnClickListener { startActivity(Intent(this, RegisterScreen::class.java)) }

        binding.txtRegistrate.text = span("Si no tienes cuenta","Registrate")
    }

    private fun initUI() {
        initListeners()
    }

    private fun initListeners(){
        with(binding){
            btnLogin.setOnClickListener {loginViewModel.onLoginSelected()}
            txtRegistrate.setOnClickListener {loginViewModel.onRegisterSelected()}




        }
    }
}
