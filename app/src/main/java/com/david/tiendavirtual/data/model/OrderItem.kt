package com.david.tiendavirtual.data.model

data class OrderItem(
    val nombre:         String,
    val precioUnitario: Double,
    val cantidad:       Int
) {
    fun subtotal(): Double = precioUnitario * cantidad
}

