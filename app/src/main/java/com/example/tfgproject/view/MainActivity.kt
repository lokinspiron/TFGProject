package com.example.tfgproject.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.inventory.tfgproject.R
import android.os.Handler
import android.os.Looper
import com.airbnb.lottie.BuildConfig
import com.example.tfgproject.AnimationUtil
import com.inventory.tfgproject.databinding.ActivityMainBinding
import timber.log.Timber


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(BuildConfig.DEBUG){
            Timber.plant(Timber.DebugTree())
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        val dstockTextView: TextView = findViewById(R.id.txtNombre)
        val logoImageView: ImageView = findViewById(R.id.imgLogo)

        AnimationUtil.UpAnimation(this, logoImageView, R.anim.desplazamiento_arriba)
        AnimationUtil.DownAnimation(this, dstockTextView, R.anim.desplazamiento_abajo)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        },4000)

    }
}