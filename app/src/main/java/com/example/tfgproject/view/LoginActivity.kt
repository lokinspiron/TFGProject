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

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val backLayout: ConstraintLayout = findViewById(R.id.bkgBienvenido)
        AnimationUtil.UpAnimation(this, backLayout, R.anim.desplazamiento_arriba)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginScreen::class.java))
        }

        val txtRegistrate2 = findViewById<TextView>(R.id.txtRegistrate2)
        txtRegistrate2.setOnClickListener {
            startActivity(Intent(this, RegisterScreen::class.java))
        }
        val rSpannableString = SpannableString(txtRegistrate2.text)
        rSpannableString.setSpan(UnderlineSpan(),0,rSpannableString.length,0)
        txtRegistrate2.text = rSpannableString


    }
}