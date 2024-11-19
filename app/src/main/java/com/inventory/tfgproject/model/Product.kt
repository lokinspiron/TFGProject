package com.inventory.tfgproject.model

data class Product(
    val name:String = "",
    val stock: Int = 0,
    val price: Double = 0.0,
    val weight: Double = 0.0,
    val categoryId:String = "",
    val providerId:String = "",
    val subcategoryId:String = "",
)
