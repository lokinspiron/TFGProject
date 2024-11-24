package com.inventory.tfgproject.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.inventory.tfgproject.databinding.ActivityRegisterScreenInfoBinding
import com.inventory.tfgproject.model.User

class RegisterScreenInfo : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterScreenInfoBinding
    private var user: User? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterScreenInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initListener()
    }

    private fun initListener() {
        user = intent.getParcelableExtra("user")

        binding.btnContinue.setOnClickListener {
            val birthDate = binding.edtBirthday.text.toString()
            val phoneNumber = binding.edtPhone.text.toString()
            val address = binding.edtAddress.text.toString()

            user?.copy(
                birthDate = birthDate.takeIf { it.isNotEmpty() },
                phoneNumber = phoneNumber.takeIf { it.isNotEmpty() },
                address = address.takeIf { it.isNotEmpty() }
            )
        }
    }
}