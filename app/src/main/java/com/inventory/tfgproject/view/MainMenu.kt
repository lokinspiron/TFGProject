package com.inventory.tfgproject.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.forEach
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseUser
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.ActivityMainMenuBinding
import com.inventory.tfgproject.model.User
import com.inventory.tfgproject.viewmodel.AuthViewModel
import com.inventory.tfgproject.viewmodel.UserViewModel
import de.hdodenhof.circleimageview.CircleImageView


class MainMenu : AppCompatActivity(){
    private var requestCamara : ActivityResultLauncher<String>? = null
    private lateinit var binding: ActivityMainMenuBinding
    val auth : AuthViewModel by viewModels()
    val userViewModel : UserViewModel by viewModels()

    private lateinit var headerImageView: CircleImageView
    private lateinit var headerNameView: TextView
    private lateinit var headerEmailView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)

        val navigationView: NavigationView = binding.navView
        val headerView = navigationView.getHeaderView(0)
        headerImageView = headerView.findViewById(R.id.imgProfilePhoto)
        headerNameView = headerView.findViewById(R.id.txtNameHeader)
        headerEmailView = headerView.findViewById(R.id.txtEmailHeader)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerlt.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerlt.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
        initVisibility()
        setContentView(binding.root)
        supportActionBar?.hide()
    }

    private fun initViewModels(currentUser: FirebaseUser) {
        userViewModel.userData.observe(this, Observer { user ->
            if (user != null) {
                updateUIWithUserData(user)
            }
        })
        userViewModel.loadUserData(currentUser)
    }

    private fun updateUIWithUserData(user: User) {
        Log.d("User Data", "User: ${user.name}, ${user.email}")
        val greetingMessage = getString(R.string.greetings, user.name)
        binding.txtWave.text = greetingMessage
        replaceFragment(MenuMainFragment(), greetingMessage)
        binding.loadingOverlay.visibility = View.GONE

        headerNameView.text = user.name
        headerEmailView.text = user.email

        updateProfileImage(user.profilePictureUrl)
    }

    private fun updateProfileImage(imageUrl: String?) {
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_user_image)
            .error(R.drawable.ic_user_image)
            .circleCrop()
            .into(headerImageView)

        Log.d("ProfileUpdate", "Updated profile picture: $imageUrl")
    }



    override fun onStart() {
        super.onStart()
        val btnDrawerToggle: ImageButton = findViewById(R.id.btnDrawerToggle)
        val drawerlt: DrawerLayout = findViewById(R.id.drawerlt)
        initListeners(btnDrawerToggle, drawerlt)
        replaceFragment(MenuMainFragment())

        val currentUser = auth.getCurrentUser()

        if (currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }
        initViewModels(currentUser)
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
        fabMenu.setOnClickListener {
            bnvMenu.menu.setGroupCheckable(0, true, false)
            bnvMenu.menu.forEach { item -> item.isChecked = false }
            bnvMenu.menu.setGroupCheckable(0, true, true)

            replaceFragment(InventoryFragment(), "Inventario")
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
                    replaceFragment(UserProfileFragment(),"Perfil")
                }
                R.id.nav_about -> {
                    replaceFragment(AboutUsFragment(),"Acerca de nosotros")
                }
                R.id.nav_logout -> {
                    val dialogFragment = DialogSafeChangeFragment.newInstance(
                        dynamicText = "Estás a punto de cerrar la sesión",
                        doItText = "Cerrar Sesión"
                    )
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

    fun replaceFragment(fragment: Fragment, greetingMessage: String? = null) {
        val fragmentTag = fragment.javaClass.simpleName
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        fragmentTransaction.replace(R.id.fcvContent, fragment, fragmentTag)

        if (fragment !is MenuMainFragment &&
            fragment !is ProviderFragment &&
            fragment !is InventoryFragment) {
            fragmentTransaction.addToBackStack(null)
        }

        fragmentTransaction.commit()

        greetingMessage?.let {
            binding.txtWave.text = it
        }
    }

    private fun initVisibility(){
        binding.loadingOverlay.visibility = View.VISIBLE
    }

    fun refreshUserData() {
        val currentUser = auth.getCurrentUser()
        if (currentUser != null) {
            userViewModel.loadUserData(currentUser)
        }
    }

    fun navigateBack() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

//    override fun onBackPressed() {
//        val drawerlt: DrawerLayout = findViewById(R.id.drawerlt)
//        if (drawerlt.isDrawerOpen(GravityCompat.START)) {
//            drawerlt.closeDrawer(GravityCompat.START)
//        } else {
//            super.onBackPressed()
//        }
//    }
}
