package com.inventory.tfgproject.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.FragmentReauthenticationDialogBinding
import com.inventory.tfgproject.model.FirebaseAuthClient


class ReauthenticateDialogFragment : DialogFragment() {
    private var _binding: FragmentReauthenticationDialogBinding? = null
    private val binding get() = _binding!!

    var onAuthSuccess: (() -> Unit)? = null
    private val firebaseAuthClient = FirebaseAuthClient()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReauthenticationDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnConfirm.setOnClickListener {
            val password = binding.edtPassword.text.toString()
            if (password.isNotEmpty()) {
                firebaseAuthClient.reauthenticate(password) { success ->
                    if (success) {
                        onAuthSuccess?.invoke()
                        dismiss()
                    } else {
                        binding.edtPassword.error = "Contraseña incorrecta"
                    }
                }
            } else {
                binding.edtPassword.error = "Introduce tu contraseña"
            }
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = ReauthenticateDialogFragment()
    }
}