package com.inventory.tfgproject.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast.LENGTH_SHORT
import androidx.fragment.app.DialogFragment
import com.inventory.tfgproject.databinding.FragmentDialogContactProviderBinding
import com.inventory.tfgproject.extension.toast
import com.inventory.tfgproject.model.Providers

class DialogContactProviderFragment: DialogFragment() {
    private lateinit var binding : FragmentDialogContactProviderBinding
    private var provider: Providers? = null

    companion object {
        fun newInstance(provider: Providers): DialogContactProviderFragment {
            return DialogContactProviderFragment().apply {
                this.provider = provider
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDialogContactProviderBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        setupGmailButton()
        setupPhoneButton()
        setupWhatsAppButton()
    }

    private fun initListeners() {

        binding.imgClose.setOnClickListener{
            dismiss()
        }
    }

    private fun setupGmailButton() {
        binding.btnGmail.setOnClickListener {
            if (provider?.email.isNullOrEmpty()) {
                toast("No hay correo electrónico registrado para este proveedor", LENGTH_SHORT)
                return@setOnClickListener
            }

            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${provider?.email}")
            }

            try {
                startActivity(intent)
            } catch (e: Exception) {
                toast("No se encontró aplicación de correo", LENGTH_SHORT)
            }
        }
    }

    private fun setupPhoneButton() {
        binding.btnPhone.setOnClickListener {
            if (provider?.phoneNumber.isNullOrEmpty()) {
                toast("No hay número de teléfono registrado para este proveedor", LENGTH_SHORT)
                return@setOnClickListener
            }

            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${provider?.phoneNumber}")
            }

            try {
                startActivity(intent)
            } catch (e: Exception) {
                toast("No se pudo iniciar la aplicación de teléfono", LENGTH_SHORT)
            }
        }
    }

    private fun setupWhatsAppButton() {
        binding.btnWhatsapp.setOnClickListener {
            if (provider?.phoneNumber.isNullOrEmpty()) {
                toast("No hay número de teléfono registrado para este proveedor", LENGTH_SHORT)
                return@setOnClickListener
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?phone=${provider?.phoneNumber}")
            }

            try {
                startActivity(intent)
            } catch (e: Exception) {
                toast("No se encontró WhatsApp instalado", LENGTH_SHORT)
            }
        }
    }
}
