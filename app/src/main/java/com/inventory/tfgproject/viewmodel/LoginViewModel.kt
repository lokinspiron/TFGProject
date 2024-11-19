package com.inventory.tfgproject.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel

class LoginViewModel: ViewModel() {


    private fun isValidEmail(email : String) = Patterns.EMAIL_ADDRESS.matcher(email).matches() || email.isEmpty()

    private fun isValidPassword(password : String): Boolean = password.length > 6 || password.isEmpty()

}