package com.inventory.tfgproject.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.inventory.tfgproject.AnimationUtil
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.ActivityRegisterScreenInfoBinding
import com.inventory.tfgproject.extension.toast
import com.inventory.tfgproject.model.User
import com.inventory.tfgproject.viewmodel.AuthViewModel

class RegisterScreenInfo : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterScreenInfoBinding
    private var user: User? = null
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterScreenInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initListener()
    }

    private fun initListener() {
        user = intent.getParcelableExtra("user")

        binding.btnContinue.setOnClickListener {
            RegisterLoadingScreen()
        }

        binding.edtPhoneRegister.setOnFocusChangeListener{_,focused ->
            if(!focused){
                val phone = binding.edtPhoneRegister.text.toString()
                if(isValidPhone(phone) || phone.isBlank()){
                    binding.phoneRegisterContainer.helperText = null
                }else {
                    binding.phoneRegisterContainer.helperText = "Número no válido"
                }
            }

        }
    }

    private fun isValidPhone(phone: String): Boolean {
        return phone.length in 10..15 && phone.all{it.isDigit()}
    }

    private fun RegisterLoadingScreen(){
        val viewLoading = layoutInflater.inflate(R.layout.activity_register_loading_screen,null,false)

        setContentView(viewLoading)

        val dstockTextView: TextView = viewLoading.findViewById(R.id.txtDStockLoading)
        val logoImageView: ImageView = viewLoading.findViewById(R.id.imgRegisterLoading)

        AnimationUtil.UpAnimation(this, logoImageView, R.anim.rotate_open_anim)
        AnimationUtil.DownAnimation(this, dstockTextView, R.anim.rotate_open_anim)

        authViewModel.sendVerificationEmail()

        authViewModel.isEmailVerified.observe(this, Observer{isVerified ->
            if(isVerified){
             toast("Correo verificado", Toast.LENGTH_SHORT)
                startActivity(Intent(this,MainMenu::class.java))
            }else{
                setContentView(viewLoading)
            }
        })

    }

    override fun onDestroy(){
        super.onDestroy()
        authViewModel.stopVerificationCheck()
    }

}