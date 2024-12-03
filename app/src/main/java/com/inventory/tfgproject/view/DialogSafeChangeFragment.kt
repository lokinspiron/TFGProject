package com.inventory.tfgproject.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.FragmentDialogSafeChangeBinding


class DialogSafeChangeFragment : DialogFragment() {
    private lateinit var binding : FragmentDialogSafeChangeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDialogSafeChangeBinding.inflate(inflater,container,false)
        initListener()
        return binding.root
    }

    private fun initListener() {
        binding.btnCancel.setOnClickListener{
            dismiss()
        }
    }

}