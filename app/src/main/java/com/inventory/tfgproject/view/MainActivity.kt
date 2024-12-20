package com.inventory.tfgproject.view

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.initialize
import com.inventory.tfgproject.AnimationUtil
import com.inventory.tfgproject.R
import com.inventory.tfgproject.extension.toast


class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Firebase.initialize(context = this)

        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance())


        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        val dstockTextView: TextView = findViewById(R.id.txtNombre)
        val logoImageView: ImageView = findViewById(R.id.imgLogo)

        AnimationUtil.upAnimation(this, logoImageView, R.anim.movement_up)
        AnimationUtil.downAnimation(this, dstockTextView, R.anim.movement_down)

        Handler(Looper.getMainLooper()).postDelayed({
            if (!isNetworkAvailable()) {
                toast("No hay conexión a Internet. Por favor, verifica tu red.", Toast.LENGTH_LONG)
            } else {
                val currentUser = FirebaseAuth.getInstance().currentUser

                if (currentUser != null) {
                    startActivity(Intent(this, MainMenu::class.java))
                } else {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                finish()
            }
        }, 4000)

    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}