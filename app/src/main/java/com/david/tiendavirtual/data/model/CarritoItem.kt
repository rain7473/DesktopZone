package com.david.tiendavirtual.data.model

data class CarritoItem(
    val producto: Producto,
    var cantidad: Int = 1
) {
    fun subtotal(): Double = producto.precioVenta * cantidad
}
