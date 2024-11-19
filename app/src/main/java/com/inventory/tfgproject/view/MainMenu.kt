package com.inventory.tfgproject.view

import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageButton
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.ActivityMainMenuBinding

class MainMenu : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding:ActivityMainMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        initListeners()

    }

    private fun initListeners() {
        val btnDrawerToggle: ImageButton = findViewById(R.id.btnDrawerToggle)
        btnDrawerToggle.setOnClickListener {
            binding.drawerlt.openDrawer(GravityCompat.START)
        }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return true
    }

    override fun onBackPressed() {

        if (binding.drawerlt.isDrawerOpen(GravityCompat.START)) {
            binding.drawerlt.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

}