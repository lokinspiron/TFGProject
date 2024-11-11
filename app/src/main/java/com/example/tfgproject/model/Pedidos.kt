package com.example.tfgproject.model

import java.lang.System.currentTimeMillis

data class Pedidos(
    val fechaPedido: Long = currentTimeMillis(),
    val estado: String = "",
    val proveedorId: String = ""
)
