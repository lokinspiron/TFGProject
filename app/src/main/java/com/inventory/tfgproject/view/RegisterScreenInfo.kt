package com.inventory.tfgproject.view

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.inventory.tfgproject.AnimationUtil
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.ActivityRegisterScreenInfoBinding
import com.inventory.tfgproject.extension.loseFocusAfterAction
import com.inventory.tfgproject.extension.toast
import com.inventory.tfgproject.model.FirebaseAuthClient
import com.inventory.tfgproject.model.FirebaseDatabaseClient
import com.inventory.tfgproject.model.User
import com.inventory.tfgproject.viewmodel.AuthViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class RegisterScreenInfo : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterScreenInfoBinding
    private var user: User? = null
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var authClient: FirebaseAuthClient
    private lateinit var db: FirebaseDatabaseClient

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()){ uri ->
        if(uri!=null){
            toast("Foto de perfil seleccionada",Toast.LENGTH_SHORT)
            uploadProfilePicture(uri)
        }else{
            toast("Error al elegir foto de perfil",Toast.LENGTH_SHORT)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterScreenInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        authClient = FirebaseAuthClient()
        db = FirebaseDatabaseClient()
        initListener()
    }

    private fun initListener() {

        binding.phoneRegisterContainer.helperText = null
        binding.btnContinue.setOnClickListener {
            registerLoadingScreen()

        }
        binding.edtPhoneRegister.loseFocusAfterAction(EditorInfo.IME_ACTION_DONE)
        binding.edtPhoneRegister.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                val phone = binding.edtPhoneRegister.text.toString().trim()
                if (phone.isBlank() || isValidPhone(phone)) {
                    binding.phoneRegisterContainer.helperText = null
                } else {
                    binding.phoneRegisterContainer.helperText = "Número no válido"
                }
            }
        }

        binding.btnBirthdate.setOnClickListener {
            val dateBirth = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecciona tu fecha de nacimiento")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            dateBirth.show(supportFragmentManager, "DatePicker")

            dateBirth.addOnPositiveButtonClickListener { selection ->
                val selectedDate = java.util.Calendar.getInstance()
                selectedDate.timeInMillis = selection

                val today = java.util.Calendar.getInstance()

                var age = today.get(java.util.Calendar.YEAR) - selectedDate.get(java.util.Calendar.YEAR)

                if (today.get(java.util.Calendar.DAY_OF_YEAR) < selectedDate.get(java.util.Calendar.DAY_OF_YEAR)) {
                    age--
                }
                if (age >= 18) {
                    toast("Edad válida",Toast.LENGTH_SHORT)
                    val birthDateString = selectedDate.time.toString()
                    user = user?.copy(birthDate = birthDateString)
                } else {
                    toast("Debes ser mayor de 18 años",Toast.LENGTH_SHORT)
                }
            }
        }

        binding.btnImgRegister.setOnClickListener{
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnCancel.setOnClickListener{
            finish()
        }
    }

    private fun isValidPhone(phone: String): Boolean {
        return phone.length in 9..15 && phone.all{it.isDigit()}
    }

    private fun registerLoadingScreen(){
        val viewLoading = layoutInflater.inflate(R.layout.activity_register_loading_screen,null,false)

        setContentView(viewLoading)

        val dstockTextView: TextView = viewLoading.findViewById(R.id.txtDStockLoading)
        val logoImageView: ImageView = viewLoading.findViewById(R.id.imgRegisterLoading)

        if (!AnimationUtil.isAnimationDone) {
            AnimationUtil.upAnimation(this, logoImageView, R.anim.movement_down)
            AnimationUtil.downAnimation(this, dstockTextView, R.anim.movement_down)

            AnimationUtil.isAnimationDone = true
        }

        registerNewUserAuth()

        authViewModel.isEmailVerified.observe(this, Observer{isVerified ->
            if(isVerified){
                authViewModel.stopVerificationCheck()
                registerUserDB()
                toast("Correo verificado", Toast.LENGTH_SHORT)
                startActivity(Intent(this,MainMenu::class.java))
            }else{
                setContentView(viewLoading)
            }
        })
        authViewModel.startVerificationCheck()
    }

    private fun registerNewUserAuth() {
        val userEmail = intent.getStringExtra("Email")
        val userPassword = intent.getStringExtra("Password")

        if (userEmail != null && userPassword != null) {
            authClient.registerUser(userEmail,userPassword){registered ->
                if(registered){
                    authViewModel.sendVerificationEmail()
                }else{
                    toast("El correo ya existe")
                }
            }
        }
    }

    private fun registerUserDB(){
        val userName = intent.getStringExtra("Username")?:""
        val userSurname = intent.getStringExtra("Surname")?:""
        val userEmail = intent.getStringExtra("Email")?:""
        val userBirthDate = user?.birthDate
        val userPhone = binding.edtPhoneRegister.text.toString().trim()
        val userAddress = binding.edtAddressRegister.text.toString().trim()
        val userProfilePhoto = user?.profilePictureUrl
        val userJoinedDate = ActualDate()

        val newUser = User(
            name = userName,
            surname = userSurname,
            email = userEmail,
            birthDate = userBirthDate,
            phoneNumber = userPhone,
            address = userAddress,
            profilePictureUrl = userProfilePhoto,
            joinedDate = userJoinedDate
        )

        val currentUser = FirebaseAuth.getInstance().currentUser
        if(currentUser != null){
            db.saveUserData(currentUser.uid,newUser)
            toast("Se ha agregado a base de datos",Toast.LENGTH_SHORT)
        }
    }

    private fun uploadProfilePicture(uri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference
        val profilePictureRef = storageRef.child("profile_pictures/${UUID.randomUUID()}.jpg")

        profilePictureRef.putFile(uri)
            .addOnSuccessListener {
                profilePictureRef.downloadUrl.addOnSuccessListener { uri ->
                    val profilePictureUrl = uri.toString()
                    user = user?.copy(profilePictureUrl = profilePictureUrl)
                    toast("Foto de perfil subida con éxito",Toast.LENGTH_SHORT)
                }
            }
            .addOnFailureListener { exception ->
                toast("Error al subir la foto de perfil",Toast.LENGTH_SHORT)
                Log.e("Firebase", "Error uploading profile picture", exception)
            }
    }
    @SuppressLint("SimpleDateFormat")
    fun ActualDate():String{
        val sdf : SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaActual = Date()
        return sdf.format(fechaActual)
    }

    override fun onDestroy(){
        super.onDestroy()
        authViewModel.stopVerificationCheck()
    }
}
