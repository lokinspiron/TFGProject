package com.inventory.tfgproject.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.FragmentDialogSafeChangeBinding
import com.inventory.tfgproject.model.FirebaseAuthClient
import com.inventory.tfgproject.viewmodel.UserViewModel

class DialogSafeChangeFragment : DialogFragment() {
    private lateinit var binding: FragmentDialogSafeChangeBinding
    private lateinit var firebaseAuthClient: FirebaseAuthClient
    var onDoItClick: (() -> Unit)? = null

    private var onDismissListener: (() -> Unit)? = null


    companion object {
        private const val ARG_DYNAMIC_TEXT = "arg_dynamic_text"
        private const val ARG_DO_IT_TEXT = "arg_do_it_text"

        fun newInstance(dynamicText: String, doItText: String): DialogSafeChangeFragment {
            val fragment = DialogSafeChangeFragment()
            val args = Bundle()
            args.putString(ARG_DYNAMIC_TEXT, dynamicText)
            args.putString(ARG_DO_IT_TEXT, doItText)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuthClient = FirebaseAuthClient()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDialogSafeChangeBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun setOnPositiveClickListener(listener: () -> Unit) {
        onDoItClick = listener
    }

    fun setOnDismissListener(listener: () -> Unit) {
        onDismissListener = listener
    }

    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dynamicText = arguments?.getString(ARG_DYNAMIC_TEXT) ?: ""
        val doItText = arguments?.getString(ARG_DO_IT_TEXT) ?: ""

        binding.txtDynamic.text = dynamicText
        binding.btnDoIt.text = doItText

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        view.findViewById<Button>(R.id.btnDoIt).setOnClickListener {
            logoutAndNavigate()
        }

        binding.btnDoIt.setOnClickListener {
            if (doItText == "Cerrar Sesión") {
                logoutAndNavigate()
            } else {
                onDoItClick?.invoke()
                dismiss()
            }
        }

    }

    private fun logoutAndNavigate() {
        try {
            val userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
            userViewModel.clearUserData()

            firebaseAuthClient.logout()

            requireActivity().run {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                dismiss()
            }
        } catch (e: Exception) {
            Log.e("DialogSafeChange", "Error during logout", e)
            requireActivity().run {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                dismiss()
            }
        }
    }
}