package com.inventory.tfgproject.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.FragmentInventoryMenuBinding


class InventoryMenuFragment : Fragment() {
    private var _binding: FragmentInventoryMenuBinding? = null
    private val binding get() = _binding!!

    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.rotate_open_anim
        )
    }
    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.rotate_close_anim
        )
    }
    private val fromBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.from_bottom_anim
        )
    }
    private val toBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.to_bottom_anim
        )
    }

    private var clicked = false

    private var categoryId: String? = null
    private var categoryName: String? = null

    companion object {
        fun newInstanceForCategory(
            categoryId: String,
            categoryName: String
        ): InventoryMenuFragment {
            val fragment = InventoryMenuFragment()
            val args = Bundle()
            args.putString("category_id", categoryId)
            args.putString("category_name", categoryName)
            args.putBoolean("is_category", true)
            fragment.arguments = args
            return fragment
        }

        fun newInstanceForSubcategory(
            subcategoryId: String?,
            subcategoryName: String?
        ): InventoryMenuFragment {
            val fragment = InventoryMenuFragment()
            val args = Bundle()
            args.putString("subcategory_id", subcategoryId)
            args.putString("subcategory_name", subcategoryName)
            fragment.arguments = args
            return fragment
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInventoryMenuBinding.inflate(inflater, container, false)
        initListener()
        return binding.root
    }

    private fun initListener() {
        binding.fabProducts.setOnClickListener {
            onAddButtonClicked()
        }
        binding.fabAddProducts.setOnClickListener {
            (activity as? MainMenu)?.replaceFragment(AddProductFragment(), " Añadir Producto")

        }
        binding.fabEditProducts.setOnClickListener {

        }
    }

    private fun onAddButtonClicked() {
        setVisibility(clicked)
        setAnimation(clicked)
        setClickable(clicked)
        clicked = !clicked
    }

    private fun setAnimation(clicked: Boolean) {
        if (!clicked) {
            binding.fabEditProducts.startAnimation(fromBottom)
            binding.fabAddProducts.startAnimation(fromBottom)
            binding.fabProducts.startAnimation(rotateOpen)
        } else {
            binding.fabEditProducts.startAnimation(toBottom)
            binding.fabAddProducts.startAnimation(toBottom)
            binding.fabProducts.startAnimation(rotateClose)
        }

    }

    private fun setVisibility(clicked: Boolean) {
        if (!clicked) {
            binding.fabAddProducts.visibility = View.VISIBLE
            binding.fabEditProducts.visibility = View.VISIBLE
        } else {
            binding.fabAddProducts.visibility = View.INVISIBLE
            binding.fabEditProducts.visibility = View.INVISIBLE
        }
    }

    private fun setClickable(clicked: Boolean) {
        if (!clicked) {
            binding.fabEditProducts.isClickable = true
            binding.fabAddProducts.isClickable = true
        } else {
            binding.fabEditProducts.isClickable = false
            binding.fabAddProducts.isClickable = false
        }
    }
}