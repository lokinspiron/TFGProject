package com.inventory.tfgproject.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent;
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import com.inventory.tfgproject.AnimationUtil
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.ActivityLoginBinding
import com.inventory.tfgproject.extension.span

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val backLayout: ConstraintLayout = findViewById(R.id.bkgBienvenido)
        AnimationUtil.upAnimation(this, backLayout, R.anim.movement_up)

        binding.btnLogin.setOnClickListener { startActivity(Intent(this, LoginScreen::class.java))}
        binding.txtRegistrate.setOnClickListener { startActivity(Intent(this, RegisterScreen::class.java)) }

        binding.txtRegistrate.text = span("Si no tienes cuenta","Registrate")
    }

}
