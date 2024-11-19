package com.inventory.tfgproject.model

import java.lang.System.currentTimeMillis

data class Orders(
    val fechaPedido: Long = currentTimeMillis(),
    val estado: String = "",
    val proveedorId: String = ""
)
