package com.example.tfgproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import com.inventory.tfgproject.R

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val backLayout: ConstraintLayout = findViewById(R.id.bkgBienvenido)

        AnimationUtil.UpAnimation(this,backLayout,R.anim.desplazamiento_arriba)
    }
}