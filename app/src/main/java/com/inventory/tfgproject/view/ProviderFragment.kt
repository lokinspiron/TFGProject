package com.inventory.tfgproject.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.FragmentInventoryBinding
import com.inventory.tfgproject.databinding.FragmentProviderBinding


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class ProviderFragment : Fragment() {
    private var _binding: FragmentProviderBinding? = null
    private val binding get() = _binding!!

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    private fun initListeners() {
        val btnDrawerToggle= binding.root.findViewById<ImageButton>(R.id.btnDrawerToggle)
        val drawerlt = binding.root.findViewById<DrawerLayout>(R.id.drawerlt)
        btnDrawerToggle.setOnClickListener {
            drawerlt.openDrawer(GravityCompat.START)
        }

        val btnNavigationView = binding.root.findViewById<BottomNavigationView>(R.id.bnvMenu)
        btnNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.btnHome -> {
                    replaceFragment(MenuMainFragment())
                    true
                }

                R.id.btnProviders -> {
                    replaceFragment(ProviderFragment())
                    true
                }
                else -> false
            }
        }

        binding.btnAddProvider.setOnClickListener{
            replaceFragment(AddProviderFragment())
        }

    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentTag = fragment.javaClass.simpleName

        val fragmentTransaction = parentFragmentManager.beginTransaction()

        val existingFragment = parentFragmentManager.findFragmentByTag(fragmentTag)

        if (existingFragment == null) {
            fragmentTransaction.replace(R.id.fcvContent, fragment, fragmentTag)
            fragmentTransaction.commit()
        } else {
            parentFragmentManager.beginTransaction()
                .show(existingFragment)
                .commit()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProviderBinding.inflate(inflater,container,false)
        initListeners()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProviderFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}