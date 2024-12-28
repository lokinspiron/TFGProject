package com.inventory.tfgproject.model


data class Orders(
    val id: String = "",
    val fechaPedido: String = "",
    val cantidad : Int = 0,
    val estado: String = "",
    val proveedorId: String = "",
    val productId: String = ""
)
