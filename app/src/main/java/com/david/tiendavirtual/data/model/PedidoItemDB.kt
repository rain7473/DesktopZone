package com.david.tiendavirtual.data.model

import java.io.Serializable

data class PedidoItemDB(
    val idFacDet:       Int,
    val idProducto:     Long,
    val cantidad:       Int,
    val precioUnitario: Double,
    val nombre:         String
) : Serializable {
    fun subtotal(): Double = precioUnitario * cantidad
}

