package com.inventory.tfgproject.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.inventory.tfgproject.databinding.FragmentDialogCreateCategoryBinding
import com.inventory.tfgproject.model.Subcategory
import com.inventory.tfgproject.viewmodel.CategoryViewModel
import java.util.UUID


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
    ): View {
        _binding = FragmentDialogCreateCategoryBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
        initEditText()
    }

    private fun initListener(){
        binding.btnAddCategory.setOnClickListener {
            val nameCategory = binding.edtNameCategory.text.toString().trim()
            val nameSubcategory = binding.edtNameSubcategory.text.toString().trim()

            if (nameCategory.isNotEmpty()) {
                val subcategory = if (nameSubcategory.isNotEmpty()) {
                    val subcategory = Subcategory(
                        id = UUID.randomUUID().toString(),
                        name = nameSubcategory
                    )
                    Log.d("DialogCreateCategoryFragment", "Subcategoría creada: $subcategory")
                    subcategory
                } else null

                categoryViewModel.addCategory(nameCategory, subcategory)

                binding.edtNameCategory.text?.clear()
                binding.edtNameSubcategory.text?.clear()

                dismiss()
            } else {
                binding.tilNameCategory.helperText = "El nombre de la categoría es obligatorio"
            }
        }

        binding.imgClose.setOnClickListener{
            dismiss()
        }
    }

    private fun initEditText(){
        binding.tilNameCategory.helperText = null
    }
}