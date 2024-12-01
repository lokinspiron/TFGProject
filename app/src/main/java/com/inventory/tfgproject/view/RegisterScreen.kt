package com.inventory.tfgproject.view

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.firebase.database.FirebaseDatabase
import com.inventory.tfgproject.databinding.ActivityRegisterScreenBinding
import com.inventory.tfgproject.extension.loseFocusAfterAction
import com.inventory.tfgproject.extension.toast
import com.inventory.tfgproject.model.FirebaseAuthClient
import com.inventory.tfgproject.model.User

class RegisterScreen : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterScreenBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initListener()
    }

    private fun initListener() {
        binding.btnContinue.setOnClickListener {
           val validName = binding.nameRegisterContainer.helperText == null
            val validSurname = binding.surnameRegisterContainer.helperText == null
            val validEmail = binding.emailRegisterContainer.helperText == null
            val validPassword = binding.passwordRegisterContainer.helperText == null
            val validConfirm = binding.confirmPasswordRegisterContainer.helperText == null

            if(validEmail && validPassword && validName && validSurname && validConfirm){
                binding.btnContinue.isEnabled = false
                initRegisterInfo()

                binding.btnContinue.postDelayed({
                    binding.btnContinue.isEnabled=true
                },2000)
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
                binding.passwordRegisterContainer.helperText = validPassword()
            }

        }
        binding.edtConfirmPasswordRegister.loseFocusAfterAction(EditorInfo.IME_ACTION_DONE)
        binding.edtConfirmPasswordRegister.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                binding.confirmPasswordRegisterContainer.helperText = validConfirm()
            }
        }
    }

    private fun initRegisterInfo() {
        val nombre = binding.edtNameRegister.text.toString().trim()
        val apellido = binding.edtSurnameRegister.text.toString().trim()
        val correo = binding.edtEmailRegister.text.toString().trim()
        val contrasena = binding.edtPasswordsRegister.text.toString().trim()

        val intent = Intent(this,RegisterScreenInfo::class.java)

        intent.putExtra("Username", nombre)
        intent.putExtra("Surname",apellido)
        intent.putExtra("Email",correo)
        intent.putExtra("Password",contrasena)

        startActivity(intent)
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
        val password = binding.edtPasswordsRegister.text.toString().trim()
        val confirmPassword = binding.edtConfirmPasswordRegister.text.toString().trim()

        if (password.isEmpty() || confirmPassword.isEmpty()) {
            return "La contraseña no puede estar vacía"
        }
        if (password != confirmPassword) {
            return "Las contraseñas no coinciden"
        }
        return null
    }

    private fun validSurname(): String? {
        val surname = binding.edtSurnameRegister.text.toString().trim()
        if(!surname.contains(Regex("[A-Z]"))){
            return "Debe contener por lo menos una mayúscula"
        }
        return null
    }

    private fun validName(): String? {
        val name = binding.edtNameRegister.text.toString().trim()
        if(!name.contains(Regex("[A-Z]"))){
            return "Debe contener por lo menos una mayúscula"
        }

        return null
    }

    private fun validPassword(): String? {
        val password = binding.edtPasswordsRegister.text.toString().trim()
        if (!password.contains(Regex("(?=.*[A-Z])(?=.*\\d)"))) {
            return "Incluye al menos una mayúscula y número"
        }
        return null
    }
}