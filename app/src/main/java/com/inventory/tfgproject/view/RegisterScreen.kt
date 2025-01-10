package com.inventory.tfgproject.view

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.ActivityRegisterScreenBinding
import com.inventory.tfgproject.extension.loseFocusAfterAction
import com.inventory.tfgproject.viewmodel.AuthViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.inventory.tfgproject.model.FirebaseAuthClient
import com.inventory.tfgproject.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class RegisterScreen : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterScreenBinding
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var auth : FirebaseAuth
    private lateinit var user : User
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        user = User()
        initListener()

        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    if (account != null) {
                        Log.d("GoogleSignIn", "Google account: ${account.displayName}")
                        firebaseAuthWithGoogle(account)
                    } else {
                        Log.e("GoogleSignIn", "Account is null, task failed")
                    }
                } catch (e: ApiException) {
                    Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("GoogleSignIn", "Error: ${e.message}")
                }
            }
        }
    }

    private fun initListener() {
        binding.btnContinueRegister.setOnClickListener {
           val validName = binding.nameRegisterContainer.helperText == null
            val validSurname = binding.surnameRegisterContainer.helperText == null
            val validEmail = binding.emailRegisterContainer.helperText == null
            val validPassword = binding.passwordRegisterContainer.helperText == null
            val validConfirm = binding.confirmPasswordRegisterContainer.helperText == null

            if(validEmail && validPassword && validName && validSurname && validConfirm){
                binding.btnContinueRegister.isEnabled = false
                initRegisterInfo()

                binding.btnContinueRegister.postDelayed({
                    binding.btnContinueRegister.isEnabled=true
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

        binding.imgGoogle.setOnClickListener{
            launchGoogleSignIn()
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

    @SuppressLint("SimpleDateFormat")
    fun actualDate():String{
        val sdf : SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaActual = Date()
        return sdf.format(fechaActual)
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val currentUser = authViewModel.getCurrentUser()

                    val userName = currentUser?.displayName?.split(" ")?.get(0) ?: ""
                    val userSurname = currentUser?.displayName?.split(" ")?.getOrNull(1) ?: ""
                    val userEmail = currentUser?.email.toString()
                    val userPhone = currentUser?.phoneNumber
                    val userAddress = ""
                    val profilePhoto = currentUser?.photoUrl
                    val joinedDate = actualDate()

                    user = User(
                        name = userName,
                        surname = userSurname,
                        email = userEmail,
                        phoneNumber = userPhone,
                        address = userAddress,
                        profilePictureUrl = profilePhoto.toString(),
                        joinedDate = joinedDate
                    )

                    val userId = currentUser?.uid
                    if (userId != null) {
                        val database = FirebaseDatabase.getInstance()
                        val userRef = database.getReference("users").child(userId)

                        userRef.get().addOnSuccessListener { snapshot ->
                            if(!snapshot.exists()){
                                userRef.setValue(user)
                                    .addOnCompleteListener { dbTask ->
                                        if (dbTask.isSuccessful) {
                                            Log.d("Firebase", "User data saved successfully.")
                                            navigateToMainMenu()
                                        } else {
                                            Log.d("Firebase", "Error saving user data.")
                                        }
                                    }
                            } else {
                                Log.d("Firebase Database ","User already exists")
                                navigateToMainMenu()
                            }
                        }
                    }

                } else {
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }



    private fun launchGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .setLogSessionId(true.toString())
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        if (FirebaseAuth.getInstance().currentUser != null) {
            FirebaseAuth.getInstance().signOut()
        }

        googleSignInClient.signOut().addOnCompleteListener {
            googleSignInClient.revokeAccess().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            }
        }
    }

    private fun navigateToMainMenu() {
        startActivity(Intent(this, MainMenu::class.java))
        finish()
    }
}