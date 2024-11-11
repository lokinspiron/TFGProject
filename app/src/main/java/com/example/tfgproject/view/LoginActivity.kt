package com.example.tfgproject.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent;
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.tfgproject.AnimationUtil
import com.example.tfgproject.LoginScreen
import com.example.tfgproject.RegisterScreen
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val backLayout: ConstraintLayout = findViewById(R.id.bkgBienvenido)
        AnimationUtil.UpAnimation(this, backLayout, R.anim.desplazamiento_arriba)

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginScreen::class.java))
        }


        binding.txtRegistrate2.setOnClickListener {
            startActivity(Intent(this, RegisterScreen::class.java))
        }
        val rSpannableString = SpannableString(binding.txtRegistrate2.text)
        rSpannableString.setSpan(UnderlineSpan(),0,rSpannableString.length,0)
        binding.txtRegistrate2.text = rSpannableString

    }
}