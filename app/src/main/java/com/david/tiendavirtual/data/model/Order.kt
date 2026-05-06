package com.david.tiendavirtual.data.model

data class Order(
    val id:       String,
    val fecha:    String,
    val items:    List<OrderItem>,
    val subtotal: Double,
    val itbms:    Double,
    val total:    Double
)

