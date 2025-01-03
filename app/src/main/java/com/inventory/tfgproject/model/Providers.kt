package com.inventory.tfgproject.model

data class Providers(
    val id:String = "",
    val name:String = "",
    val address: String? = null,
    val email:String? = null,
    val phoneNumber: String? = null,
    val imageUrl: String? = null
){
    override fun toString(): String {
        return "Provider(id=$id, name=$name, email=$email, phone=$phoneNumber)"
    }
}
