package com.inventory.tfgproject.view

import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
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

        val btnDrawerToggle: ImageButton = findViewById(R.id.btnDrawerToggle)
        val drawerlt : DrawerLayout = findViewById(R.id.drawerlt)
        val lltInventory : LinearLayout = findViewById(R.id.lltInventory)
        val lltProvider : LinearLayout = findViewById(R.id.lltProvider)

        initListeners(btnDrawerToggle,drawerlt,lltInventory,lltProvider)

    }

    private fun initListeners(btnDrawerToggle: ImageButton,drawerlt: DrawerLayout,lltInventory: LinearLayout,lltProvider: LinearLayout) {

        btnDrawerToggle.setOnClickListener {
            drawerlt.openDrawer(GravityCompat.START)
        }

        val fragment_inventory = Inventory_Fragment()
        lltInventory.setOnClickListener{
            val inventory_transaction= supportFragmentManager.beginTransaction()
            inventory_transaction.replace(R.id.fcvInventory,fragment_inventory)
            inventory_transaction.addToBackStack(null)
            inventory_transaction.commit()
        }

        val fragment_provider = ProviderFragment()
        lltProvider.setOnClickListener{
            val provider_transaction = supportFragmentManager.beginTransaction()
            provider_transaction.replace(R.id.fcvProviders,fragment_provider)
            provider_transaction.addToBackStack(null)
            provider_transaction.commit()
        }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return true
    }

    override fun onBackPressed() {
        val drawerlt : DrawerLayout = findViewById(R.id.drawerlt)
        if (drawerlt.isDrawerOpen(GravityCompat.START)) {
            drawerlt.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

}