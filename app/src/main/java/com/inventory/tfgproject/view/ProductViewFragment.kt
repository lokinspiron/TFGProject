package com.inventory.tfgproject.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.FragmentInventoryBinding
import com.inventory.tfgproject.databinding.FragmentProductViewBinding


class ProductViewFragment : Fragment() {

    private var _binding: FragmentProductViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProductViewBinding.inflate(inflater,container,false)
        return binding.root
    }

    private fun initListeners(){


    }

    private fun initViewModel(){

    }
}