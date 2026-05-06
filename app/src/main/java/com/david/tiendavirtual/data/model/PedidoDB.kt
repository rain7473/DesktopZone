package com.david.tiendavirtual.data.model

import java.io.Serializable

data class PedidoDB(
    val idFactura: Int,
    val fecha:     String,
    val subtotal:  Double,
    val itbms:     Double,
    val total:     Double,
    val detalles:  List<PedidoItemDB>
) : Serializable

