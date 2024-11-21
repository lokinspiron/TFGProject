package com.inventory.tfgproject.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.inventory.tfgproject.databinding.DialogCreateCategoryBinding

class CreateCategoryDialogFragment : DialogFragment() {
    private lateinit var binding : DialogCreateCategoryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()

    }

    private fun initListeners() {
        binding.imgClose.setOnClickListener{
            dismiss()
        }
    }
}