package com.inventory.tfgproject.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.FragmentDialogCreateCategoryBinding
import com.inventory.tfgproject.databinding.FragmentInventoryBinding
import com.inventory.tfgproject.viewmodel.CategoryViewModel


class DialogCreateCategoryFragment : DialogFragment() {

    private var _binding: FragmentDialogCreateCategoryBinding? = null
    private val binding get() = _binding!!

    private val categoryViewModel : CategoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDialogCreateCategoryBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
    }

    fun initListener(){
        binding.btnAddCategory.setOnClickListener{

        }
    }



}