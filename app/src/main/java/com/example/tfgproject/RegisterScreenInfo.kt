package com.example.tfgproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.ActivityRegisterScreen2Binding

class RegisterScreenInfo : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterScreen2Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterScreen2Binding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}