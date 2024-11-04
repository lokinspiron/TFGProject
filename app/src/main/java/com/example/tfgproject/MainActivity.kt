package com.example.tfgproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import com.inventory.tfgproject.R
import android.os.Handler
import android.os.Looper


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        val animacion1: Animation = AnimationUtils.loadAnimation(this, R.anim.desplazamiento_arriba)
        val animacion2: Animation = AnimationUtils.loadAnimation(this, R.anim.desplazamiento_abajo)

        val dstockTextView: TextView = findViewById(R.id.txtNombre)
        val logoImageView: ImageView = findViewById(R.id.imgLogo)

        dstockTextView.animation = animacion2
        logoImageView.animation = animacion1

        dstockTextView.setAnimation(animacion2);
        logoImageView.setAnimation(animacion1);

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
            finish()
        },4000)

    }
}