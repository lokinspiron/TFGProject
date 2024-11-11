package com.example.tfgproject.model

data class User(
    val name:String = "",
    val surname:String = "",
    val email:String = "",
    val birthDate: String? = null,
    val phoneNumber: String? = null,
    val address: String? = null,
    val profilePictureUrl: String? = null,
    val joinedDate:Long = System.currentTimeMillis()) {}