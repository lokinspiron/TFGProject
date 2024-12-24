package com.inventory.tfgproject.model

data class Product(
    val id: String = "",
    var name: String = "",
    var stock: Int = 0,
    var price: Double = 0.0,
    var currencyUnit: String = "EUR",
    var weight: Double = 0.0,
    var weightUnit: String = "kg",
    var categoryId: String = "",
    var providerId: String = "",
    var subcategoryId: String = "",
    var imageUrl: String? = null
)