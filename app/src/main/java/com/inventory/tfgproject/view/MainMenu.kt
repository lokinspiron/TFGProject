package com.inventory.tfgproject.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.ActivityMainMenuBinding
import com.inventory.tfgproject.viewmodel.UserViewModel


class MainMenu : AppCompatActivity(){
    private var requestCamara : ActivityResultLauncher<String>? = null
    private lateinit var binding: ActivityMainMenuBinding
    private val userViewModel : UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        userViewModel.loadUserData()
    }

    private fun initViewModels() {
        userViewModel.userData.observe(this,Observer{ user ->
            if (user != null) {
                Log.d("User Data", "User: ${user.name}, ${user.email}")
                val greetingMessage = getString(R.string.greetings, user.name)
                binding.txtWave.text = greetingMessage
                replaceFragment(MenuMainFragment(),greetingMessage)

                val navigationView: NavigationView = findViewById(R.id.nav_view)
                val headerView = navigationView.getHeaderView(0)

                val txtNameHeader: TextView = headerView.findViewById(R.id.txtNameHeader)
                val txtEmailHeader: TextView = headerView.findViewById(R.id.txtEmailHeader)

                txtNameHeader.text = user.name
                txtEmailHeader.text = user.email

            } else {
                Log.d("User Data", "User data is null")
            }
        })
    }

    override fun onStart() {
        super.onStart()
        val btnDrawerToggle: ImageButton = findViewById(R.id.btnDrawerToggle)
        val drawerlt: DrawerLayout = findViewById(R.id.drawerlt)
        initListeners(btnDrawerToggle, drawerlt)
        replaceFragment(MenuMainFragment())
        initViewModels()
    }

    private fun initListeners(btnDrawerToggle: ImageButton, drawerlt: DrawerLayout) {
        btnDrawerToggle.setOnClickListener {
            drawerlt.openDrawer(GravityCompat.START)
        }

        val bnvMenu = binding.root.findViewById<BottomNavigationView>(R.id.bnvMenu)

        bnvMenu.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.btnHome -> {
                    replaceFragment(MenuMainFragment(),getString(R.string.greetings,userViewModel.userData.value?.name))
                    true
                }
                R.id.btnProviders -> {
                    replaceFragment(ProviderFragment(),"Proveedores")
                    true

                }
                else -> false
            }
        }

        val fabMenu = binding.root.findViewById<FloatingActionButton>(R.id.fabMenu)
        fabMenu.setOnClickListener{
            replaceFragment(InventoryFragment(),"Inventario")

        }
        val navMenu = binding.root.findViewById<NavigationView>(R.id.nav_view)
        navMenu.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(MenuMainFragment(),getString(R.string.greetings,userViewModel.userData.value?.name))
                }
                R.id.nav_scan -> {
                    replaceFragment(ScanCodeFragment(),"Escaneo")
                }
                R.id.nav_share -> {
                    share()
                }
                R.id.nav_settings -> {
                    Toast.makeText(this, "Settings seleccionado", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_about -> {
                    Toast.makeText(this, "About seleccionado", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_logout -> {
                    val dialogFragment: DialogFragment = DialogSafeChangeFragment()
                    dialogFragment.show(supportFragmentManager, "LogOut")
                }
            }
            drawerlt.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun share() {
        val message = """
            ¡Transforma tu negocio con DStock! ¿Eres un empresario que busca optimizar procesos, aumentar la productividad y llevar tu negocio al siguiente nivel? Con DStock, tendrás acceso a herramientas innovadoras y fáciles de usar que te ayudarán a gestionar tu empresa de manera más eficiente.
        """.trimIndent()
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(intent,null)
        startActivity(shareIntent)
    }


    private fun replaceFragment(fragment: Fragment,greetingMessage:String?=null) {
        val fragmentTag = fragment.javaClass.simpleName

        val fragmentTransaction = supportFragmentManager.beginTransaction()

        val existingFragment = supportFragmentManager.findFragmentByTag(fragmentTag)

        if (existingFragment == null) {
            fragmentTransaction.replace(R.id.fcvContent, fragment, fragmentTag)
            fragmentTransaction.commit()
        } else {
            supportFragmentManager.beginTransaction()
                .show(existingFragment)
                .commit()
        }
        greetingMessage?.let {
            binding.txtWave.text = it
        }
    }

    override fun onBackPressed() {
        val drawerlt: DrawerLayout = findViewById(R.id.drawerlt)
        if (drawerlt.isDrawerOpen(GravityCompat.START)) {
            drawerlt.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

}
