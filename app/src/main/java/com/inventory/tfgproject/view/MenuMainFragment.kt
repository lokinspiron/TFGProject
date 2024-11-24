package com.inventory.tfgproject.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.FragmentMenuMainBinding


class MenuMainFragment : Fragment() {
    private lateinit var binding : FragmentMenuMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMenuMainBinding.inflate(inflater,container,false)
        initListeners()
        return binding.root
    }

    private fun initListeners() {
        val btnDrawerToggle= binding.root.findViewById<ImageButton>(R.id.btnDrawerToggle)
        val drawerlt = binding.root.findViewById<DrawerLayout>(R.id.drawerlt)
        btnDrawerToggle.setOnClickListener {
            drawerlt.openDrawer(GravityCompat.START)
        }

        val bnvMenu = binding.root.findViewById<BottomNavigationView>(R.id.bnvMenu)

        bnvMenu.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.btnHome -> {
                    replaceFragment(InventoryFragment())
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

    private fun replaceFragment(fragment: Fragment)  {
        val fragmentTag = fragment.javaClass.simpleName

        val fragmentTransaction = childFragmentManager.beginTransaction()

        val existingFragment = childFragmentManager.findFragmentByTag(fragmentTag)

        if (existingFragment == null) {
            fragmentTransaction.replace(R.id.fcvContent, fragment, fragmentTag)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        } else {
            fragmentTransaction.show(existingFragment)
            fragmentTransaction.commit()
        }
    }

}