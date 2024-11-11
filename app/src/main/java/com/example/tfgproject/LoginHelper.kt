package com.example.tfgproject

import android.content.Context
import android.widget.Toast

class LoginHelper(private val context: Context) {
    fun showLoginSuccessToast() {
        Toast.makeText(context,"Login Exitoso",Toast.LENGTH_SHORT).show()
    }
}