package com.inventory.tfgproject.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.inventory.tfgproject.databinding.FragmentDialogCreateOrderBinding

class DialogContactProviderFragment: DialogFragment() {
    private lateinit var binding : DialogContactProviderFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()

    }

    private fun initListeners() {

    }
}