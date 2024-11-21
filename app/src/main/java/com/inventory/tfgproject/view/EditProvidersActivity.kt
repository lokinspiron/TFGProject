package com.inventory.tfgproject.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.ActivityEditProvidersBinding

class EditProvidersActivity : AppCompatActivity() {
    private lateinit var binding : ActivityEditProvidersBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditProvidersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
    }
}