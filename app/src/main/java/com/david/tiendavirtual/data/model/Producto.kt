package com.david.tiendavirtual.data.model

import java.io.Serializable

data class Producto(
    val idProducto: Long,
    val nombre: String,
    val unidad: String,
    val descripcion: String,
    val stock: Int,
    val precioCosto: Double,
    val precioVenta: Double,
    val imagen: String?,
    val idCategoria: Int,
    val idMarca: Int,
    val categoria: String? = null,
    val marca: String? = null
) : Serializable
