package com.inventory.tfgproject.model

import java.lang.System.currentTimeMillis

data class Orders(
    val id: String = "",
    val fechaPedido: Long = currentTimeMillis(),
    val estado: String = "",
    val proveedorId: String = ""
)
