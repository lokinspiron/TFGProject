package com.inventory.tfgproject.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.inventory.tfgproject.databinding.ActivityRegisterScreenBinding
import com.inventory.tfgproject.model.FirebaseAuthClient
import com.inventory.tfgproject.model.User

class RegisterScreen : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterScreenBinding
    private lateinit var authClient: FirebaseAuthClient
    private lateinit var db: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initListener()
    }

    private fun initListener() {
        binding.btnContinue.setOnClickListener {

            val password = binding.edtPassword.text.toString()
            val confPassword = binding.edtPasswordConfirm.text.toString()

            /*if (name.isNotEmpty() && surname.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confPassword.isNotEmpty()) {
                val user = User(name = name, surname = surname, email = email)

                authClient.registerUser(email, password) { isSuccessful ->
                    if (isSuccessful) {
                        Toast.makeText(this, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, RegisterScreenInfo::class.java)
                        intent.putExtra("user", user)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Error al registrar el usuario", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT)
                    .show()
            }*/
        }
    }
}