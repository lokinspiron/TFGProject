package com.example.tfgproject.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.ActivityRegisterLoadingScreenBinding

class RegisterLoadingScreen : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterLoadingScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterLoadingScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}