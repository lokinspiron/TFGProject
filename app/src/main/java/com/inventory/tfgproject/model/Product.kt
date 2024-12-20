package com.inventory.tfgproject.model

data class Product(
    val id: String = "",
    val name: String = "",
    var stock: Int = 0,
    val price: Double = 0.0,
    val currencyUnit: String = "EUR",
    val weight: Double = 0.0,
    val weightUnit: String = "kg",
    val categoryId: String = "",
    val providerId: String = "",
    val subcategoryId: String = "",
    val imageUrl: String? = null
)