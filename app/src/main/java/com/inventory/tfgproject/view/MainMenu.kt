package com.inventory.tfgproject.view

import android.content.ClipData.Item
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.ActivityMainMenuBinding

class MainMenu : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val btnDrawerToggle: ImageButton = findViewById(R.id.btnDrawerToggle)
        val drawerlt: DrawerLayout = findViewById(R.id.drawerlt)

        initListeners(btnDrawerToggle, drawerlt)

    }

    private fun initListeners(btnDrawerToggle: ImageButton, drawerlt: DrawerLayout) {

        btnDrawerToggle.setOnClickListener {
            drawerlt.openDrawer(GravityCompat.START)
        }


        val btnNavigationView = findViewById<BottomNavigationView>(R.id.bnvMenu)
        btnNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.btnInventory -> {
                    replaceFragment(Inventory_Fragment())
                    true
                }

                R.id.btnProviders -> {
                    replaceFragment(ProviderFragment())
                    true
                }

                else -> false
            }
        }


    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return true
    }


    override fun onBackPressed() {
        val drawerlt: DrawerLayout = findViewById(R.id.drawerlt)
        if (drawerlt.isDrawerOpen(GravityCompat.START)) {
            drawerlt.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fcvInventory, fragment)
        transaction.addToBackStack(null)
        transaction.commit()

    }
}