package com.andres.joyeria.ui.screens

data class ActualizarProductoRequest(
    val sku: String,
    val nombre: String,
    val descripcion: String?,
    val material: String,
    val peso_gr: Double?,
    val talla_medida: String?,
    val precio_costo: Double,
    val precio_venta: Double,
    val status: Boolean,
    val id_categoria: Int
)