package com.inventory.tfgproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.inventory.tfgproject.databinding.ActivityRegisterScreenBinding

class RegisterScreen : AppCompatActivity() {
    private lateinit var binding:ActivityRegisterScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnContinue.setOnClickListener { startActivity(Intent(this,RegisterScreenInfo::class.java)) }
    }
}