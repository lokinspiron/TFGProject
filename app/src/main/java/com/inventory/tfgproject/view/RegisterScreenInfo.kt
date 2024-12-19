package com.inventory.tfgproject.view

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.datepicker.MaterialDatePicker
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
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var authClient: FirebaseAuthClient
    private lateinit var db: FirebaseDatabaseClient
    private lateinit var user : User

    private var profilePictureUrl : String? = null

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uploadProfilePicture(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterScreenInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        authClient = FirebaseAuthClient()
        db = FirebaseDatabaseClient()
        user = User()
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
                    user = user.copy(birthDate = birthDateString)
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

        registerNewUserAuth(viewLoading)

    }

    private fun emailVerification(viewLoading: View){
        authViewModel.isEmailVerified.observe(this, Observer{isVerified ->
            if(isVerified){
                authViewModel.stopVerificationCheck()
                toast("Correo verificado", Toast.LENGTH_SHORT)
                startActivity(Intent(this,MainMenu::class.java))
            }else{
                toast("Error al verificar el correo",Toast.LENGTH_SHORT)
            }
        })

    }

    private fun registerNewUserAuth(viewLoading: View) {
        authViewModel.authStatus.observe(this,Observer{ isSuccessful ->
            if(isSuccessful){
                Log.d("Autenticacion de usuario","Usuario autenticado : {currentUser?.email}")
                authViewModel.sendVerificationEmail()
                emailVerification(viewLoading)
            }else{
                Toast.makeText(this,"Error de autenticacion",Toast.LENGTH_SHORT).show()
            }
        })
        registerUserDB()
    }

    private fun registerUserDB() {

        val userName = intent.getStringExtra("Username") ?: ""
        val userSurname = intent.getStringExtra("Surname") ?: ""
        val userEmail = intent.getStringExtra("Email") ?: ""
        val userPassword = intent.getStringExtra("Password")?:""
        val userPhone = binding.edtPhoneRegister.text.toString().trim()
        val userAddress = binding.edtAddressRegister.text.toString().trim()
        val userJoinedDate = actualDate()

        user = User(
            name = userName,
            surname = userSurname,
            email = userEmail,
            phoneNumber = userPhone,
            address = userAddress,
            profilePictureUrl = profilePictureUrl,
            joinedDate = userJoinedDate
        )

        authViewModel.createUser(userEmail,userPassword,user)
        toast("Se ha agregado a base de datos", Toast.LENGTH_SHORT)
        }

    private fun uploadProfilePicture(uri: Uri?) {
        if (uri == null) {
            profilePictureUrl = "https://firebasestorage.googleapis.com/v0/b/d-stock-01.firebasestorage.app/o/profile_pictures%2Fic_user_image.png?alt=media&token=3593949b-d6e4-420b-914d-c95eea68c7c3"
            Log.d("Firebase", "Usando imagen de perfil predeterminada: $profilePictureUrl")
            toast("Imagen de perfil predeterminada", Toast.LENGTH_SHORT)
            return
        }

        val storageRef = FirebaseStorage.getInstance().reference
        val profilePictureRef = storageRef.child("profile_pictures/${UUID.randomUUID()}.jpg")

        profilePictureRef.putFile(uri)
            .addOnSuccessListener {
                profilePictureRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    profilePictureUrl = downloadUri.toString()

                    Log.d("Firebase", "Foto subida correctamente: $profilePictureUrl")
                    toast("Foto de perfil subida con éxito", Toast.LENGTH_SHORT)
                }
            }
            .addOnFailureListener { exception ->
                profilePictureUrl = "https://firebasestorage.googleapis.com/v0/b/d-stock-01.firebasestorage.app/o/profile_pictures%2Fic_user_image.png?alt=media&token=3593949b-d6e4-420b-914d-c95eea68c7c3"
                toast("Error al subir la foto. Usando imagen predeterminada", Toast.LENGTH_SHORT)
                Log.e("Firebase", "Error uploading profile picture", exception)
            }
    }

    @SuppressLint("SimpleDateFormat")
    fun actualDate():String{
        val sdf : SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaActual = Date()
        return sdf.format(fechaActual)
    }

    override fun onDestroy(){
        super.onDestroy()
        authViewModel.stopVerificationCheck()
    }
}
