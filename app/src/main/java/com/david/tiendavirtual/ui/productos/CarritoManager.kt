package com.david.tiendavirtual.ui.productos

import com.david.tiendavirtual.data.model.CarritoItem
import com.david.tiendavirtual.data.model.Producto

object CarritoManager {
    private val items = mutableListOf<CarritoItem>()

    fun agregarProducto(producto: Producto, cantidad: Int = 1) {
        val existente = items.find { it.producto.idProducto == producto.idProducto }
        if (existente != null) {
            existente.cantidad += cantidad
        } else {
            items.add(CarritoItem(producto, cantidad))
        }
    }

    fun obtenerItems(): List<CarritoItem> = items.toList()

    fun obtenerCantidad(idProducto: Long): Int =
        items.find { it.producto.idProducto == idProducto }?.cantidad ?: 0

    fun eliminar(idProducto: Long) {
        items.removeAll { it.producto.idProducto == idProducto }
    }

    fun incrementar(idProducto: Long) {
        items.find { it.producto.idProducto == idProducto }?.cantidad += 1
    }

    fun decrementar(idProducto: Long) {
        val item = items.find { it.producto.idProducto == idProducto } ?: return
        if (item.cantidad > 1) item.cantidad -= 1 else eliminar(idProducto)
    }

    fun vaciar() = items.clear()

    fun calcularSubtotal(): Double = items.sumOf { it.subtotal() }

    fun calcularItbms(): Double = calcularSubtotal() * 0.07

    fun calcularTotal(): Double = calcularSubtotal() + calcularItbms()

    fun totalItems(): Int = items.sumOf { it.cantidad }
}
