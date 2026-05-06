package com.david.tiendavirtual.data.model

data class DashboardData(
    val ventasHoy:       Double,
    val ventasMes:       Double,
    val totalPedidosMes: Int,
    val totalProductos:  Int,
    val bajoStock:       Int,
    val agotados:        Int,
    val graficoSemana:   List<GraficoItem>
)

data class GraficoItem(
    val etiqueta:        String,
    val ingresos:        Double,
    val egresos:         Double,
    val ingresosPasados: Double = 0.0
)

