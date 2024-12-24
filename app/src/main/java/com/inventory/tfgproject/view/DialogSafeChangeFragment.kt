package com.inventory.tfgproject.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.FragmentDialogSafeChangeBinding
import com.inventory.tfgproject.model.FirebaseAuthClient

class DialogSafeChangeFragment : DialogFragment() {
    private lateinit var binding : FragmentDialogSafeChangeBinding
    private lateinit var firebaseAuthClient: FirebaseAuthClient
    var onDoItClick: (() -> Unit)? = null

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
        binding = FragmentDialogSafeChangeBinding.inflate(inflater,container,false)
        initListener()
        return binding.root
    }

    private fun initListener() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dynamicText = arguments?.getString(ARG_DYNAMIC_TEXT) ?: ""
        val doItText = arguments?.getString(ARG_DO_IT_TEXT) ?: ""

        view.findViewById<TextView>(R.id.txtDynamic).text = dynamicText
        view.findViewById<Button>(R.id.btnDoIt).text = doItText

        binding.btnCancel.setOnClickListener{
            dismiss()
        }

        view.findViewById<Button>(R.id.btnDoIt).setOnClickListener {
            logoutAndNavigate()
        }

        binding.btnDoIt.setOnClickListener{
            onDoItClick?.invoke()
            dismiss()
        }

    }

    private fun logoutAndNavigate() {
        firebaseAuthClient.logout()

        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        dismiss()
    }

}