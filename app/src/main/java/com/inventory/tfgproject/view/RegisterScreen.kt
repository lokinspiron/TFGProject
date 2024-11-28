package com.inventory.tfgproject.view

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.inventory.tfgproject.databinding.ActivityRegisterScreenBinding
import com.inventory.tfgproject.extension.toast
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
        //initEditText()
        initListener()
    }

    private fun initListener() {
        binding.btnContinue.setOnClickListener {
           val validName = binding.nameRegisterContainer.helperText == null
            val validSurname = binding.surnameRegisterContainer.helperText == null
            val validEmail = binding.emailRegisterContainer.helperText == null
            val validPassword = binding.passwordRegisterContainer.helperText == null

            if(validEmail && validPassword && validName && validSurname){
                startActivity(Intent(this,RegisterScreenInfo::class.java))
            }else {
                invalidForm()
            }
        }
        binding.edtNameRegister.setOnFocusChangeListener{_,focused ->
            if(!focused){
                binding.nameRegisterContainer.helperText = validName()
            }
        }
        binding.edtSurnameRegister.setOnFocusChangeListener{_,focused ->
            if(!focused){
                binding.surnameRegisterContainer.helperText = validSurname()
            }

        }
        binding.edtEmailRegister.setOnFocusChangeListener{_,focused ->
            if(!focused){
                binding.emailRegisterContainer.helperText = validEmail()
            }

        }
        binding.edtPasswordsRegister.setOnFocusChangeListener{_,focused ->
            if(!focused){
                binding.surnameRegisterContainer.helperText = validPassword()
            }

        }
        binding.confirmPasswordRegisterContainer.setOnFocusChangeListener{_,focused ->
            if(!focused){
                binding.confirmPasswordRegisterContainer.helperText = validConfirm()
            }
        }
    }

    fun initEditText(){
        binding.nameRegisterContainer.isHelperTextEnabled = false
        binding.surnameRegisterContainer.isHelperTextEnabled = false
        binding.emailRegisterContainer.isHelperTextEnabled = false
        binding.passwordRegisterContainer.isHelperTextEnabled = false
        binding.confirmPasswordRegisterContainer.isHelperTextEnabled = false
    }

    private fun invalidForm() {
        if (binding.edtNameRegister.text.isNullOrEmpty()) {
            binding.nameRegisterContainer.helperText = "Nombre(s) requerido(s)"
        }
        if (binding.edtSurnameRegister.text.isNullOrEmpty()) {
            binding.surnameRegisterContainer.helperText = "Apellido(s) requerido(s)"
        }
        if (binding.edtEmailRegister.text.isNullOrEmpty()) {
            binding.emailRegisterContainer.helperText = "Correo inválido"
        }
        if (binding.edtPasswordsRegister.text.isNullOrEmpty()) {
            binding.passwordRegisterContainer.helperText = "Contraseña requerida"
        }
        if (binding.edtConfirmPasswordRegister.text.isNullOrEmpty()) {
            binding.confirmPasswordRegisterContainer.helperText = "Confirmar contraseña requerida"
        }
    }

    private fun validEmail(): String? {
        val email = binding.edtEmailRegister.text.toString().trim()
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            return "Introduce un correo válido"
        }
        return null
    }

    private fun validConfirm():String?{
        val password = binding.edtPasswordsRegister.text.toString()
        val confirmPassword = binding.edtConfirmPasswordRegister.text.toString()

        if (password != confirmPassword) {
            binding.confirmPasswordRegisterContainer.helperText = "Las contraseñas no coinciden"
        }
        return null
    }

    private fun validSurname(): String? {
        val surname = binding.edtSurnameRegister.text.toString()
        if(!surname.contains(Regex("[A-Z]"))){
            return "El apellido debe tener por lo menos una mayúscula"
        }
        return null
    }

    private fun validName(): String? {
        val name = binding.edtNameRegister.text.toString()
        if(!name.contains(Regex("[A-Z]"))){
            return "El nombre debe tener por lo menos una mayúscula"
        }

        return null
    }

    private fun validPassword(): String? {
        val password = binding.edtPasswordsRegister.text.toString()
        if(!password.contains(Regex("[A-Z](?=.*\\\\d)"))){
            return "La contraseña debe tener por lo menos una mayúscula y un numero"
        }
        return null
    }
}