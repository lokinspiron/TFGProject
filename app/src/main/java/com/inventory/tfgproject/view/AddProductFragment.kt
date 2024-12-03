package com.inventory.tfgproject.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.FragmentAddProductBinding
import com.inventory.tfgproject.databinding.FragmentInventoryBinding


class AddProductFragment : Fragment() {

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddProductBinding.inflate(inflater,container,false)
        return binding.root
    }

    private fun updateTextView() {
        val txtView = requireActivity().findViewById<TextView>(R.id.txtWave)
        val txtAddProduct = getString(R.string.flbtn_add_product)
        txtView.text = txtAddProduct
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateTextView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}